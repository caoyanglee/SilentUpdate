package www.weimu.io.silentupdate

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * @project KotLinProject
 * @author 艹羊
 * @date 2017/6/12 上午10:32
 * @description
 */
abstract class WMActivityLifeCycleCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityStarted(activity: Activity?) {}

    override fun onActivityResumed(activity: Activity?) {}

    override fun onActivityPaused(activity: Activity?) {}

    override fun onActivityStopped(activity: Activity?) {}

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}


    override fun onActivityDestroyed(activity: Activity?) {}
}