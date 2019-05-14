package com.pmm.silentupdate.core

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Activity声明周期监听者
 */
internal abstract class ActivityLifeListener : Application.ActivityLifecycleCallbacks {

    override fun onActivityStarted(activity: Activity?) {}

    override fun onActivityResumed(activity: Activity?) {}

    override fun onActivityPaused(activity: Activity?) {}

    override fun onActivityStopped(activity: Activity?) {}

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity?) {}
}