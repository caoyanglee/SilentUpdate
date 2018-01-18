package www.weimu.io.silentupdatedemo

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.app.AppOpsManager
import android.content.pm.ApplicationInfo
import www.weimu.io.silentupdate.UpdateCenter
import www.weimu.io.silentupdate.isNotificationEnabled
import www.weimu.io.silentupdate.openAppInfoPage
import android.app.NotificationChannel
import android.os.Build
import android.support.annotation.RequiresApi


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


    /**
     * 点击返回键
     */
    override fun onBackPressed() {
        super.onBackPressed()
        UpdateCenter.detach()
    }


}
