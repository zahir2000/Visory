package com.taruc.visory.quickblox.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.view.KeyEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.taruc.visory.R
import com.taruc.visory.quickblox.utils.showErrorSnackbar


abstract class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null


    internal fun showProgressDialog(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.setCanceledOnTouchOutside(false)

            // Disable the back button
            progressDialog!!.setOnKeyListener(KeyEventListener())
        }
        progressDialog!!.setMessage(getString(messageId))
        progressDialog!!.show()
    }

    override fun onPause() {
        super.onPause()
        hideProgressDialog()
    }

    internal fun hideProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    protected fun checkPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (checkPermission(permission)) {
                return true
            }
        }
        return false
    }

    protected fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
    }

    inner class KeyEventListener : DialogInterface.OnKeyListener {
        override fun onKey(dialog: DialogInterface?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
            return keyCode == KeyEvent.KEYCODE_BACK
        }
    }
}