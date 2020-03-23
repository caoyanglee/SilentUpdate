package com.pmm.silentupdate.core

import android.content.Context
import android.content.ContextWrapper
import java.io.File

/**
 * Author:你需要一台永动机
 * Date:2018/12/14 14:26
 * Description:
 */
interface DialogShowAction {

    fun show(context: ContextWrapper,
             updateInfo: UpdateInfo,
             positiveClick: (() -> Unit),
             negativeClick: (() -> Unit))
}
