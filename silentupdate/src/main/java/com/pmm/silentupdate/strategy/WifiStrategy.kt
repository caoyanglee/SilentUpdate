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
        SilentUpdate.getCurrentActivity().showInstallNotification(file)
    }


}
