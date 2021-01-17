package com.pmm.silentupdate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import com.pmm.silentupdate.core.*
import com.pmm.silentupdate.strategy.MobileUpdateStrategy
import com.pmm.silentupdate.strategy.UpdateStrategy
import com.pmm.silentupdate.strategy.WifiUpdateStrategy
import java.io.File


object SilentUpdate {

    //以下数据可配置
    var downLoadDialogShowAction: DialogShowAction? = null//自定义 下载Dialog -> 流量模式
    var installDialogShowAction: DialogShowAction? = null//自定义  安装Dialog -> 无线模式,文件已存在
    var intervalDay = 7//间隔弹窗提示时间-默认7天后提醒-仅仅适用于【isUseDefaultHint=true】

    private val mobileUpdateStrategy by lazy { MobileUpdateStrategy() }
    private val wifiUpdateStrategy by lazy { WifiUpdateStrategy() }

    /**
     * 静默更新的初始化
     * @param App的上下文
     */
    fun init(context: Application) {
        //上下文初始化
        ContextCenter.init(context)
        //增加通知频道【兼容8.0】
        val channelName = context.getString(R.string.module_silentupdate_channelName)
        createNotificationChannel(
                context = context,
                channelId = Const.NOTIFICATION_CHANNEL_ID,
                channelName = channelName,
                channelDesc = channelName
        )
        //创建更新要用的apk文件夹
        val state = Environment.getExternalStorageState()
        val rootDir = if (state == Environment.MEDIA_MOUNTED)
            "${(context.externalCacheDir?.absolutePath) ?: ""}/${Environment.DIRECTORY_DOWNLOADS}/"
        else
            context.cacheDir
        val folderDir = File("$rootDir")
        if (!folderDir.exists()) folderDir.mkdirs()
    }

    /**
     * 增加通知栏的频道 通用库更新后可删除
     * @param importance NotificationManager.IMPORTANCE_LOW
     */
    private fun createNotificationChannel(
            context: Context,
            channelId: String,
            channelName: String,
            channelDesc: String = "",
            importance: Int = 0,
            enableVibration: Boolean = true,
            lightColor: Int = Color.GREEN
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // channelId 通知渠道的id
            // channelName 用户可以看到的通知渠道的名字.
            // importance 用户可以看到的通知渠道的描述
            val mChannel = NotificationChannel(channelId, channelName, importance)
            // 配置通知渠道的属性
            mChannel.description = channelDesc
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true)
            mChannel.lightColor = lightColor
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(enableVibration)
            //mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            //最后在NotificationManager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }


    /**
     * 更新操作
     * - 流量模式
     * - WIFI模式
     * @param apkUrl app的下载地址
     * @param latestVersion 最新的版本号
     */
    fun update(receive: UpdateInfo.() -> Unit) {
        val updateInfo = UpdateInfo()
        updateInfo.receive()
        val apkUrl = updateInfo.apkUrl
        val latestVersion = updateInfo.latestVersion
        if (apkUrl.isBlank() or latestVersion.isBlank()) return
        SPCenter.modifyUpdateInfo(updateInfo)

        val context = ContextCenter.getAppContext()

        //策略模式
        val strategy: UpdateStrategy = when {
            //WIFI
            isConnectWifi(context) -> wifiUpdateStrategy
            //流量
            else -> mobileUpdateStrategy
        }
        strategy.update(apkUrl, latestVersion)
    }

    //是否连接Wifi
    private fun isConnectWifi(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            val type = networkInfo.type
            if (type == ConnectivityManager.TYPE_WIFI) {
                return true
            }
        }
        return false
    }

    /**
     * 主动更新 同流量模式
     * 检查本地文件，有直接显示下载弹窗
     */
    fun activeUpdate(receive: UpdateInfo.() -> Unit) {
        val updateInfo = UpdateInfo()
        updateInfo.receive()
        val apkUrl = updateInfo.apkUrl
        val latestVersion = updateInfo.latestVersion
        if (apkUrl.isBlank() or latestVersion.isBlank()) return
        SPCenter.modifyUpdateInfo(updateInfo)

        //策略模式
        mobileUpdateStrategy.update(apkUrl, latestVersion)
    }


    /**
     * 清除sp缓存数据
     */
    fun clearCache() {
        SPCenter.clearDownloadTaskId()
        SPCenter.clearDialogTime()
        SPCenter.clearUpdateInfo()
    }
}



