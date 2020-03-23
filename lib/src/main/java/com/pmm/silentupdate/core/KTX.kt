package com.pmm.silentupdate.core

import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.pmm.silentupdate.BuildConfig
import com.pmm.silentupdate.R
import com.pmm.silentupdate.SilentUpdate
import java.io.File
import java.util.*


/**
 * 获取文件的Uri
 * 兼容7.0
 * @param file 文件
 */
private fun getUri4File(context: Context, file: File?): Uri {
    //获取当前app的包名
    val fileProviderAuth = "${context.packageName}.fileprovider"
    checkNotNull(file)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context.applicationContext, fileProviderAuth, file)
    } else {
        Uri.fromFile(file)
    }
}

/**
 * /构造打开APK的Intent
 */
internal fun Context.constructOpenApkIntent(file: File): Intent {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//7.0有效
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)//7.0有效
    }
    val uri = getUri4File(this, file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    return intent
}

/**
 * 直接打开APK
 */
internal fun Context.openApkByFilePath(file: File) {
    //防止有的系统 强制关闭安装未知来源的app 导致的crash
    try {
        startActivity(constructOpenApkIntent(file))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

//log
internal fun Any.loge(message: String) {
    if (BuildConfig.DEBUG) Log.e("silentUpdate", message)
}

//检查更新的URL
internal fun String.checkUpdateUrl() {
    val url = this
    if (!url.contains("http") && !url.contains("https")) {
        throw IllegalArgumentException("url must start with http or https")
    }
}

//显示 系统内置-下载弹窗
internal fun ContextWrapper?.showSystemDownloadDialog(apkUrl: String, fileName: String) {
    if (this == null) return
    val updateInfo = SPCenter.getUpdateInfo()
    val dialog = AlertDialog.Builder(this)
            .setCancelable(!updateInfo.isForce)
            .setTitle(updateInfo.title)
            .setMessage(updateInfo.msg)
            .setPositiveButton(getString(R.string.module_silentupdate_update), null)
            .setNegativeButton(getString(R.string.module_silentupdate_hold_on), null)
            .create()
    dialog.setOnShowListener {
        //positive
        val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        posBtn.setOnClickListener {
            if (!updateInfo.isForce) dialog.dismiss()
            //this?.toast("开始下载......")
            DownLoadCenter.addRequest(apkUrl, fileName, true)
        }
        val negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        //negative
        if (updateInfo.isForce) {
            negBtn.visibility = View.GONE
        } else {
            negBtn.setOnClickListener {
                dialog.dismiss()
                SPCenter.modifyDialogTime(Calendar.getInstance().time.time)
            }
        }
    }
    dialog.show()
}

//显示 系统内置-安装弹窗
internal fun ContextWrapper?.showSystemInstallDialog(updateInfo: UpdateInfo, file: File) {
    if (this == null) return
    val dialog = AlertDialog.Builder(this)
            .setCancelable(!updateInfo.isForce)
            .setTitle(updateInfo.title)
            .setMessage(updateInfo.msg)
            .setPositiveButton(getString(R.string.module_silentupdate_install), null)
            .setNegativeButton(getString(R.string.module_silentupdate_hold_on), null)
            .create()
    dialog.setOnShowListener {
        //positive
        val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        posBtn.setOnClickListener {
            if (!updateInfo.isForce) dialog.dismiss()
            this.openApkByFilePath(file)
        }
        val negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        //negative
        if (updateInfo.isForce) {
            negBtn.visibility = View.GONE
        } else {
            negBtn.setOnClickListener {
                dialog.dismiss()
                SPCenter.modifyDialogTime(Calendar.getInstance().time.time)

            }
        }
    }
    dialog.show()
}

//更新Notification
internal fun Context?.showInstallNotification(file: File) {
    this?.loge("showInstallNotification")
    val activity = this ?: return
    val notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    //判断是否在时间间隔内
    val dialogTime = SPCenter.getDialogTime()
    if (dialogTime == 0L || checkMoreThanDays(dialogTime,SilentUpdate.intervalDay)) {
        val updateInfo = SPCenter.getUpdateInfo()
        val title = updateInfo.title
        val msg = updateInfo.msg
        val intent = activity.constructOpenApkIntent(file)
        val pIntent = PendingIntent.getActivity(activity, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(activity,Const.NOTIFICATION_CHANNEL_ID).apply {
            this.setSmallIcon(android.R.drawable.stat_sys_download_done)// 设置小图标
            this.setLargeIcon(BitmapFactory.decodeResource(activity.resources, getAppIcon(activity)))//设置大图标
            this.setTicker(title)// 手机状态栏的提示----最上面的一条
            this.setWhen(System.currentTimeMillis())// 设置时间
            this.setContentTitle(title)// 设置标题
            this.setContentText(msg)// 设置通知的内容
            this.setContentIntent(pIntent)// 点击后的意图
            this.setDefaults(Notification.DEFAULT_ALL)// 设置提示全部
            this.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//锁屏通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.setChannelId(Const.NOTIFICATION_CHANNEL_ID)
            }
        }

        val notification = builder.build()// 4.1以上要用才起作用
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL// 点击后自动取消
        //显示
        notificationManager.notify(UUID.randomUUID().hashCode(), notification)
    }
}


/**
 * 状态：文件已经存在或Wifi情况下下载完成
 * 显示Dialog:提示用户安装
 */
internal fun ContextWrapper?.showInstallDialog(file: File) {
    this?.loge("showInstallDialog")
    //判断是否在时间间隔内
    val dialogTime = SPCenter.getDialogTime()
    if (dialogTime == 0L || checkMoreThanDays(dialogTime,SilentUpdate.intervalDay)) {
        val updateInfo = SPCenter.getUpdateInfo()
        if (SilentUpdate.installDialogShowAction != null) {
            this.showCustomInstallDialog(file)
        } else {
            this.showSystemInstallDialog(updateInfo, file)
        }
    }
}

//显示 自定义-安装弹窗
private fun ContextWrapper?.showCustomInstallDialog(file: File) {
    if (this == null) return
    SilentUpdate.installDialogShowAction?.show(
            context = this,
            updateInfo = SPCenter.getUpdateInfo(),
            positiveClick = { this.openApkByFilePath(file) },
            negativeClick = {
                SPCenter.modifyDialogTime(Calendar.getInstance().time.time)//记录
            }
    )
}


/**
 * 状态：流量
 * 显示Dialog：提示用户下载
 */
internal fun ContextWrapper?.showDownloadDialog(apkUrl: String, fileName: String) {
    this?.loge("showDownloadDialog")
    val dialogTime = SPCenter.getDialogTime()
    if (dialogTime == 0L || checkMoreThanDays(dialogTime,SilentUpdate.intervalDay)) {
        //判断是否有自定义的下载弹窗
        if (SilentUpdate.downLoadDialogShowAction != null) {
            this.showCustomDownloadDialog(apkUrl, fileName)
        } else {
            this.showSystemDownloadDialog(apkUrl, fileName)
        }
    }
}

private fun ContextWrapper?.showCustomDownloadDialog(apkUrl: String, fileName: String) {
    if (this == null) return
    SilentUpdate.downLoadDialogShowAction?.show(
            context = this,
            updateInfo = SPCenter.getUpdateInfo(),
            positiveClick = { DownLoadCenter.addRequest(apkUrl, fileName, true) },
            negativeClick = {
                SPCenter.modifyDialogTime(Calendar.getInstance().time.time)//记录
            })
}

/**
 * 比较时间 是否超过几天
 * 单位：毫秒
 */
private fun checkMoreThanDays(timeMillis:Long, day: Int = 7): Boolean {
    val currentTime = Calendar.getInstance().time.time
    if (timeMillis == 0L) return true
    val differ = currentTime - timeMillis
    if (differ > 1000 * 60 * 60 * 24 * day) {
        return true
    }
    return false
}


/**
 * 获取app的图片
 */
private fun getAppIcon(context:Context): Int {
    val pm: PackageManager = context.packageManager
    try {
        val info = pm.getApplicationInfo(context.packageName, 0)
        return info.icon
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return -1
}

/**
 * 获取应用的名字
 */
internal fun Context.getAppName(): String? {
    val pm: PackageManager = packageManager
    try {
        val info = pm.getApplicationInfo(this.packageName, 0)
        return info.loadLabel(pm).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return ""
}

/**
 * 是否存在文件
 */
internal fun File.isFileExist(): Boolean {
    if (TextUtils.isEmpty(this.path)) return false
    return this.exists() && this.isFile
}