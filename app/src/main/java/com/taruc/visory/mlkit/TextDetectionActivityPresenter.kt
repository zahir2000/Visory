package com.taruc.visory.mlkit

import android.graphics.Bitmap
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

class TextDetectionActivityPresenter(val view: View) {
    fun runTextRecognition(selectedImage: Bitmap) {
        view.showImageTextView()
        view.showProgress()
        val image = FirebaseVisionImage.fromBitmap(selectedImage)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        detector.processImage(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                e.printStackTrace()
            }
    }

    private fun processTextRecognitionResult(texts: FirebaseVisionText) {
        view.hideProgress()
        //view.hideImageTextView()
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            view.showImageTextView()
            view.showNoTextMessage()
            return
        }
        view.showText(texts.text)
    }

    interface View {
        fun showNoTextMessage()
        fun showText(text: String)
        fun showProgress()
        fun hideProgress()
        fun hideImageTextView()
        fun showImageTextView()
    }
}