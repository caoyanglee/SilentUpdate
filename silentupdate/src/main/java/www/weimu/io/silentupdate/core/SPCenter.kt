package www.weimu.io.silentupdate.core

import android.content.Context
import www.weimu.io.silentupdate.BuildConfig
import www.weimu.io.silentupdate.SilentUpdate

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
    private val UPDATE_CONTENT = "updateContent"


    //获取存储时间
    fun getUpdateContent(): String {
        return sp.getString(UPDATE_CONTENT, "")
    }

    //修改存储时间
    fun modifyUpdateContent(content: String) {
        sp.edit().putString(UPDATE_CONTENT, content).apply()
    }

    //清除存储时间
    fun clearUpdateContent() {
        sp.edit().remove(UPDATE_CONTENT).apply()
    }

}