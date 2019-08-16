package com.pmm.silentupdate.strategy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import com.pmm.silentupdate.core.*
import com.weimu.universalview.ktx.*
import java.io.File

/**
 * Wifi的情况
 */
internal class WifiUpdateStrategy private constructor() : UpdateStrategy {

    companion object {
        private var strategy: UpdateStrategy? = null


        fun getDefault(): UpdateStrategy {
            if (strategy == null) {
                synchronized(UpdateStrategy::class.java) {
                    if (strategy == null) {
                        strategy = WifiUpdateStrategy()
                    }
                }
            }
            return strategy!!
        }
    }

    init {
        //增加通知频道【兼容8.0】
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        //下载完成后
        DownLoadCenter.onDownloadComplete = {
            val activity = ContextCenter.getTopActivity()
            activity.showInstallNotification(it)//更新Notification
            activity.showInstallDialog(it)//显示安装弹窗
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val context = ContextCenter.getAppContext()
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 通知渠道的id
        // 用户可以看到的通知渠道的名字.
        val name = "${context.getAppName()}更新专用"
        // 用户可以看到的通知渠道的描述
        val description = "${context.getAppName()}更新专用"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(Const.NOTIFICATION_CHANNEL_ID, name, importance)
        // 配置通知渠道的属性
        mChannel.description = description
        // 设置通知出现时的闪灯（如果 android 设备支持的话）
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        // 设置通知出现时的震动（如果 android 设备支持的话）
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        //最后在notificationmanager中创建该通知渠道
        mNotificationManager.createNotificationChannel(mChannel);
    }

    //升级操作 WIFI的情况下
    override fun update(apkUrl: String, latestVersion: String) {
        try {
            apkUrl.checkUpdateUrl()
        } catch (e: Exception) {
            return
        }

        val context = ContextCenter.getAppContext()
        val activity = ContextCenter.getTopActivity()
        val fileName = "${context.getAppName()}_v$latestVersion.apk"
        val path = Const.UPDATE_FILE_DIR + fileName

        val taskId = SPCenter.getDownloadTaskId()
        loge("==============")
        loge("taskID=$taskId")
        if (File(path).isFileExist()) {
            loge("【文件已经存在】")
            if (DownLoadCenter.isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                //状态：完成
                activity.showInstallDialog(File(path)) //弹出dialog
            } else if (DownLoadCenter.isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                DownLoadCenter.addRequest(apkUrl, fileName, false)
            } else if (DownLoadCenter.isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            } else {
                activity.showInstallDialog(File(path)) //弹出dialog
            }
        } else {
            loge("开始下载")
            //绑定广播接收者
            DownLoadCenter.bindReceiver()
            //不存在 直接下载
            DownLoadCenter.addRequest(apkUrl, fileName)
        }
    }


}
