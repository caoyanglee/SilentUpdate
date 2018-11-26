package www.weimu.io.silentupdate

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import www.weimu.io.silentupdate.core.*
import www.weimu.io.silentupdate.strategy.MobileStrategy
import www.weimu.io.silentupdate.strategy.Strategy
import www.weimu.io.silentupdate.strategy.WifiStrategy
import java.io.File
import java.lang.ref.WeakReference
import java.util.*


object SilentUpdate {
    private val activityStack = Stack<Activity>()
    private lateinit var strategy: Strategy
    private lateinit var applicationContext: WeakReference<Context>

    //以下数据可配置
    var updateListener: UpdateListener? = null//更新回调
    var isUseDefaultHint = true//是否使用默认提示 包括Dialog和Notification
    var intervalDay = 7//间隔弹窗提示时间-默认7天后提醒-仅仅适用于【isUseDefaultHint=true】

    internal fun getCurrentActivity(): Activity? {
        var targetActivity: Activity? = null
        try {
            targetActivity = activityStack.peek()
        } catch (e: Exception) {
            //
        }
        return targetActivity
    }

    internal fun getApplicationContext() = applicationContext.get()!!

    //链接至Application
    fun init(context: Application) {
        applicationContext = WeakReference(context.applicationContext)
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
        //策略模式
        val context = getApplicationContext()

        strategy = when {
            context.isConnectWifi() -> WifiStrategy.getDefault()
            else -> MobileStrategy.getDefault()
        }
        strategy.update(apkUrl, latestVersion)
    }

    //若不使用默认的Dialog 使用者需要配合自定义Dialog和此方法来 打开apk安装界面
    //打开Apk安装界面
    fun installApk(apkFile: File) {
        getApplicationContext().openApkByFilePath(apkFile)
    }

}



