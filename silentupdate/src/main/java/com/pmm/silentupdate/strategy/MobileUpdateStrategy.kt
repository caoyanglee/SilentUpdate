package com.pmm.silentupdate.strategy

import android.app.Activity
import android.os.Handler
import com.pmm.silentupdate.SilentUpdate
import com.pmm.silentupdate.core.*
import com.weimu.universalview.ktx.getAppName
import com.weimu.universalview.ktx.isFileExist
import com.weimu.universalview.ktx.moreThanDays
import com.weimu.universalview.ktx.openApkByFilePath
import java.io.File
import java.util.*


/**
 * 流量的情况
 */
internal class MobileUpdateStrategy : UpdateStrategy {

    init {
        //下载完成后
        DownLoadCenter.onDownloadComplete = {
            Handler().postDelayed({
                val activity = ContextCenter.getTopActivity()
                activity.showInstallDialog(it)//显示安装弹窗
                ContextCenter.getAppContext().openApkByFilePath(it)
            }, 200)
        }
    }


    //升级操作 流量的情况下
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
            loge("文件已经存在")
            if (DownLoadCenter.isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                activity.showInstallDialog(File(path)) //弹出dialog
            } else if (DownLoadCenter.isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                DownLoadCenter.addRequest(apkUrl, fileName, true)
            } else if (DownLoadCenter.isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            } else {
                activity.showInstallDialog(File(path)) //弹出dialog
            }
        } else {
            loge("显示 下载弹窗")
            activity.showDownloadDialog(apkUrl, fileName)//显示 下载弹窗
        }
    }


}
