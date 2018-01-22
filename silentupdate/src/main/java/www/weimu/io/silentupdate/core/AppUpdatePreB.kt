package www.weimu.io.silentupdate.core

import android.content.Context
import www.weimu.io.silentupdate.BuildConfig

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 14:08
 * Description:下载专用Bean
 */
//对应downloadManager
//是否正在下载
data class AppUpdatePreB(var apkTaskID: Long)


private val SHARE_DOWNLOAD_ID = "download_id"

fun Context.clearUpdateShare() {
    val sp = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    sp.edit().remove(SHARE_DOWNLOAD_ID).apply()
}

fun Context.setUpdateShare(share: AppUpdatePreB) {
    val sp = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    sp.edit().putLong(SHARE_DOWNLOAD_ID, share.apkTaskID).apply()
}

fun Context.getUpdateShare(): AppUpdatePreB {
    val sp = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    val apkTaskID = sp.getLong(SHARE_DOWNLOAD_ID, -1L)

    val apShare = AppUpdatePreB(apkTaskID)
    return apShare
}

//存储某些变量
fun Context.saveShareStuff(fn: AppUpdatePreB.() -> Unit) {
    val appSharePre = getUpdateShare()
    appSharePre.fn()
    setUpdateShare(appSharePre)
}