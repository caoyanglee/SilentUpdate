package www.weimu.io.silentupdate.model

import android.content.Context
import com.google.gson.Gson
import www.weimu.io.silentupdate.BuildConfig
import www.weimu.io.silentupdate.UpdateCenter

/**
 * Author:你需要一台永动机
 * Date:2018/1/17 14:08
 * Description:
 */
class UpdateShare(private var mContext: Context) {


    private val SHARE_UPDATE = "update"

    fun clearUpdateShare() {
        val sp = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        sp.edit().remove(SHARE_UPDATE).apply()
    }

    fun setUpdateShare(share: AppUpdatePreB) {
        val sp = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        sp.edit().putString(SHARE_UPDATE, share.toJson()).apply()
    }

    fun getUpdateShare(): AppUpdatePreB {
        val sp = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        val infoString = sp.getString(SHARE_UPDATE, "")
        if (infoString.isEmpty()) {
            val apShare = AppUpdatePreB()
            setUpdateShare(apShare)
            return apShare
        }
        return Gson().fromJson<AppUpdatePreB>(infoString, AppUpdatePreB::class.java)
    }

    //存储某些变量
    fun saveShareStuff(fn: AppUpdatePreB.() -> Unit) {
        val appSharePre = getUpdateShare()
        appSharePre.fn()
        setUpdateShare(appSharePre)
    }
}