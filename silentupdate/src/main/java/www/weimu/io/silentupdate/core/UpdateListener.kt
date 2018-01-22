package www.weimu.io.silentupdate.core

import java.io.File

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 16:00
 * Description:
 */
interface UpdateListener {
    //下载完成
    fun onDownLoadSuccess(file: File)

    //文件已存在
    fun onFileIsExist(file: File)
}