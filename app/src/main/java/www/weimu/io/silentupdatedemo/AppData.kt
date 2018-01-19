package www.weimu.io.silentupdatedemo

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import www.weimu.io.silentupdate.core.DownloadListener
import www.weimu.io.silentupdate.UpdateCenter
import java.io.File

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 13:59
 * Description:
 */
class AppData : Application() {

    override fun onCreate() {
        super.onCreate()
        //初始化 step01
        UpdateCenter.attach(this)
        //自定义操作
        UpdateCenter.isShowDialog = true
        UpdateCenter.isShowNotification = true
        //设置回调
        UpdateCenter.downloadListener = object : DownloadListener {

            override fun onDownLoadSuccess(file: File) {
                Log.e("su", "下载完成")
            }

            override fun onFileIsExist(file: File) {
                Log.e("su", "文件已存在")
            }

        }
    }


}

//吐司通知
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}