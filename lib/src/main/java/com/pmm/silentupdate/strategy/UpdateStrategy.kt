package com.pmm.silentupdate.strategy

import java.io.File

/**
 * Author:你需要一台永动机
 * Date:2018/7/17 18:17
 * Description:
 */
internal interface UpdateStrategy {

    //更新
    fun update(apkUrl: String, latestVersion: String)

}