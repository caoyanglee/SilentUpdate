package www.weimu.io.silentupdatedemo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import www.weimu.io.silentupdate.UpdateCenter

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
    }


    //类似的程序退出入口
    override fun onDestroy() {
        super.onDestroy()
        AppData.exitApp()
    }
}
