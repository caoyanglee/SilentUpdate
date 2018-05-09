package www.weimu.io.silentupdate.strategy

import android.app.AlertDialog
import android.app.Application
import android.os.Handler
import www.weimu.io.silentupdate.SilentUpdate
import www.weimu.io.silentupdate.core.*
import java.io.File


/**
 * 流量的情况
 */
internal class MobileStrategy(context: Application) : Strategy(context) {

    //升级操作 流量的情况下
    override fun update(apkUrl: String, latestVersion: String) {
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
                if (SilentUpdate.isUseDefaultHint) showInstallDialog(File(path)) //弹出dialog
                SilentUpdate.updateListener?.onFileIsExist(File(path))
            } else if (isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                addRequest(apkUrl, fileName, true)
            } else if (isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            }else{
                if (SilentUpdate.isUseDefaultHint) showInstallDialog(File(path)) //弹出dialog
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
        val activity = SilentUpdate.getCurrentActivity()
        AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle("提示！")
                .setMessage("发现新版本！请点击立即更新。")
                .setPositiveButton("更新", { dialog, which ->
                    addRequest(apkUrl, fileName, true)
                })
                .setNegativeButton("稍后", null)
                .show()
    }


}
