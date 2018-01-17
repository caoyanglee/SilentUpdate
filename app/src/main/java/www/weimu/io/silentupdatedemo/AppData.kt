package www.weimu.io.silentupdatedemo

import android.app.Application
import android.util.Log
import android.widget.Toast
import www.weimu.io.silentupdate.DownloadListener
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
        UpdateCenter.isShowDialog = true
        UpdateCenter.isShowNotification=true
        UpdateCenter.downloadListener = object : DownloadListener {

            override fun onDownLoadSuccess(file: File) {
                Log.e("weimu", "下载完成")
            }

            override fun onFileIsExist(file: File) {
                Log.e("weimu", "文件已存在")
            }

        }
    }

    companion object {

        fun exitApp() {
            //退出App的处理 step02
            UpdateCenter.detach()
        }

    }

}