package com.pmm.silentupdate.strategy

import android.os.Handler
import com.pmm.silentupdate.core.*
import java.io.File
import java.net.URI


/**
 * 流量的情况
 */
internal class MobileUpdateStrategy : UpdateStrategy {

    init {
        //下载完成后
        DownLoadCenter.onDownloadComplete = {
            val updateInfo = SPCenter.getUpdateInfo()
            Handler().postDelayed({
                val activity = ContextCenter.getTopActivity()
                activity.showInstallDialog(it,updateInfo.isForce)//显示安装弹窗
                ContextCenter.getAppContext().openApkByUri(it)
            }, 200)
        }
    }


    //升级操作 流量的情况下
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
            loge("文件已经存在")
            if (DownLoadCenter.isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                activity.showInstallDialog(uri, isForce) //弹出dialog
            } else if (DownLoadCenter.isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                DownLoadCenter.addRequest(apkUrl, fileName, true)
            } else if (DownLoadCenter.isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            } else {
                activity.showInstallDialog(uri, isForce) //弹出dialog
            }
        } else {
            loge("显示 下载弹窗")
            activity.showDownloadDialog(apkUrl, fileName, isForce = isForce)//显示 下载弹窗
        }
    }


}
