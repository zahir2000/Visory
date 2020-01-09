package com.taruc.visory.mlkit

import android.graphics.Bitmap

interface IClassifier {
    data class Recognition(
        var id: String = "",
        var title: String = "",
        var confidence: Float = 0F
    )

    fun recognizeImage(bitmap: Bitmap): List<Recognition>
    fun close()
}