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
internal class MobileUpdateStrategy private constructor() : UpdateStrategy {


    companion object {
        private var strategy: UpdateStrategy? = null

        fun getDefault(): UpdateStrategy {
            if (strategy == null) {
                synchronized(UpdateStrategy::class.java) {
                    if (strategy == null) {
                        strategy = MobileUpdateStrategy()
                    }
                }
            }
            return strategy!!
        }
    }

    init {
        //下载完成后
        DownLoadCenter.onDownloadComplete = {
            val context = ContextCenter.getAppContext()
            Handler().postDelayed({
                context.openApkByFilePath(it)
            }, 200)
        }
    }


    //升级操作 流量的情况下
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
            loge("开始下载")
            //绑定广播接收者
            DownLoadCenter.bindReceiver()
            //不存在 直接下载
            showUpdateTip(apkUrl, fileName)
        }
    }


    /**
     * 状态：流量
     * 显示Dialog：提示用户下载
     */
    private fun showUpdateTip(apkUrl: String, fileName: String) {
        val activity = ContextCenter.getTopActivity() ?: return
        val dialogTime = SPCenter.getDialogTime()
        if (dialogTime == 0L || dialogTime.moreThanDays(SilentUpdate.intervalDay)) {
            //判断是否有自定义的下载弹窗
            if (SilentUpdate.downLoadTipDialog != null) {
                activity.showCustomDownloadDialog(apkUrl, fileName)
            } else {
                activity.showDownloadDialog {
                    DownLoadCenter.addRequest(apkUrl, fileName, true)
                }
            }
        }
    }

    private fun Activity.showCustomDownloadDialog(apkUrl: String, fileName: String) {
        SilentUpdate.downLoadTipDialog?.show(
                context = this,
                updateInfo = SPCenter.getUpdateInfo(),
                positiveClick = { DownLoadCenter.addRequest(apkUrl, fileName, true) },
                negativeClick = {
                    //记录
                    SPCenter.modifyDialogTime(Calendar.getInstance().time.time)
                })
    }


}
