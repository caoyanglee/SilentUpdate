package www.weimu.io.silentupdatedemo

import android.Manifest
import android.app.*
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.ContextThemeWrapper
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import www.weimu.io.silentupdate.UpdateCenter
import www.weimu.io.silentupdate.constructOpenApkItent
import java.io.File
import java.util.*
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.NotificationCompat


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
    }


    //检查权限 step1
    private fun checkPermission() {
        RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted!!)
                        getLatestApk()
                    else
                        showMissingPermissionDialog("没有此权限，APP将无法正常更新APP")
                }
    }

    //获取下载链接 step2
    fun getLatestApk() {
        //具体的网络请求步骤自己操作
        val downloadUrl = "https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk"
//        val downloadUrl="http://wangcaidai.vip//apk//cashloan_android.apk"
        //判断版本号
        val latestVersion = "3.0"
        val currentVersion = BuildConfig.VERSION_NAME
        if (latestVersion > currentVersion)
            UpdateCenter.obtainLatestApk(downloadUrl, latestVersion)
    }

    //类似的程序退出入口
    override fun onDestroy() {
        super.onDestroy()
        AppData.exitApp()
    }
}
