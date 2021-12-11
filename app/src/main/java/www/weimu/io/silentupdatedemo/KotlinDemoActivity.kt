package www.weimu.io.silentupdatedemo

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pmm.silentupdate.SilentUpdate
import com.pmm.ui.ktx.requestPermission
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.Serializable

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
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, allGrantedCallback = {
            getLatestApk()
        })
    }

    class CheckVersionResultPO(
        val apkUrl: String,
        val latestVersion: String
    ) : Serializable

    //获取下载链接 step2
    private fun getLatestApk() {
        //具体的网络请求步骤自己操作
        MainScope().launch {
            val to = CheckVersionResultPO(
                apkUrl = "http://file.market.xiaomi.com/download/AppStore/09867a8623a9f4d3f954993da6e33b06f74a7a51b/com.pmm.remember_0.7.3_patch8.apk",
                latestVersion = "1.1.2"
            )

            //判断版本号
            if (to.latestVersion > BuildConfig.VERSION_NAME) {
                Toast.makeText(this@KotlinDemoActivity, "开始下载中...", Toast.LENGTH_SHORT).show()

                SilentUpdate.update {
                    this.apkUrl = to.apkUrl
                    this.latestVersion = to.latestVersion
                    this.msg = "1.bug修复"
                    this.isForce = false
                    this.extra = hashMapOf()
                }
            }
        }
    }


}
