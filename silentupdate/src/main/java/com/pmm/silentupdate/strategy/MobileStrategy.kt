package com.pmm.silentupdate.strategy

import android.app.Activity
import android.app.AlertDialog
import android.os.Handler
import android.view.View
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
internal class MobileStrategy private constructor() : Strategy() {


    companion object {
        private var strategy: Strategy? = null

        fun getDefault(): Strategy {
            if (strategy == null) {
                synchronized(Strategy::class.java) {
                    if (strategy == null) {
                        strategy = MobileStrategy()
                    }
                }
            }
            return strategy!!
        }
    }


    //升级操作 流量的情况下
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
            loge("文件已经存在")
            if (isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                showInstallDialog(File(path)) //弹出dialog
            } else if (isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                addRequest(apkUrl, fileName, true)
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
            showUpdateTip(apkUrl, fileName)
        }
    }


    /**
     * 状态：流量
     * 显示Dialog：提示用户下载
     */
    private fun showUpdateTip(apkUrl: String, fileName: String) {
        val dialogTime = SPCenter.getDialogTime()
        if (dialogTime == 0L || dialogTime.moreThanDays(SilentUpdate.intervalDay)) {
            SilentUpdate.getCurrentActivity()?.apply {
                //判断是否有自定义的下载弹窗
                if (SilentUpdate.downLoadTipDialog != null) {
                    SilentUpdate.downLoadTipDialog?.show(
                            context = this,
                            updateInfo = SPCenter.getUpdateInfo(),
                            positiveClick = { addRequest(apkUrl, fileName, true) },
                            negativeClick = {
                                //记录
                                SPCenter.modifyDialogTime(Calendar.getInstance().time.time)
                            })
                } else {
                    showDownloadDialog(apkUrl, fileName)
                }
            }
        }
    }

    private fun Activity.showDownloadDialog(apkUrl: String, fileName: String) {
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
                addRequest(apkUrl, fileName, true)
            }
            val negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            //negative
            if (updateInfo.isForce) {
                negBtn.visibility = View.GONE
            } else {
                negBtn.setOnClickListener {
                    SPCenter.modifyDialogTime(Calendar.getInstance().time.time)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }


    //下载完成后
    override fun afterDownLoadComplete(file: File) {
        val context = SilentUpdate.getApplicationContext()
        Handler().postDelayed({
            context.openApkByFilePath(file)
        }, 200)
    }

}
