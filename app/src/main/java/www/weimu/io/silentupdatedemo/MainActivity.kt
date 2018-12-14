package www.weimu.io.silentupdatedemo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.weimu.universalib.ktx.clearAllCache
import com.weimu.universalib.ktx.toast
import kotlinx.android.synthetic.main.activity_main.*
import www.weimu.io.silentupdate.SilentUpdate


/**
 * kotlin的调用方法
 */
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //kotlin
        btn_kotlin.setOnClickListener {
            startActivity(Intent(this, KotlinDemoActivity::class.java))
        }
        //java
        btn_java.setOnClickListener {
            startActivity(Intent(this, JavaDemoActivity::class.java))
        }
        //clear cache
        btn_clear_cache.setOnClickListener {
            SilentUpdate.clearCache()
            toast("清除缓存成功")
        }
    }


}
