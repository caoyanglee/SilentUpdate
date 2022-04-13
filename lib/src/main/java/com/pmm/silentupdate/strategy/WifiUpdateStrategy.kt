package com.pmm.silentupdate.strategy

import android.net.Uri
import com.pmm.silentupdate.core.*
import java.io.File
import java.net.URI

/**
 * Wifi的情况
 */
internal class WifiUpdateStrategy : UpdateStrategy {

    init {
        //下载完成后
        DownLoadCenter.onDownloadComplete = {
            val updateInfo = SPCenter.getUpdateInfo()
            //todo 锤子，努比亚 会出现不弹窗问题
            val activity = ContextCenter.getTopActivity()
            activity.showInstallNotificationV2(it)//更新Notification
            activity.showInstallDialog(it, updateInfo.isForce)//显示安装弹窗
        }
    }


    //升级操作 WIFI的情况下
    override fun update(apkUrl: String, latestVersion: String, isForce: Boolean) {
        try {
            apkUrl.checkUpdateUrl()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        val context = ContextCenter.getAppContext()
        val activity = ContextCenter.getTopActivity()
        val fileName = "${context.getAppName()}_v$latestVersion.apk"
        SPCenter.key = fileName
        val taskId = SPCenter.getDownloadTaskId(fileName)
        loge("==============")
        val uri = DownLoadCenter.getFileUriByTaskId(taskId)
        val path = Const.UPDATE_FILE_DIR + fileName
        loge("taskID=$taskId")
        loge("uri=$uri")
        if (uri != null && File(path).isFileExist()) {
            loge("【文件已经存在】")
            when {
                DownLoadCenter.isDownTaskSuccess(taskId) -> {
                    loge("任务已经下载完成")
                    //状态：完成
                    activity.showInstallDialog(uri, isForce) //弹出dialog
                }
                DownLoadCenter.isDownTaskPause(taskId) -> {
                    loge("任务已经暂停")
                    //启动下载
                    loge("继续下载")
                    DownLoadCenter.addRequest(apkUrl, fileName, false)
                }
                DownLoadCenter.isDownTaskProcessing(taskId) -> {
                    loge("任务正在执行当中")
                }
                else -> {
                    activity.showInstallDialog(uri, isForce) //弹出dialog
                }
            }
        } else {
            loge("开始下载")
            //不存在 直接下载
            DownLoadCenter.addRequest(apkUrl, fileName)
        }
    }


}
