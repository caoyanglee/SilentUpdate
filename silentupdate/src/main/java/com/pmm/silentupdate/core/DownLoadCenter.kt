package com.pmm.silentupdate.core

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import java.io.File
import java.net.URI
import java.util.HashMap

/**
 * Author:你需要一台永动机
 * Date:2019-08-15 18:30
 * Description:
 */
internal object DownLoadCenter {

    private val downloadManager: DownloadManager by lazy { ContextCenter.getAppContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }
    private val appUpdateReceiver: AppUpdateReceiver by lazy { AppUpdateReceiver() }
    var onDownloadComplete: ((file: File) -> Unit)? = null

    //更新apk Wifi&Mobile
    internal fun addRequest(apkUrl: String, fileName: String?, isMobileMode: Boolean = false) {
        bindReceiver() //绑定广播接收者
        val uri = Uri.parse(apkUrl)
        loge("url=$apkUrl")
        loge("uri=$uri")
        val request = DownloadManager.Request(uri)
        //设置在什么网络情况下进行下载
        request.setAllowedNetworkTypes(if (isMobileMode) DownloadManager.Request.NETWORK_MOBILE else DownloadManager.Request.NETWORK_WIFI)
        //设置通知栏标题
        request.setNotificationVisibility(if (isMobileMode) DownloadManager.Request.VISIBILITY_VISIBLE else DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setTitle(fileName)
        request.setDescription(ContextCenter.getAppContext().packageName)
        request.setAllowedOverRoaming(false)
        request.setVisibleInDownloadsUi(true)
        //设置文件存放目录
        //request.setDestinationInExternalFilesDir(AppData.getContext(), "download", "youudo_v" + version + ".apk");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val id: Long
        try {
            id = downloadManager.enqueue(request)
            //存入到share里
            SPCenter.setDownloadTaskId(id)
        } catch (e: Exception) {
            //e.printStackTrace()
        }
    }


    //查询任务的状体
    private fun queryTaskStatus(id: Long): String {
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        while (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        }
        cursor.close()
        return ""
    }

    //下载任务是否结束
    internal fun isDownTaskProcessing(id: Long) = queryTaskStatus(id) == "192"

    //下载任务是否暂停
    internal fun isDownTaskPause(id: Long) = queryTaskStatus(id) == "193"

    //下载任务是否成功
    internal fun isDownTaskSuccess(id: Long) = queryTaskStatus(id) == "200"


    //通过下载id获取 文件地址
    private fun getFilePathByTaskId(id: Long): String {
        var filePath = ""
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        while (cursor.moveToNext()) {
            filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    ?: ""
        }
        cursor.close()
        return filePath
    }


    //下载完成
    private fun downloadComplete(intent: Intent) {
        loge("下载完成")
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        //判断ID是否一致
        if (id != SPCenter.getDownloadTaskId()) return
        loge("注销接收者")
        unbindReceiver()//注销接收者
        try {
            val uri = Uri.parse(getFilePathByTaskId(id)).toString()
            if (uri.isBlank()) {
                loge("下载了无效文件，请确定url是否可以成功请求")
                return
            }
            //必须try-catch
            val file = File(URI(uri))
            onDownloadComplete?.invoke(file)
        } catch (e: Exception) {
            e.printStackTrace()
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


    //绑定广播接收者
    internal fun bindReceiver() {
        //广播接收者
        appUpdateReceiver.onDownloadComplete = {
            downloadComplete(it)
        }
        val filter = IntentFilter()
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE")
        filter.addAction("android.intent.action.VIEW_DOWNLOADS")
        ContextCenter.getAppContext().registerReceiver(appUpdateReceiver, filter)
    }

    //取消绑定广播接收者
    internal fun unbindReceiver() {
        try {
            ContextCenter.getAppContext().unregisterReceiver(appUpdateReceiver)
        } catch (e: Exception) {
            //nothing
        }
    }


}