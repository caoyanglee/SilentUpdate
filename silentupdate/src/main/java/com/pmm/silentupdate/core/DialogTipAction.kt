package com.pmm.silentupdate.core

import android.content.Context
import java.io.File

/**
 * Author:你需要一台永动机
 * Date:2018/12/14 14:26
 * Description:
 */
interface DialogTipAction {

    fun show(context: Context,
             updateInfo: UpdateInfo,
             positiveClick: (() -> Unit),
             negativeClick: (() -> Unit))
}
