package www.weimu.io.silentupdate.strategy

import android.app.AlertDialog
import android.app.Application
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import www.weimu.io.silentupdate.SilentUpdate
import www.weimu.io.silentupdate.core.getUpdateShare
import www.weimu.io.silentupdate.core.loge
import www.weimu.io.silentupdate.core.openApkByFilePath
import www.weimu.io.silentupdate.core.saveShareStuff
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.*

/**
 * Author:你需要一台永动机
 * Date:2018/1/22 13:49
 * Description:
 */
internal abstract class Strategy(context: Application) {
    protected var notificationManager: NotificationManager
    protected var downloadManager: DownloadManager
    private lateinit var appUpdateReceiver: AppUpdateReceiver

    init {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    //更新
    abstract fun update(apkUrl: String, latestVersion: String)

    //下载完成
//    abstract fun downloadComplete(intent: Intent)

    //下载完成后
    abstract fun afterDownLoadComplete(file: File)


    //更新apk Wifi
    protected fun addRequest(apkUrl: String, fileName: String?, isShowNotification: Boolean = false) {
        val context = SilentUpdate.getApplicationContext()
        val uri = Uri.parse(apkUrl)
        loge("url=${apkUrl}")
        loge("uri=${uri}")
        val request = DownloadManager.Request(uri)
        //设置在什么网络情况下进行下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        //设置通知栏标题
        request.setNotificationVisibility(if (isShowNotification) DownloadManager.Request.VISIBILITY_VISIBLE else DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setTitle(fileName)
        request.setDescription(context.packageName)
        request.setAllowedOverRoaming(false)
        request.setVisibleInDownloadsUi(true)
        //设置文件存放目录
        //request.setDestinationInExternalFilesDir(AppData.getContext(), "download", "youudo_v" + version + ".apk");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val id = downloadManager.enqueue(request)
        //存入到share里
        context.saveShareStuff {
            apkTaskID = id
        }
    }


    //通过下载id 找到对应的文件地址
    @Deprecated("查询下载任务的状态")
    private fun queryDownTaskById(id: Long): String? {
        var filePath: String? = null
        val query = DownloadManager.Query()

        query.setFilterById(id)
        val cursor = downloadManager.query(query)

        while (cursor.moveToNext()) {
            val downId = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
            val address = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            filePath = address
            val status = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val size = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val sizeTotal = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val map = HashMap<String, String>()
        }
        cursor.close()
        return filePath
    }


    //查询任务的状体
    protected fun queryTaskStatus(id: Long): String {
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        while (cursor.moveToNext()) {
            val status = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            return status
        }
        cursor.close()
        return ""
    }

    //下载任务是否结束
    protected fun isDownTaskProcessing(id: Long) = queryTaskStatus(id) == "192"

    //下载任务是否暂停
    protected fun isDownTaskPause(id: Long) = queryTaskStatus(id) == "193"

    //下载任务是否成功
    protected fun isDownTaskSuccess(id: Long) = queryTaskStatus(id) == "200"


    //通过下载id获取 文件地址
    protected fun getFilePathByTaskId(id: Long): String? {
        var filePath: String? = null
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        while (cursor.moveToNext()) {
            filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        }
        cursor.close()
        return filePath
    }


    //绑定广播接收者
    protected fun bindReceiver() {
        val context = SilentUpdate.getApplicationContext()
        //广播接收者
        appUpdateReceiver = AppUpdateReceiver()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE")
        filter.addAction("android.intent.action.VIEW_DOWNLOADS")
        context.registerReceiver(appUpdateReceiver, filter)
    }

    //取消绑定广播接收者
    protected fun unbindReceiver() {
        val context = SilentUpdate.getApplicationContext()
        context.unregisterReceiver(appUpdateReceiver)
    }

    //广播接收者
    private inner class AppUpdateReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                    downloadComplete(intent)
                }
            }
        }
    }

    //下载完成
    private fun downloadComplete(intent: Intent) {
        val context = SilentUpdate.getApplicationContext()
        loge("下载完成")
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        //判断ID是否一致
        if (id != context.getUpdateShare().apkTaskID) return
        loge("注销接收者")
        unbindReceiver()

        try {
            val uri = Uri.parse(getFilePathByTaskId(id)).toString()
            //必须try-catch
            val file = File(URI(uri))
            if (SilentUpdate.updateListener == null) {
                afterDownLoadComplete(file)
            } else {
                SilentUpdate.updateListener?.onDownLoadSuccess(file)
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    /**
     * 状态：WIFI
     * 显示Dialog:提示用户安装
     */
    protected fun showInstallDialog(file: File) {
        val activity = SilentUpdate.getCurrentActivity()
        AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle("发现新版本！")
                .setMessage("请点击立即安装~")
                .setPositiveButton("立即安装", { dialog, which ->
                    activity.openApkByFilePath(file)
                })
                .show()
    }


}