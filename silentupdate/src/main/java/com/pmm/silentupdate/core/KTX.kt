package com.pmm.silentupdate.core

import android.util.Log
import com.pmm.silentupdate.BuildConfig


//log
internal fun Any.loge(message: String) {
    if (BuildConfig.DEBUG) Log.e("silentUpdate", message)
}

