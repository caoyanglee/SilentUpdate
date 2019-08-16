package com.pmm.silentupdate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import com.pmm.silentupdate.core.*
import com.pmm.silentupdate.core.Const
import com.pmm.silentupdate.core.SPCenter
import com.pmm.silentupdate.strategy.MobileUpdateStrategy
import com.pmm.silentupdate.strategy.UpdateStrategy
import com.pmm.silentupdate.strategy.WifiUpdateStrategy
import com.weimu.universalview.helper.FileHelper
import com.weimu.universalview.ktx.getAppName
import com.weimu.universalview.ktx.isConnectWifi


object SilentUpdate {

    //以下数据可配置
    var downLoadTipDialog: DialogTipAction? = null//自定义 下载Dialog -> 流量模式
    var installTipDialog: DialogTipAction? = null//自定义  安装Dialog -> 无线模式,文件已存在
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()
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
            context.isConnectWifi() -> wifiUpdateStrategy
            //流量
            else -> mobileUpdateStrategy
        }
        strategy.update(apkUrl, latestVersion)
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

    /**
     * 删除apk 手动删除 已安装的apk
     * @param version 版本
     */
    fun deleteApk(version: String): Boolean {
        val context = ContextCenter.getAppContext()
        val path = "${Const.UPDATE_FILE_DIR}${context.getAppName()}_v$version.apk"
        return FileHelper.deleteFile(path)
    }

    /**
     * 增加通知栏的频道
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val context = ContextCenter.getAppContext()
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

}



