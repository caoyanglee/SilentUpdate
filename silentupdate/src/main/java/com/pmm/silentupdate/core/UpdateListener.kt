package com.pmm.silentupdate.core

import java.io.File

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 16:00
 * Description:
 */
interface UpdateListener {
    //下载完成
    fun onDownLoadSuccess(file: File, updateContent: String)

    //文件已存在
    fun onFileIsExist(file: File, updateContent: String)
}