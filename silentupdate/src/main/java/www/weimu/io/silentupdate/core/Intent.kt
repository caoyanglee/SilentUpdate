package www.weimu.io.silentupdate.core

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.io.File


//直接打开APK
fun Context.openApkByFilePath(file: File) {
    startActivity(constructOpenApkItent(file))
}

//构造打开APK的Intent
fun Context.constructOpenApkItent(file: File): Intent {
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.action = android.content.Intent.ACTION_VIEW
    if (Build.VERSION.SDK_INT >= 24) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//7.0有效
    }
    val uri = FileUtils.getUriForFile(this, file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    return intent
}

//通知开关是否打开
fun Context.isNotificationEnabled(): Boolean {
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
fun Context.openAppInfoPage(targetPackageName: String = packageName) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", targetPackageName, null)
    intent.data = uri
    startActivity(intent)
}



