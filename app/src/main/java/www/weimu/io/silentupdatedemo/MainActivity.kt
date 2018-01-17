package www.weimu.io.silentupdatedemo

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import www.weimu.io.silentupdate.UpdateCenter


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
