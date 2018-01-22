package www.weimu.io.silentupdate.core

import android.os.Environment

/**
 * Author:你需要一台永动机
 * Date:2018/1/22 14:07
 * Description:
 */
internal object Const {
    //更新文件地址  默认指定文件夹【下载】
    var fileDirectory = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_DOWNLOADS + "/"
}