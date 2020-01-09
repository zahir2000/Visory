package com.taruc.visory.quickblox.utils

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.taruc.visory.quickblox.App

fun showErrorSnackbar(view: View, @StringRes errorMessage: Int, error: String,
                      @StringRes actionLabel: Int, clickListener: View.OnClickListener): Snackbar {
    val errorMessageString = App.getInstance().getString(errorMessage)
    val message = String.format("%s: %s", errorMessageString, error)
    return showErrorSnackbar(view, message, actionLabel, clickListener)
}

fun showErrorSnackbar(view: View, message: String, @StringRes actionLabel: Int,
                      clickListener: View.OnClickListener?): Snackbar {
    val snackbar = Snackbar.make(view, message.trim { it <= ' ' }, Snackbar.LENGTH_INDEFINITE)
    if (clickListener != null) {
        snackbar.setAction(actionLabel, clickListener)
    }
    snackbar.show()
    return snackbar
}