package www.weimu.io.silentupdate.core

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import www.weimu.io.silentupdate.BuildConfig
import java.io.File


//直接打开APK
internal fun Context.openApkByFilePath(file: File) {
    startActivity(constructOpenApkItent(file))
}

//构造打开APK的Intent
internal fun Context.constructOpenApkItent(file: File): Intent {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//7.0有效
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)//7.0有效
    }
    val uri = getUriForFile(file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    return intent
}

//通知开关是否打开
internal fun Context.isNotificationEnabled(): Boolean {
    val CHECK_OP_NO_THROW = "checkOpNoThrow"
    val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"
    val mAppOps = this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val appInfo = this.applicationInfo
    val pkg = this.applicationContext.packageName
    val uid = appInfo.uid
    var appOpsClass: Class<*>? = null
    /* Context.APP_OPS_MANAGER */
    try {
        appOpsClass = Class.forName(AppOpsManager::class.java.name)
        val checkOpNoThrowMethod = appOpsClass!!.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                String::class.java)
        val opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION)
        val value = opPostNotificationValue.get(Int::class.java) as Int
        return checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) as Int == AppOpsManager.MODE_ALLOWED
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    }

    return false
}

//app信息界面 -- 修改权限  --修改通知开关
internal fun Context.openAppInfoPage(targetPackageName: String = packageName) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", targetPackageName, null)
    intent.data = uri
    startActivity(intent)
}


//是否存在文件
internal fun Any.isFileExist(filePath: String): Boolean {
    if (TextUtils.isEmpty(filePath)) return false
    val file = File(filePath)
    return file.exists() && file.isFile
}

//log
internal fun Any.loge(message: String) {
    if (BuildConfig.DEBUG)
        Log.e("weimu", message)
}


/**
 * 获取文件的Uri
 * 兼容7.0
 */
internal fun Context.getUriForFile(file: File?): Uri {
    //获取当前app的包名
    val FPAuth = "$packageName.fileprovider"

    if (file == null) throw NullPointerException()

    val uri: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        uri = FileProvider.getUriForFile(this.applicationContext, FPAuth, file)
    } else {
        uri = Uri.fromFile(file)
    }
    return uri
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
fun Context.getAppIcon(): Int {
    val pm: PackageManager = packageManager
    try {
        val info = pm.getApplicationInfo(this.packageName, 0)
        return info.icon
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return -1
}

//是否连接Wifi
internal fun Context.isConnectWifi(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    if (networkInfo != null && networkInfo.isConnected) {
        val type = networkInfo.type
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true
        }
    }
    return false
}

