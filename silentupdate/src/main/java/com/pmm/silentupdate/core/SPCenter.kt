package com.pmm.silentupdate.core

import android.content.Context
import com.pmm.silentupdate.BuildConfig
import com.pmm.silentupdate.SilentUpdate
import com.weimu.universalview.ktx.toJsonStr
import com.weimu.universalview.ktx.toObject

/**
 * Author:你需要一台永动机
 * Date:2018/4/13 09:44
 * Description:存储文件的时间
 */
internal object SPCenter {

    //Dialog的显示间距
    private val DIALOG_TIME = "dialogTime"

    private val sp by lazy { SilentUpdate.getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE) }

    //获取存储时间
    fun getDialogTime(): Long {
        return sp.getLong(DIALOG_TIME, 0L)
    }

    //修改存储时间
    fun modifyDialogTime(storeTime: Long) {
        sp.edit().putLong(DIALOG_TIME, storeTime).apply()
    }

    //清除存储时间
    fun clearDialogTime() {
        sp.edit().remove(DIALOG_TIME).apply()
    }


    //更新的内容
    private val UPDATE_INFO = "updateInfo"

    //获取更新内容
    fun getUpdateInfo(): UpdateInfo = (sp.getString(UPDATE_INFO, "") ?: "").toObject()

    //修改更新内容
    fun modifyUpdateInfo(updateInfo: UpdateInfo) {
        sp.edit().putString(UPDATE_INFO, updateInfo.toJsonStr()).apply()
    }

    //清除更新内容
    fun clearUpdateInfo() {
        sp.edit().remove(UPDATE_INFO).apply()
    }

}