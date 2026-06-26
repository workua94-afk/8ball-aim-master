package com.shotmaster.pool

import android.content.res.Resources

object ResourcesGetter {
    val densityDpi: Int
        get() = Resources.getSystem().displayMetrics.densityDpi
}
