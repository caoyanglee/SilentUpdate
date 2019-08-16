package com.pmm.silentupdate

import android.app.Application
import com.pmm.silentupdate.core.*
import com.pmm.silentupdate.core.Const
import com.pmm.silentupdate.core.SPCenter
import com.pmm.silentupdate.strategy.MobileUpdateStrategy
import com.pmm.silentupdate.strategy.UpdateStrategy
import com.pmm.silentupdate.strategy.WifiUpdateStrategy
import com.weimu.universalview.helper.FileHelper
import com.weimu.universalview.ktx.getAppName
import com.weimu.universalview.ktx.isConnectWifi


object SilentUpdate {

    //以下数据可配置
    var downLoadTipDialog: DialogTipAction? = null//自定义 下载Dialog -> 流量模式
    var installTipDialog: DialogTipAction? = null//自定义  安装Dialog -> 无线模式,文件已存在
    var intervalDay = 7//间隔弹窗提示时间-默认7天后提醒-仅仅适用于【isUseDefaultHint=true】

    /**
     * 静默更新的初始化
     * @param App的上下文
     */
    fun init(context: Application) {
        ContextCenter.init(context)
    }


    /**
     * 更新操作
     * - 流量模式
     * - WIFI模式
     * @param apkUrl app的下载地址
     * @param latestVersion 最新的版本号
     */
    fun update(receive: UpdateInfo.() -> Unit) {
        val updateInfo = UpdateInfo()
        updateInfo.receive()
        val apkUrl = updateInfo.apkUrl
        val latestVersion = updateInfo.latestVersion
        if (apkUrl.isBlank() or latestVersion.isBlank()) return
        SPCenter.modifyUpdateInfo(updateInfo)

        val context = ContextCenter.getAppContext()

        //策略模式
        val strategy: UpdateStrategy = when {
            //WIFI
            context.isConnectWifi() -> WifiUpdateStrategy.getDefault()
            //流量
            else -> MobileUpdateStrategy.getDefault()
        }
        strategy.update(apkUrl, latestVersion)
    }

    /**
     * 主动更新 同流量模式
     * 检查本地文件，有直接显示下载弹窗
     */
    fun activeUpdate(receive: UpdateInfo.() -> Unit) {
        val updateInfo = UpdateInfo()
        updateInfo.receive()
        val apkUrl = updateInfo.apkUrl
        val latestVersion = updateInfo.latestVersion
        if (apkUrl.isBlank() or latestVersion.isBlank()) return
        SPCenter.modifyUpdateInfo(updateInfo)

        //策略模式
        MobileUpdateStrategy.getDefault().update(apkUrl, latestVersion)
    }


    /**
     * 清除sp缓存数据
     */
    fun clearCache() {
        SPCenter.clearDownloadTaskId()
        SPCenter.clearDialogTime()
        SPCenter.clearUpdateInfo()
    }

    /**
     * 删除apk 手动删除 已安装的apk
     * @param version 版本
     */
    fun deleteApk(version: String): Boolean {
        val context = ContextCenter.getAppContext()
        val path = "${Const.UPDATE_FILE_DIR}${context.getAppName()}_v$version.apk"
        return FileHelper.deleteFile(path)
    }


}



