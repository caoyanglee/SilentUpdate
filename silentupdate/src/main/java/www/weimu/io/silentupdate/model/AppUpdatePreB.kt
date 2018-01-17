package www.weimu.io.silentupdate.model

import com.google.gson.Gson
import java.io.Serializable

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 14:08
 * Description:下载专用Bean
 */
class AppUpdatePreB : Serializable {

    fun toJson(): String {
        return Gson().toJson(this)
    }

    //应用版本
    var apkTaskID: Long? = -1L//对应downloadManager
    var isDownloading = false//是否正在下载
}