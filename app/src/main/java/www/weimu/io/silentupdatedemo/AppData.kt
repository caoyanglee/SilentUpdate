package www.weimu.io.silentupdatedemo

import android.app.Application
import android.util.Log
import www.weimu.io.silentupdate.core.UpdateListener
import www.weimu.io.silentupdate.SilentUpdate
import www.weimu.io.silentupdate.core.loge
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
        SilentUpdate.init(this)
        //设置回调
//        SilentUpdate.updateListener = object : UpdateListener {
//
//            override fun onDownLoadSuccess(file: File) {
//                loge("")
//                Log.e("weimu", "下载完成")
//            }
//
//            override fun onFileIsExist(file: File) {
//                Log.e("su", "文件已存在")
//            }
//
//        }
    }


}
