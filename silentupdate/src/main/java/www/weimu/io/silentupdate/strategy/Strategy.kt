package www.weimu.io.silentupdate.strategy

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import www.weimu.io.silentupdate.SilentUpdate
import www.weimu.io.silentupdate.core.*
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.*

/**
 * Author:你需要一台永动机
 * Date:2018/1/22 13:49
 * Description:
 */
internal abstract class Strategy : StrategyAction {
    protected var notificationManager: NotificationManager
    protected var downloadManager: DownloadManager
    private lateinit var appUpdateReceiver: AppUpdateReceiver

    init {
        val context = SilentUpdate.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        //增加通知频道【兼容8.0】
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }
    }

    //检查更新的URL
    override fun checkUpdateUrl(url: String) {
        if (!url.contains("http") && !url.contains("https")) {
            throw IllegalArgumentException("url must start with http or https")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 通知渠道的id
        // 用户可以看到的通知渠道的名字.
        val name = "${context.getAppName()}更新专用"
        // 用户可以看到的通知渠道的描述
        val description = "${context.getAppName()}更新专用"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(Const.NOTIFICATION_CHANNEL_ID, name, importance)
        // 配置通知渠道的属性
        mChannel.description = description
        // 设置通知出现时的闪灯（如果 android 设备支持的话）
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        // 设置通知出现时的震动（如果 android 设备支持的话）
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        //最后在notificationmanager中创建该通知渠道
        mNotificationManager.createNotificationChannel(mChannel);
    }

    //更新
    abstract fun update(apkUrl: String, latestVersion: String)


    //下载完成后
    abstract fun afterDownLoadComplete(file: File)


    //更新apk Wifi&Mobile
    protected fun addRequest(apkUrl: String, fileName: String?, isMobileMode: Boolean = false) {
        val context = SilentUpdate.getApplicationContext()
        val uri = Uri.parse(apkUrl)
        loge("url=${apkUrl}")
        loge("uri=${uri}")
        val request = DownloadManager.Request(uri)
        //设置在什么网络情况下进行下载
        request.setAllowedNetworkTypes(if (isMobileMode) DownloadManager.Request.NETWORK_MOBILE else DownloadManager.Request.NETWORK_WIFI)
        //设置通知栏标题
        request.setNotificationVisibility(if (isMobileMode) DownloadManager.Request.VISIBILITY_VISIBLE else DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setTitle(fileName)
        request.setDescription(context.packageName)
        request.setAllowedOverRoaming(false)
        request.setVisibleInDownloadsUi(true)
        //设置文件存放目录
        //request.setDestinationInExternalFilesDir(AppData.getContext(), "download", "youudo_v" + version + ".apk");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val id: Long
        try {
            id = downloadManager.enqueue(request)
            //存入到share里
            context.saveShareStuff {
                apkTaskID = id
            }
        } catch (e: Exception) {
            //e.printStackTrace()
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
    protected fun isDownTaskProcessing(id: Long) = queryTaskStatus(id) == "192"

    //下载任务是否暂停
    protected fun isDownTaskPause(id: Long) = queryTaskStatus(id) == "193"

    //下载任务是否成功
    protected fun isDownTaskSuccess(id: Long) = queryTaskStatus(id) == "200"


    //通过下载id获取 文件地址
    private fun getFilePathByTaskId(id: Long): String {
        var filePath = ""
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        while (cursor.moveToNext()) {
            filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)) ?: ""
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
        try {
            val context = SilentUpdate.getApplicationContext()
            context.unregisterReceiver(appUpdateReceiver)
        } catch (e: Exception) {

        }
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
            if (uri.isBlank()) {
                loge("下载了无效文件，请确定url是否可以成功请求")
                return
            }
            //必须try-catch
            val file = File(URI(uri))
            afterDownLoadComplete(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 状态：文件已经存在或Wifi情况下下载完成
     * 显示Dialog:提示用户安装
     */
    protected fun showInstallDialog(file: File) {
        //判断是否在时间间隔内
        val dialogTime = SPCenter.getDialogTime()
        if (dialogTime == 0L || dialogTime.moreThanDays(SilentUpdate.intervalDay)) {
            SilentUpdate.getCurrentActivity()?.apply {
                if (SilentUpdate.installTipDialog != null) {
                    SilentUpdate.installTipDialog?.show(
                            context = this,
                            updateContent = SPCenter.getUpdateContent(),
                            positiveClick = { openApkByFilePath(file) },
                            negativeClick = {
                                //记录
                                SPCenter.modifyDialogTime(Calendar.getInstance().time.time)
                            })
                } else {

                    AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("提示")
                            .setMessage("发现新版本！请点击立即安装。")
                            .setPositiveButton("立即安装") { dialog, which ->
                                openApkByFilePath(file)
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


}