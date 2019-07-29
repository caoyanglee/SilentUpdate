package com.pmm.silentupdate.core

import android.os.Bundle
import com.weimu.universalview.core.BaseB
import java.io.Serializable

/**
 * Author:你需要一台永动机
 * Date:2019-07-29 11:47
 * Description:更新内容
 */
class UpdateInfo : BaseB() {
    var apkUrl: String = ""
    var latestVersion: String = ""
    var title = "提示"//更新标题
    var msg = "发现新版本！请点击立即安装"//更新的内容
    var isForce = false//是否强制
    var extra: Bundle? = null//可以扩展更多参数
}