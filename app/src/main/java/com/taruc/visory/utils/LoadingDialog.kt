package com.taruc.visory.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import com.taruc.visory.R

object LoadingDialog {
    fun showLoadingDialog(context: Context): Dialog {
        val progressDialog = Dialog(context)

        progressDialog.let {
            it.show()
            it.window?.requestFeature(Window.FEATURE_NO_TITLE)
            it.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            it.setContentView(R.layout.progress_dialog)
            it.setCancelable(false)
            it.setCanceledOnTouchOutside(false)
            return it
        }
    }
}