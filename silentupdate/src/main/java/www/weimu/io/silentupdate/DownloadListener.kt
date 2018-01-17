package www.weimu.io.silentupdate

import java.io.File

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 16:00
 * Description:
 */
interface DownloadListener {
    fun onDownLoadSuccess(file: File)
    fun onFileIsExist(file: File)
}