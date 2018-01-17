package www.weimu.io.silentupdate

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import www.weimu.io.silentupdate.model.UpdateShare
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.*


@SuppressLint("StaticFieldLeak")
/**
 * Author:你需要一台永动机
 * Date:2017/11/22 14:00
 * Description:
 */
object UpdateCenter {
    private val activityList = Stack<Activity>()
    private lateinit var mContext: Application
    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var appUpdateReceiver: AppUpdateReceiver
    private lateinit var packageManager: PackageManager
    private lateinit var updateShare: UpdateShare


    //以下数据可配置
    var downloadListener: DownloadListener? = null
    var isShowDialog: Boolean = true
    var isShowNotification: Boolean = true
    var fileDirectory = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DOWNLOADS + "/"

    private fun getCurrentActivity() = activityList.peek()

    //链接至Application
    fun attach(mContext: Application) {
        this.mContext = mContext
        downloadManager = mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        packageManager = mContext.packageManager;
        updateShare = UpdateShare(mContext)

        //登记activity
        activityList.clear()
        this.mContext.registerActivityLifecycleCallbacks(object : WMActivityLifeCycleCallbacks() {

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                activityList.add(activity)
            }

            override fun onActivityDestroyed(activity: Activity?) {
                activityList.remove(activity)
            }
        })
    }


    //分离Application
    fun detach() {
        //this.mContext.unregisterReceiver(appUpdateReceiver)
        updateShare.saveShareStuff { isDownloading = false }
        activityList.clear()
    }


    //获取apk  不管是网络还是本地
    fun obtainLatestApk(apkUrl: String, latestVersion: String) {

        //绑定广播接收者
        bindReceiver()

        val path = fileDirectory + "${getAppName()}_v$latestVersion.apk"

        val isExist = FileUtils.isFileExist(path)
        //Logger.e("path=${path}  是否存在=" + isExist)
        if (isExist && !updateShare.getUpdateShare().isDownloading) {
            if (isShowDialog) showDialog(File(path)) //若存在且下载完成  弹出dialog
            downloadListener?.onFileIsExist(File(path))
        } else {
            updateApkByHide(apkUrl, latestVersion)//不存在 直接下载
        }
    }

    private fun bindReceiver() {
        //广播接收者
        appUpdateReceiver = AppUpdateReceiver()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE")
        filter.addAction("android.intent.action.VIEW_DOWNLOADS")
        this.mContext.registerReceiver(appUpdateReceiver, filter)
    }

    //更新apk hide
    private fun updateApkByHide(apkUrl: String, latestVersion: String?) {
        val request = DownloadManager.Request(Uri.parse(apkUrl))
        //设置在什么网络情况下进行下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setTitle("${getAppName()}_v" + latestVersion)
        request.setDescription(mContext.getPackageName())
        request.setAllowedOverRoaming(false)
        request.setVisibleInDownloadsUi(true)
        //设置文件存放目录
        //request.setDestinationInExternalFilesDir(AppData.getContext(), "download", "youudo_v" + version + ".apk");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${getAppName()}_v$latestVersion.apk")

        val id = downloadManager.enqueue(request)
        //存入到share里
        updateShare.saveShareStuff {
            apkTaskID = id
            isDownloading = true
        }
    }


    //通过下载id 找到对应的文件地址
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
            val statuss = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val size = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val sizeTotal = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val map = HashMap<String, String>()
            map.put("downid", downId)
            map.put("title", title)
            map.put("address", address)
            map.put("sizeTotal", sizeTotal)
        }
        cursor.close()
        return filePath
    }

    //广播接收者
    class AppUpdateReceiver : BroadcastReceiver() {

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
        Log.e("weimu","downloadComplete")
        updateShare.saveShareStuff {
            isDownloading = false
        }
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        //判断ID是否一致
        if (id != updateShare.getUpdateShare().apkTaskID) return

        val uri = Uri.parse(queryDownTaskById(id)).toString()
        try {
            //必须try-catch
            val file = File(URI(uri))
            if (isShowNotification) showNotification(file)
            if (isShowDialog) showDialog(file)
            downloadListener?.onDownLoadSuccess(file)

        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    /**
     * 显示更新dialog
     */
    private fun showDialog(file: File) {
        val activity = getCurrentActivity()
        AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle("软件更新")
                .setMessage("新版本已下载完成,请立即安装更新！")
                .setPositiveButton("立即安装", { dialog, which ->
                    activity.openApkByFilePath(file)
                })
                .show()
    }

    /**
     * 更新notification
     *
     * file 文件地址
     */
    private fun showNotification(file: File, content: String = "请点击立即安装~") {
        val title = "发现新版本！"
        val intent = mContext.constructOpenApkItent(file)
        val pintent = PendingIntent.getActivity(mContext, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = Notification.Builder(mContext)
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


    /*
     * 获取程序的名字
     */
    private fun getAppName(): String? {

        try {
            val info = packageManager.getApplicationInfo(getCurrentActivity().packageName, 0)
            return info.loadLabel(packageManager).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }


}



