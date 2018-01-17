package www.weimu.io.silentupdate

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.io.File


private val SCHEME = "package"

//进入拨号键面
fun Context.launchDialogPage(phoneNumber: String) {
    //用intent启动拨打电话
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber))
    startActivity(intent)
}

//直接拨打电话
fun Context.initiateCall(phoneNumber: String) {
    //用intent启动拨打电话
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("call:" + phoneNumber))
    startActivity(intent)
}


//app信息界面 -- 修改权限
fun Context.openAppInfoPage(targetPackageName: String = packageName) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts(SCHEME, targetPackageName, null)
    intent.data = uri
    startActivity(intent)
}

//系统app列表
fun Context.openAppList() {
    val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
    startActivity(intent)
}

//系统浏览器-打开网页
fun Context.openWebSite(url: String) {
    val intent = Intent()
    intent.action = "android.intent.action.VIEW"
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val content_url = Uri.parse(url)
    intent.data = content_url
    startActivity(intent)
}

//打开微信
fun Context.openWeiXin(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_MAIN)
        val cmp = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.component = cmp
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "检查到您手机没有安装该APP，请安装后使用该功能", Toast.LENGTH_SHORT).show()
    }

}

//打开相对应包名的app
fun Context.openAPP(appPackageName: String, context: Context) {
    try {
        val intent = packageManager.getLaunchIntentForPackage(appPackageName)
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "检查到您手机没有安装该APP，请安装后使用该功能", Toast.LENGTH_SHORT).show()
    }

}

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



