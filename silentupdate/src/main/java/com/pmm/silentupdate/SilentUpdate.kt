package com.pmm.silentupdate

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.pmm.silentupdate.core.*
import com.pmm.silentupdate.core.ActivityLifeListener
import com.pmm.silentupdate.core.Const
import com.pmm.silentupdate.core.SPCenter
import com.pmm.silentupdate.strategy.MobileStrategy
import com.pmm.silentupdate.strategy.Strategy
import com.pmm.silentupdate.strategy.WifiStrategy
import com.weimu.universalview.helper.FileHelper
import com.weimu.universalview.ktx.getAppName
import com.weimu.universalview.ktx.isConnectWifi
import java.lang.ref.WeakReference
import java.util.*


object SilentUpdate {
    private val activityStack = Stack<WeakReference<Activity?>>()
    private lateinit var strategy: Strategy
    private lateinit var applicationContext: WeakReference<Context>


    //以下数据可配置
    var downLoadTipDialog: DialogTipAction? = null//自定义 下载Dialog -> 流量模式
    var installTipDialog: DialogTipAction? = null//自定义  安装Dialog -> 无线模式,文件已存在
    var intervalDay = 7//间隔弹窗提示时间-默认7天后提醒-仅仅适用于【isUseDefaultHint=true】


    internal fun getCurrentActivity(): Activity? {
        var targetActivity: Activity? = null
        try {
            targetActivity = activityStack.peek().get()
        } catch (e: Exception) {
            //do nothing
        }
        return targetActivity
    }

    internal fun getApplicationContext() = applicationContext.get()!!

    /**
     * 静默更新的初始化
     * @param App的上下文
     */
    fun init(context: Application) {
        applicationContext = WeakReference(context.applicationContext)
        //登记activity
        activityStack.clear()
        context.registerActivityLifecycleCallbacks(object : ActivityLifeListener() {

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                activityStack.add(WeakReference(activity))
            }

            override fun onActivityDestroyed(activity: Activity?) {
                activityStack.remove(WeakReference(activity))
            }
        })
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

        //策略模式
        strategy = when {
            //WIFI
            getApplicationContext().isConnectWifi() -> WifiStrategy.getDefault()
            //流量
            else -> MobileStrategy.getDefault()
        }
        strategy.update(apkUrl, latestVersion)
    }

    /**
     * 主动更新 同流量模式
     * 检查本地文件，没有直接显示下载弹窗
     */
    fun activeUpdate(receive: UpdateInfo.() -> Unit) {
        val updateInfo = UpdateInfo()
        updateInfo.receive()
        val apkUrl = updateInfo.apkUrl
        val latestVersion = updateInfo.latestVersion
        if (apkUrl.isBlank() or latestVersion.isBlank()) return
        SPCenter.modifyUpdateInfo(updateInfo)

        //策略模式
        MobileStrategy.getDefault().update(apkUrl, latestVersion)
    }


    /**
     * 清除sp缓存数据
     */
    fun clearCache() {
        SPCenter.clearDialogTime()
        SPCenter.clearUpdateInfo()
    }

    /**
     * 删除apk 手动删除 已安装的apk
     * @param version 版本
     */
    fun deleteApk(version: String): Boolean {
        val path = "${Const.UPDATE_FILE_DIR}${getApplicationContext().getAppName()}_v$version.apk"
        return FileHelper.deleteFile(path)
    }


}



