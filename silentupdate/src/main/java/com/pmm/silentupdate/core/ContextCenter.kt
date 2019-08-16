package com.pmm.silentupdate.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.lang.ref.WeakReference
import java.util.*

/**
 * Author:你需要一台永动机
 * Date:2019-08-15 18:16
 * Description:
 */
object ContextCenter {
    private lateinit var applicationContext: WeakReference<Context>
    private val activityStack = Stack<WeakReference<Activity?>>()

    internal fun getTopActivity(): Activity? {
        var targetActivity: Activity? = null
        try {
            targetActivity = activityStack.peek().get()
        } catch (e: Exception) {
            //do nothing
        }
        return targetActivity
    }

    internal fun getAppContext() = applicationContext.get()!!


    /**
     * 静默更新的初始化
     * @param App的上下文
     */
    internal fun init(context: Application) {
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
}