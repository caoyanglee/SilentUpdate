package www.weimu.io.silentupdatedemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import com.pmm.silentupdate.SilentUpdate
import com.pmm.ui.ktx.click
import com.pmm.ui.ktx.toast


/**
 * kotlin的调用方法
 */
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //kotlin
        btnKotlin.setOnClickListener {
            startActivity(Intent(this, KotlinDemoActivity::class.java))
        }
        //java
        btnJava.setOnClickListener {
            startActivity(Intent(this, JavaDemoActivity::class.java))
        }
        //clear cache
        btnClearCache.setOnClickListener {
            SilentUpdate.clearCache()
            toast("清除缓存成功")
        }
        //delete apk
        btnDeleteApk.setOnClickListener {
            if (SilentUpdate.deleteApk(version = "1.1.1"))
                toast("删除成功")
            else
                toast("删除失败")
        }
    }


}
