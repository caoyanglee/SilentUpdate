package www.weimu.io.silentupdate.strategy

import android.app.AlertDialog
import android.app.Application
import android.os.Handler
import www.weimu.io.silentupdate.SilentUpdate
import www.weimu.io.silentupdate.core.*
import java.io.File
import java.util.*


/**
 * 流量的情况
 */
internal class MobileStrategy : Strategy {


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

    private constructor()


    //升级操作 流量的情况下
    override fun update(apkUrl: String, latestVersion: String) {
        checkUpdateUrl(apkUrl)
        val context = SilentUpdate.getApplicationContext()
        val fileName = "${context.getAppName()}_v$latestVersion.apk"
        val path = Const.UPDATE_FILE_DIR + fileName

        val taskId = context.getUpdateShare().apkTaskID
        loge("==============")
        loge("taskID=$taskId")
        if (isFileExist(path)) {
            loge("文件已经存在")
            if (isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                //状态：完成
                if (SilentUpdate.isUseDefaultHint) {
                    showInstallDialog(File(path)) //弹出dialog
                } else {
                    SilentUpdate.updateListener?.onFileIsExist(File(path))
                }
            } else if (isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                addRequest(apkUrl, fileName, true)
            } else if (isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            } else {
                if (SilentUpdate.isUseDefaultHint) {
                    showInstallDialog(File(path)) //弹出dialog
                } else {
                    SilentUpdate.updateListener?.onFileIsExist(File(path))
                }
            }
        } else {
            loge("开始下载")
            //绑定广播接收者
            bindReceiver()
            //不存在 直接下载
            showUpdateTip(apkUrl, fileName)
        }
    }

    //下载完成后
    override fun afterDownLoadComplete(file: File) {
        val context = SilentUpdate.getApplicationContext()
        Handler().postDelayed({
            context.openApkByFilePath(file)
        }, 200)
    }


    /**
     * 状态：流量
     * 显示Dialog：提示用户下载
     */
    private fun showUpdateTip(apkUrl: String, fileName: String?) {
        val dialogTime = SPCenter.getDialogTime()
        if (dialogTime == 0L || dialogTime.moreThanDays(SilentUpdate.intervalDay)) {
            SilentUpdate.getCurrentActivity()?.apply {
                AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("提示")
                        .setMessage("发现新版本！请点击立即更新。")
                        .setPositiveButton("更新") { dialog, which ->
                            addRequest(apkUrl, fileName, true)
                        }
                        .setNegativeButton("稍后") { dialog, which ->
                            //记录
                            SPCenter.modifyDialogTime(Calendar.getInstance().time.time)
                        }
                        .show()
            }
        }
    }


}
