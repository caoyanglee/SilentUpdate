package com.pmm.silentupdate.core

import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import com.pmm.silentupdate.BuildConfig
import com.pmm.silentupdate.SilentUpdate
import com.weimu.universalview.ktx.*
import java.io.File
import java.util.*


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
    val updateInfo = SPCenter.getUpdateInfo()
    val dialog = AlertDialog.Builder(this)
            .setCancelable(!updateInfo.isForce)
            .setTitle(updateInfo.title)
            .setMessage(updateInfo.msg)
            .setPositiveButton("立即更新", null)
            .setNegativeButton("稍后", null)
            .create()
    dialog.setOnShowListener {
        //positive
        val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        posBtn.setOnClickListener {
            if (!updateInfo.isForce) dialog.dismiss()
            this?.toast("开始下载......")
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
    val dialog = AlertDialog.Builder(this)
            .setCancelable(!updateInfo.isForce)
            .setTitle(updateInfo.title)
            .setMessage(updateInfo.msg)
            .setPositiveButton("立即安装", null)
            .setNegativeButton("稍后", null)
            .create()
    dialog.setOnShowListener {
        //positive
        val posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        posBtn.setOnClickListener {
            if (!updateInfo.isForce) dialog.dismiss()
            this?.openApkByFilePath(file)
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
internal fun ContextWrapper?.showInstallNotification(file: File) {
    this?.loge("showInstallNotification")
    val activity = this ?: return
    val notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    //判断是否在时间间隔内
    val dialogTime = SPCenter.getDialogTime()
    if (dialogTime == 0L || dialogTime.moreThanDays(SilentUpdate.intervalDay)) {
        val updateInfo = SPCenter.getUpdateInfo()
        val title = updateInfo.title
        val msg = updateInfo.msg
        val intent = activity.constructOpenApkItent(file)
        val pIntent = PendingIntent.getActivity(activity, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(activity).apply {
            this.setSmallIcon(android.R.drawable.stat_sys_download_done)// 设置小图标
            this.setLargeIcon(BitmapFactory.decodeResource(activity.resources, activity.getAppIcon()))//设置大图标
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
    if (dialogTime == 0L || dialogTime.moreThanDays(SilentUpdate.intervalDay)) {
        val updateInfo = SPCenter.getUpdateInfo()
        if (SilentUpdate.installTipDialog != null) {
            this.showCustomInstallDialog(file)
        } else {
            this.showSystemInstallDialog(updateInfo, file)
        }
    }
}

//显示 自定义-安装弹窗
private fun ContextWrapper?.showCustomInstallDialog(file: File) {
    if (this == null) return
    SilentUpdate.installTipDialog?.show(
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
    if (dialogTime == 0L || dialogTime.moreThanDays(SilentUpdate.intervalDay)) {
        //判断是否有自定义的下载弹窗
        if (SilentUpdate.downLoadTipDialog != null) {
            this.showCustomDownloadDialog(apkUrl, fileName)
        } else {
            this.showSystemDownloadDialog(apkUrl, fileName)
        }
    }
}

private fun ContextWrapper?.showCustomDownloadDialog(apkUrl: String, fileName: String) {
    if (this == null) return
    SilentUpdate.downLoadTipDialog?.show(
            context = this,
            updateInfo = SPCenter.getUpdateInfo(),
            positiveClick = { DownLoadCenter.addRequest(apkUrl, fileName, true) },
            negativeClick = {
                SPCenter.modifyDialogTime(Calendar.getInstance().time.time)//记录
            })
}