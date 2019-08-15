package com.pmm.silentupdate.strategy

import java.io.File

/**
 * Author:你需要一台永动机
 * Date:2018/7/17 18:17
 * Description:
 */
internal interface StrategyAction {

    //检查更新的URL
    fun checkUpdateUrl(url: String)

    //更新
    fun update(apkUrl: String, latestVersion: String)

    //下载完成后
    fun afterDownLoadComplete(file: File)


}