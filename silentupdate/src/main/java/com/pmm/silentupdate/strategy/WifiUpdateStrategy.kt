package com.pmm.silentupdate.strategy

import com.pmm.silentupdate.core.*
import com.weimu.universalview.ktx.getAppName
import com.weimu.universalview.ktx.isFileExist
import java.io.File

/**
 * Wifi的情况
 */
internal class WifiUpdateStrategy : UpdateStrategy {

    init {
        //下载完成后
        DownLoadCenter.onDownloadComplete = {
            //todo 锤子，努比亚 会出现不弹窗问题
            val activity = ContextCenter.getTopActivity()
            activity.showInstallNotification(it)//更新Notification
            activity.showInstallDialog(it)//显示安装弹窗
        }
    }


    //升级操作 WIFI的情况下
    override fun update(apkUrl: String, latestVersion: String) {
        try {
            apkUrl.checkUpdateUrl()
        } catch (e: Exception) {
            e.printStackTrace()
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
            //不存在 直接下载
            DownLoadCenter.addRequest(apkUrl, fileName)
        }
    }


}
