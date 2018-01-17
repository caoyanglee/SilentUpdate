package www.weimu.io.silentupdatedemo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.ContextThemeWrapper


//缺失权限的提示
fun ContextThemeWrapper.showMissingPermissionDialog(permissionTips: String = "新版本已下载完成,请立即安装更新！") {
    AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("提示")
            .setMessage(permissionTips)
            .setPositiveButton("去开启", { dialog, which ->
                this.openAppInfoPage()
            })
            .setNegativeButton("知道了", null)
            .show()
}

//app信息界面 -- 修改权限
fun Context.openAppInfoPage(targetPackageName: String = packageName) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", targetPackageName, null)
    intent.data = uri
    startActivity(intent)
}