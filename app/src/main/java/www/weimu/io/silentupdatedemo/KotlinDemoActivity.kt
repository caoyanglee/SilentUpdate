package www.weimu.io.silentupdatedemo

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import www.weimu.io.silentupdate.UpdateCenter

/**
 * kotlin的调用方式
 */
class KotlinDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_demo)
        checkPermission()
    }

    //检查权限 step1
    private fun checkPermission() {
        RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted!!)
                        getLatestApk()

                }
    }

    //获取下载链接 step2
    fun getLatestApk() {
        //具体的网络请求步骤自己操作
        val apkUrl = "https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk"
        //判断版本号
        val latestVersion = "1.1.0"
        val currentVersion = BuildConfig.VERSION_NAME

        //将服务器传给你的最新版本号字段给latestVersion
        if (latestVersion > currentVersion)
            Toast.makeText(this@KotlinDemoActivity, "开始下载中...", Toast.LENGTH_SHORT).show()
        UpdateCenter.update(apkUrl, latestVersion)
    }


}
