package com.pmm.silentupdate.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.pmm.silentupdate.BuildConfig
import com.weimu.universalib.ktx.getUri4File
import java.io.File
import java.util.*


//直接打开APK
internal fun Context.openApkByFilePath(file: File) {
    //防止有的系统 强制关闭安装未知来源的app 导致的crash
    try {
        startActivity(constructOpenApkItent(file))
    } catch (e: Exception) {
        e.printStackTrace()
        //doNothing
    }

}

//构造打开APK的Intent
internal fun Context.constructOpenApkItent(file: File): Intent {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//7.0有效
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)//7.0有效
    }
    val uri = getUri4File(file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    return intent
}


//是否存在文件
internal fun Any.isFileExist(filePath: String): Boolean {
    if (TextUtils.isEmpty(filePath)) return false
    val file = File(filePath)
    return file.exists() && file.isFile
}

//log
internal fun Any.loge(message: String) {
    if (BuildConfig.DEBUG) Log.e("silentUpdate", message)
}

//获取应用的名字
internal fun Context.getAppName(): String? {
    val pm: PackageManager = packageManager
    try {
        val info = pm.getApplicationInfo(this.packageName, 0)
        return info.loadLabel(pm).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return "updateApk"
}

//获取app的图片
internal fun Context.getAppIcon(): Int {
    val pm: PackageManager = packageManager
    try {
        val info = pm.getApplicationInfo(this.packageName, 0)
        return info.icon
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return -1
}


//比价时间
internal fun Long.moreThanDays(day: Int): Boolean {
    val currentTime = Calendar.getInstance().time.time
    val recordTime = this
    if (recordTime == 0L) return true
    val differ = currentTime - recordTime
    if (differ > 1000 * 60 * 60 * 24 * day) {
        return true
    }
    return false
}

