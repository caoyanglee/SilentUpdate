package www.weimu.io.silentupdatedemo

import android.app.Application
import android.util.Log
import www.weimu.io.silentupdate.SilentUpdate
import www.weimu.io.silentupdate.core.UpdateListener
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
        //是否显示 用户默认弹窗
        SilentUpdate.isUseDefaultHint = true
        ///间隔弹窗提示时间-默认7天后提醒-仅仅适用于【isUseDefaultHint=true】
        SilentUpdate.intervalDay = 1
        //设置回调
        SilentUpdate.updateListener = object : UpdateListener {

            override fun onDownLoadSuccess(file: File) {
                Log.e("weimu", "updateListener 下载完成")
                //SilentUpdate.installApk(file)//当取消默认的dialog时
            }

            override fun onFileIsExist(file: File) {
                Log.e("weimu", "updateListener 文件已存在")
                //SilentUpdate.installApk(file)//当取消默认的dialog时
            }

        }
    }


}
