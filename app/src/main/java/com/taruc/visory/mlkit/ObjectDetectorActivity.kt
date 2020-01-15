package com.taruc.visory.mlkit

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.taruc.visory.R
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.activity_object_detector.*
import java.lang.StringBuilder
import java.util.concurrent.Executors

class ObjectDetectorActivity : AppCompatActivity() {

    lateinit var classifier: Classifier
    private val executor = Executors.newSingleThreadExecutor()
    lateinit var textViewResult: TextView
    lateinit var btnDetectObject: Button
    lateinit var btnToggleCamera: Button
    lateinit var imageViewResult: ImageView
    lateinit var cameraView: CameraView

    companion object {
        private const val MODEL_PATH = "mobilenet_quant_v1_224.tflite"
        private const val LABEL_PATH = "labels.txt"
        private const val INPUT_SIZE = 224
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object_detector)

        cameraView = findViewById(R.id.cameraView)
        imageViewResult = findViewById(R.id.imageViewResult)
        textViewResult = findViewById(R.id.textViewResult)
        textViewResult.movementMethod = ScrollingMovementMethod()

        btnToggleCamera = findViewById(R.id.btnToggleCamera)
        btnDetectObject = findViewById(R.id.btnDetectObject)

        cameraView.layoutParams.apply {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val height = size.y

            btnToggleCamera.measure(0, 0)
            val btnHeight = btnToggleCamera.measuredHeight
            btnDetectObject.measure(0, 0)
            val btn2Height = btnDetectObject.measuredHeight
            textViewObjectDetector.measure(0, 0)
            val txtHeight = textViewObjectDetector.measuredHeight

            (this as LinearLayout.LayoutParams).height = ((height - (txtHeight + (txtHeight / 2)))
                    - (((btnHeight + btn2Height) * 2)
                    + (btnHeight / 2)))
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val resultDialog = Dialog(this)
        val customProgressView = LayoutInflater.from(this).inflate(R.layout.result_dialog_layout, null)
        resultDialog.setCancelable(false)
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        resultDialog.setContentView(customProgressView)

        val imageResult = customProgressView.findViewById<ImageView>(R.id.dialog_image_result)
        val loadingText = customProgressView.findViewById<TextView>(R.id.dialog_text_loading)
        val result = customProgressView.findViewById<TextView>(R.id.dialog_text_result)
        val indicatorLayout = customProgressView.findViewById<View>(R.id.dialog_indicator_layout)
        val closeResultButton = customProgressView.findViewById<Button>(R.id.button_close_dialog)

        closeResultButton.setOnClickListener{
            resultDialog.dismiss()
        }

        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) { }

            override fun onError(cameraKitError: CameraKitError) { }

            override fun onImage(cameraKitImage: CameraKitImage) {

                var bitmap = cameraKitImage.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

                indicatorLayout.visibility = View.GONE
                loadingText.visibility = View.GONE
                closeResultButton.visibility = View.VISIBLE

                val results = classifier.recognizeImage(bitmap)
                imageResult.setImageBitmap(bitmap)

                val builder = StringBuilder()

                if(results.size == 1){
                    builder.append(results[0].title.capitalize() + "\nwith\n" + "%.0f".format(results[0].confidence * 100) + "% reliance")
                }else if(results.size == 2){
                    if(results[0].confidence.compareTo(results[1].confidence) > 0){
                        builder.append("First Possible Object:\n" + results[0].title.capitalize() + " with " + "%.0f".format(results[0].confidence * 100) + "% reliance")
                        builder.append("\n\nSecond Possible Object:\n" + results[1].title.capitalize() + " with " + "%.0f".format(results[1].confidence * 100) + "% reliance")
                    }else{
                        builder.append("First Possible Object:\n" + results[1].title.capitalize() + " with " + "%.0f".format(results[1].confidence * 100) + "% reliance")
                        builder.append("\n\nSecond Possible Object:\n" + results[0].title.capitalize() + " with " + "%.0f".format(results[0].confidence * 100) + "% reliance")
                    }
                }else{
                    builder.append("Sorry, we could not identify this object. Please try again or change your location to area with more light.")
                }

                result.text = builder.toString()

                result.visibility = View.VISIBLE
                imageResult.visibility = View.VISIBLE

                resultDialog.setCancelable(true)

            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) { }
        })

        btnToggleCamera.setOnClickListener { cameraView.toggleFacing() }

        btnDetectObject.setOnClickListener {
            cameraView.captureImage()
            resultDialog.show()
            result.visibility = View.GONE
            imageResult.visibility = View.GONE

        }

        resultDialog.setOnDismissListener {
            loadingText.visibility = View.VISIBLE
            indicatorLayout.visibility = View.VISIBLE
        }

        initTensorFlowAndLoadModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.execute { classifier.close() }
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                classifier = Classifier.create(
                    assets,
                    MODEL_PATH,
                    LABEL_PATH,
                    INPUT_SIZE)
                makeButtonVisible()
            } catch (e: Exception) {}
        }
    }

    private fun makeButtonVisible() {
        runOnUiThread { btnDetectObject.visibility = View.VISIBLE }
    }
}
