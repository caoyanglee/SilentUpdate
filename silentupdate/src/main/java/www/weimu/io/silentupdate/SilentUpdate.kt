package www.weimu.io.silentupdate

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import www.weimu.io.silentupdate.core.*
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.*


object SilentUpdate {
    private val activityStack = Stack<Activity>()
    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var appUpdateReceiver: AppUpdateReceiver


    private var fileDirectory = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DOWNLOADS + "/"


    //以下数据可配置
    var downloadListener: DownloadListener? = null
    var isShowDialog: Boolean = true
    var isShowNotification: Boolean = true


    private fun getCurrentActivity() = activityStack.peek()
    private fun getApplicationContext() = getCurrentActivity().applicationContext

    //链接至Application
    fun init(mContext: Application) {
        downloadManager = mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        //登记activity
        activityStack.clear()
        mContext.registerActivityLifecycleCallbacks(object : ActivityLifeListener() {

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                activityStack.add(activity)
            }

            override fun onActivityDestroyed(activity: Activity?) {
                activityStack.remove(activity)
            }
        })
    }

    //核心操作
    fun update(apkUrl: String, latestVersion: String) {
        val context = getApplicationContext()
        if (context.isConnectWifi()) {
            updateByWifi(apkUrl, latestVersion)
        } else {
            updateByMobile(apkUrl, latestVersion)
        }
    }


    //升级操作 WIFI的情况下
    private fun updateByWifi(apkUrl: String, latestVersion: String) {
        val context = getApplicationContext()
        val fileName = "${context.getAppName()}_v$latestVersion.apk"
        val path = fileDirectory + fileName

        val taskId = context.getUpdateShare().apkTaskID
        loge("taskID=$taskId")
        if (isFileExist(path)) {
            loge("文件已经存在")
            if (isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                //状态：完成
                if (isShowDialog) showDialog(File(path)) //若存在且下载完成  弹出dialog
                downloadListener?.onFileIsExist(File(path))
            } else if (isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                updateApkByWifi(apkUrl, fileName)
            } else if (isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            }
        } else {
            loge("开始下载")
            //绑定广播接收者
            bindReceiver()
            //不存在 直接下载
            updateApkByWifi(apkUrl, fileName)
        }
    }


    //升级操作  流量的情况下
    private fun updateByMobile(apkUrl: String, latestVersion: String) {
        val context = getApplicationContext()
        val fileName = "${context.getAppName()}_v$latestVersion.apk"
        val path = fileDirectory + fileName

        val taskId = context.getUpdateShare().apkTaskID
        loge("taskID=$taskId")
        if (isFileExist(path)) {
            loge("文件已经存在")
            if (isDownTaskSuccess(taskId)) {
                loge("任务已经下载完成")
                //状态：完成
                if (isShowDialog) showDialog(File(path)) //若存在且下载完成  弹出dialog
                downloadListener?.onFileIsExist(File(path))
            } else if (isDownTaskPause(taskId)) {
                loge("任务已经暂停")
                //启动下载
                loge("继续下载")
                showUpdateTip(apkUrl,fileName)
            } else if (isDownTaskProcessing(taskId)) {
                loge("任务正在执行当中")
            }
        } else {
            loge("开始下载")
            //绑定广播接收者
            bindReceiver()
            //不存在 直接下载
            showUpdateTip(apkUrl,fileName)
        }
    }

    //绑定广播接收者
    private fun bindReceiver() {
        val context = getApplicationContext()
        //广播接收者
        appUpdateReceiver = AppUpdateReceiver()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE")
        filter.addAction("android.intent.action.VIEW_DOWNLOADS")
        context.registerReceiver(appUpdateReceiver, filter)

    }

    //更新apk Wifi
    private fun updateApkByWifi(apkUrl: String, fileName: String?) {
        val context = getApplicationContext()
        val uri = Uri.parse(apkUrl)
        loge("url=${apkUrl}")
        loge("uri=${uri}")
        val request = DownloadManager.Request(uri)
        //设置在什么网络情况下进行下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
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


    //更新apk mobile
    private fun updateApkByMobile(apkUrl: String, fileName: String?) {
        val context = getApplicationContext()
        val uri = Uri.parse(apkUrl)
        loge("url=${apkUrl}")
        loge("uri=${uri}")
        val request = DownloadManager.Request(uri)
        //设置在什么网络情况下进行下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
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
    private fun queryTaskStatus(id: Long): String {
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
    private fun isDownTaskProcessing(id: Long) = queryTaskStatus(id) == "192"

    //下载任务是否暂停
    private fun isDownTaskPause(id: Long) = queryTaskStatus(id) == "193"

    //下载任务是否成功
    private fun isDownTaskSuccess(id: Long) = queryTaskStatus(id) == "200"


    //通过下载id获取 文件地址
    private fun getFilePathByTaskId(id: Long): String? {
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

    //广播接收者
    private class AppUpdateReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                    loge("下载完成")
                    downloadComplete(intent)
                }
            }
        }
    }


    //下载完成
    private fun downloadComplete(intent: Intent) {
        val context = getApplicationContext()
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        //判断ID是否一致
        if (id != context.getUpdateShare().apkTaskID) return
        loge("注销接收者")
        context.unregisterReceiver(appUpdateReceiver)

        try {
            val uri = Uri.parse(getFilePathByTaskId(id)).toString()
            //必须try-catch
            val file = File(URI(uri))
            if (context.isConnectWifi()){
                if (isShowNotification) showNotification(file)
                if (isShowDialog) showDialog(file)
            }else{
                context.openApkByFilePath(file)
            }

            downloadListener?.onDownLoadSuccess(file)

        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }


    /**
     * 状态：流量
     * 显示Dialog：提示用户下载
     */
    private fun showUpdateTip(apkUrl: String, fileName: String?) {
        val activity = getCurrentActivity()
        AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle("发现新版本！")
                .setMessage("请点击下载更新~")
                .setPositiveButton("更新", { dialog, which ->
                    updateApkByMobile(apkUrl, fileName)
                })
                .setNegativeButton("取消", null)
                .show()
    }


    /**
     * 状态：WIFI
     * 显示Dialog:提示用户安装
     */
    private fun showDialog(file: File) {
        val activity = getCurrentActivity()
        AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle("发现新版本！")
                .setMessage("请点击立即安装~")
                .setPositiveButton("立即安装", { dialog, which ->
                    activity.openApkByFilePath(file)
                })
                .show()
    }


    /**
     * 更新notification
     *
     */
    private fun showNotification(file: File) {
        val context = getCurrentActivity()

        val title = "发现新版本！"
        val content: String = "请点击立即安装~"
        val intent = context.constructOpenApkItent(file)
        val pintent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = Notification.Builder(context)
        builder.setSmallIcon(-1)// 设置图标
        builder.setTicker(title)// 手机状态栏的提示----最上面的一条
        builder.setWhen(System.currentTimeMillis())// 设置时间
        builder.setContentTitle(title)// 设置标题
        builder.setContentText(content)// 设置通知的内容
        builder.setContentIntent(pintent)// 点击后的意图
        builder.setDefaults(Notification.DEFAULT_ALL)// 设置提示全部
        val notification = builder.build()// 4.1以上要用才起作用
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL// 点击后自动取消
        //显示
        notificationManager.notify(UUID.randomUUID().hashCode(), notification)
    }


}



