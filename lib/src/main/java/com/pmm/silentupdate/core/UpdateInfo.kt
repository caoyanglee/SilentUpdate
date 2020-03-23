package com.pmm.silentupdate.core

import android.os.Bundle
import com.pmm.silentupdate.R
import java.io.Serializable

/**
 * Author:你需要一台永动机
 * Date:2019-07-29 11:47
 * Description:更新内容
 */
class UpdateInfo : Serializable {
    var apkUrl: String = ""
    var latestVersion: String = ""
    var title = ContextCenter.getAppContext().getString(R.string.module_silentupdate_update_title)//更新标题
    var msg = ContextCenter.getAppContext().getString(R.string.module_silentupdate_update_msg_default)//更新的内容
    var isForce = false//是否强制
    var extra: Bundle? = null//可以扩展更多参数
}