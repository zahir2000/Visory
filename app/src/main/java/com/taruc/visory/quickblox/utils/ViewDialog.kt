package com.taruc.visory.quickblox.utils

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.Window
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.taruc.visory.R

class ViewDialog(var context: Context) {

    lateinit var dialog: Dialog

    fun showDialog() {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_loading_layout)

        val gifImageView: ImageView = dialog.findViewById(R.id.custom_loading_image)

        val imageViewTarget = GlideDrawableImageViewTarget(gifImageView)
        Glide
            .with(context)
            .load(R.drawable.earth_day)
            .error(R.drawable.loading)
            .centerCrop()
            .crossFade()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(imageViewTarget)

        dialog.show()
    }

    fun showDialogForXSeconds(time: Long) {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_loading_layout)

        val gifImageView: ImageView = dialog.findViewById(R.id.custom_loading_image)

        val imageViewTarget = GlideDrawableImageViewTarget(gifImageView)
        Glide
            .with(context)
            .load(R.drawable.earth_day)
            .error(R.drawable.loading)
            .centerCrop()
            .crossFade()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(imageViewTarget)

        dialog.show()

        Handler().postDelayed({
            hideDialog()
        }, time)
    }

    fun showDialogFor5Seconds() {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_loading_layout)

        val gifImageView: ImageView = dialog.findViewById(R.id.custom_loading_image)

        val imageViewTarget = GlideDrawableImageViewTarget(gifImageView)
        Glide
            .with(context)
            .load(R.drawable.earth_day)
            .error(R.drawable.loading)
            .centerCrop()
            .crossFade()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(imageViewTarget)

        dialog.show()

        Handler().postDelayed({
            hideDialog()
        }, 5000)
    }

    fun hideDialog(){
        dialog.dismiss()
    }

}