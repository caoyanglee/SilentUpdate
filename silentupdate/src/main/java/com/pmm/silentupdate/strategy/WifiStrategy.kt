package com.pmm.silentupdate.strategy

import android.app.Notification
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.pmm.silentupdate.SilentUpdate
import com.pmm.silentupdate.core.*
import com.weimu.universalview.ktx.*
import java.io.File
import java.util.*

/**
 * Wifi的情况
 */
internal class WifiStrategy private constructor() : Strategy() {

    companion object {
        private var strategy: Strategy? = null


        fun getDefault(): Strategy {
            if (strategy == null) {
                synchronized(Strategy::class.java) {
                    if (strategy == null) {
                        strategy = WifiStrategy()
                    }
                }
            }
            return strategy!!
        }
    }

    //升级操作 WIFI的情况下
    override fun update(apkUrl: String, latestVersion: String) {
        try {
            checkUpdateUrl(apkUrl)
        } catch (e: Exception) {
            return
        }
        val context = SilentUpdate.getApplicationContext()
        val fileName = "${context.getAppName()}_v$latestVersion.apk"
        val path = Const.UPDATE_FILE_DIR + fileName

        val taskId = context.getUpdateShare().apkTaskID
        loge("==============")
        loge("taskID=$taskId")
        if (File(path).isFileExist()) {
            loge("【文件已经存在】")
            if (isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                //状态：完成
                showInstallDialog(File(path)) //弹出dialog
            } else if (isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                addRequest(apkUrl, fileName, false)
            } else if (isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            } else {
                showInstallDialog(File(path)) //弹出dialog
            }
        } else {
            loge("开始下载")
            //绑定广播接收者
            bindReceiver()
            //不存在 直接下载
            addRequest(apkUrl, fileName)
        }
    }

    //下载完成后
    override fun afterDownLoadComplete(file: File) {
        showInstallNotification(file)
        showInstallDialog(file)
    }


    //更新Notification
    private fun showInstallNotification(file: File) {
        //判断是否在时间间隔内
        val dialogTime = SPCenter.getDialogTime()
        if (dialogTime == 0L || dialogTime.moreThanDays(SilentUpdate.intervalDay)) {
            val updateInfo = SPCenter.getUpdateInfo()
            val activity = SilentUpdate.getCurrentActivity() ?: return
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


}
