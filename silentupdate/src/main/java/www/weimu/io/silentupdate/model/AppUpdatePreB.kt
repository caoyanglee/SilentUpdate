package www.weimu.io.silentupdate.model

import android.content.Context
import www.weimu.io.silentupdate.BuildConfig

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 14:08
 * Description:下载专用Bean
 */
//对应downloadManager
//是否正在下载
data class AppUpdatePreB(var apkTaskID: Long, var isDownloading: Boolean)


private val SHARE_DOWNLOAD_ID = "download_id"
private val SHARE_IS_DOWNLOADING = "is_downloading"

fun Context.clearUpdateShare() {
    val sp = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    sp.edit()
            .remove(SHARE_DOWNLOAD_ID)
            .remove(SHARE_IS_DOWNLOADING)
            .apply()
}

fun Context.setUpdateShare(share: AppUpdatePreB) {
    val sp = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    sp.edit()
            .putLong(SHARE_DOWNLOAD_ID, share.apkTaskID)
            .putBoolean(SHARE_IS_DOWNLOADING, share.isDownloading)
            .apply()
}

fun Context.getUpdateShare(): AppUpdatePreB {
    val sp = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    val apkTaskID = sp.getLong(SHARE_DOWNLOAD_ID, -1L)
    val isDownloading = sp.getBoolean(SHARE_IS_DOWNLOADING, false)

    val apShare = AppUpdatePreB(apkTaskID, isDownloading)
    return apShare
}

//存储某些变量
fun Context.saveShareStuff(fn: AppUpdatePreB.() -> Unit) {
    val appSharePre = getUpdateShare()
    appSharePre.fn()
    setUpdateShare(appSharePre)
}