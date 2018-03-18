package www.weimu.io.silentupdate

import android.app.*
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import www.weimu.io.silentupdate.core.*
import www.weimu.io.silentupdate.strategy.MobileStrategy
import www.weimu.io.silentupdate.strategy.Strategy
import www.weimu.io.silentupdate.strategy.WifiStrategy
import java.io.File
import java.util.*


object SilentUpdate {
    private val activityStack = Stack<Activity>()
    private lateinit var strategy: Strategy

    //以下数据可配置
    var updateListener: UpdateListener? = null//更新回调
    var isUseDefaultHint = true//是否使用默认提示 包括Dialog和Notification

    internal fun getCurrentActivity() = activityStack.peek()
    internal fun getApplicationContext() = getCurrentActivity().applicationContext

    //链接至Application
    fun init(context: Application) {
        //策略模式
        strategy = if (context.isConnectWifi()) WifiStrategy(context) else MobileStrategy(context)
        //登记activity
        activityStack.clear()
        context.registerActivityLifecycleCallbacks(object : ActivityLifeListener() {

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
        strategy.update(apkUrl, latestVersion)
    }

    //若不使用默认的Dialog 使用者需要配合自定义Dialog和此方法来 打开apk安装界面
    //打开Apk安装界面
    fun openApkInstallPage(apkFile: File) {
        getApplicationContext().openApkByFilePath(apkFile)
    }

}



