package www.weimu.io.silentupdate

import android.content.Context
import android.content.Intent
import android.os.Build
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



