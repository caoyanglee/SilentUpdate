package www.weimu.io.silentupdatedemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

//app信息界面 -- 修改权限
fun Context.openAppInfoPage(targetPackageName: String = packageName) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", targetPackageName, null)
    intent.data = uri
    startActivity(intent)
}