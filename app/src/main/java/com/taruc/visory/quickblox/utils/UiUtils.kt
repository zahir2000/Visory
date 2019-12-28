package com.taruc.visory.quickblox.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.annotation.*
import androidx.annotation.IntRange
import com.taruc.visory.R
import com.taruc.visory.quickblox.App
import java.util.*

fun getColoredCircleDrawable(): Drawable {
    val rand = Random()
    val randColor = Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))
    val drawable = getDrawable(R.drawable.shape_circle) as GradientDrawable
    drawable.setColor(randColor)
    return drawable
}

fun getString(@StringRes stringId: Int): String {
    return App.getInstance().getString(stringId)
}

fun getDrawable(@DrawableRes drawableId: Int): Drawable {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        App.getInstance().resources.getDrawable(drawableId)
    } else {
        App.getInstance().resources.getDrawable(drawableId, null)
    }
}