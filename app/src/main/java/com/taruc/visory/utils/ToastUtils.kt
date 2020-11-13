package com.taruc.visory.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.taruc.visory.R
import com.taruc.visory.quickblox.App
import com.taruc.visory.ui.LoginActivity

@IntDef(Toast.LENGTH_LONG, Toast.LENGTH_SHORT)
private annotation class ToastLength

fun shortToast(@StringRes text: Int) {
    shortToast(App.getInstance().getString(text))
}

fun shortToast(text: String) {
    show(text, Toast.LENGTH_SHORT)
}

fun longToast(@StringRes text: Int) {
    longToast(App.getInstance().getString(text))
}

fun longToast(text: String) {
    show(text, Toast.LENGTH_LONG)
}

private fun makeToast(text: String, @ToastLength length: Int): Toast {
    return Toast.makeText(App.getInstance(), text, length)
}

private fun show(text: String, @ToastLength length: Int) {
    makeToast(text, length).show()
}

fun makeDefaultSnackbar(view: View, text: String){
    val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null)
    snackbar.setActionTextColor(Color.WHITE)
    val snackbarView = snackbar.view
    snackbarView.setBackgroundResource(R.color.colorPrimary)
    snackbar.show()
}

fun makeErrorSnackbar(view: View, text: String){
    val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null)
    snackbar.setActionTextColor(Color.WHITE)
    val snackbarView = snackbar.view
    snackbarView.setBackgroundColor(Color.rgb(182, 15, 10))
    snackbar.show()
}

fun makeSuccessSnackbar(view: View, text: String){
    val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null)
    snackbar.setActionTextColor(Color.WHITE)
    val snackbarView = snackbar.view
    snackbarView.setBackgroundColor(Color.rgb(46, 139, 87))
    snackbar.show()
}

fun makeWarningSnackbar(view: View, text: String){
    val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null)
    snackbar.setActionTextColor(Color.BLACK)
    val snackbarView = snackbar.view
    snackbarView.setBackgroundColor(Color.rgb(248, 222, 126))
    val textSnack = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textSnack.setTextColor(Color.BLACK)
    snackbar.show()
}