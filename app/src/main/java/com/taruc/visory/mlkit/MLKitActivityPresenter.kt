package com.taruc.visory.mlkit

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition

class MLKitActivityPresenter(val view: View) {
    fun runTextRecognition(selectedImage: Bitmap) {
        view.showImageTextView()
        view.showProgress()

        val recognizer = TextRecognition.getClient()
        val image = InputImage.fromBitmap(selectedImage, 0)
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResults(texts)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun runImageLabeling(selectedImage: Bitmap) {
        view.showImageTextView()
        view.showProgress()

        val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(selectedImage, 0)
        imageLabeler.process(image)
            .addOnSuccessListener {
                processImageLabelingResults(it)
        }
            .addOnFailureListener{
                it.printStackTrace()
            }
    }

    private fun processTextRecognitionResults(texts: Text) {
        view.hideProgress()
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            view.showImageTextView()
            view.showNoTextMessage()
            return
        }
        view.showText(texts.text)
    }

    private fun processImageLabelingResults(texts: MutableList<ImageLabel>) {
        view.hideProgress()
        var detectedText = ""
        for (label in texts) {
            val text = label.text
            val confidence = (label.confidence * 100).toInt()
            val index = label.index
            detectedText += "$text with $confidence%\n"
        }

        if (detectedText == ""){
            view.showImageTextView()
            view.showNoTextMessage()
            return
        }

        view.showText(detectedText.trim())
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