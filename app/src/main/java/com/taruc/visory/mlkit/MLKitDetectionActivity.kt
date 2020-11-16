package com.taruc.visory.mlkit

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.taruc.visory.R
import com.taruc.visory.quickblox.utils.MLKIT_IMAGE_LABELING
import com.taruc.visory.quickblox.utils.MLKIT_TEXT_DETECTION
import kotlinx.android.synthetic.main.activity_mlkit_detection.*
import java.io.IOException

private const val REQUEST_IMAGE_CAPTURE = 1001
private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
private const val MY_PERMISSIONS_REQUEST_CAMERA = 2
private const val CAMERA_REQUEST_CODE: Int = 143

class MLKitDetectionActivity : AppCompatActivity(), MLKitActivityPresenter.View {
    private lateinit var presenter: MLKitActivityPresenter
    private var imageUri: Uri? = null
    private var preview: ImageView? = null
    private lateinit var b: Dialog
    private lateinit var cameraBtn: TextView
    private lateinit var galleryBtn: TextView
    private var mlkitType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mlkit_detection)

        mlkitType = intent.getStringExtra("type")
        updateUI()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.presenter = MLKitActivityPresenter(this)
        preview = findViewById(R.id.imageView)
        setUpNewImageListener()
    }

    private fun updateUI(){
        when(mlkitType) {
            MLKIT_IMAGE_LABELING -> {
                title = "Image Recognition"
                button_mlkit_detection.text = "Recognize Object"
            }

            MLKIT_TEXT_DETECTION -> {
                title = "Text Detection"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun showNoTextMessage() {
        Toast.makeText(this, "No text detected", Toast.LENGTH_LONG).show()
        //dateTextView.text = "No text has been detected"
    }

    override fun showText(text: String) {
        text_detection_scroll_view.visibility = View.VISIBLE

        dateTextView.text = text
        dateTextView.contentDescription = "Detected text is $text"
        Handler().postDelayed({
            dateTextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }, 100)
    }

    override fun showProgress() {
        text_detection_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        text_detection_progress_bar.visibility = View.GONE
    }

    override fun hideImageTextView() {
        imageView.visibility = View.GONE
    }

    override fun showImageTextView() {
        imageView.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAMERA_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                dateTextView.text = ""
                data?.data?.let {
                    val selectedImageBitmap = resizeImage(it)
                    imageView.setImageBitmap(selectedImageBitmap)
                    runDetection(selectedImageBitmap!!)
                }
            }
            REQUEST_IMAGE_CAPTURE -> if (resultCode == Activity.RESULT_OK) {
                if (imageUri == null) {
                    Toast.makeText(applicationContext, "Image URI is empty!", Toast.LENGTH_LONG)
                        .show()
                    return
                }

                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                val height = size.y
                val lp = imageView.layoutParams as ConstraintLayout.LayoutParams
                imageView.layoutParams.height =
                    height - button_mlkit_detection.height - text_detection_scroll_view.height - lp.topMargin

                val bitmap = getBitmapFromContentUri(this.contentResolver, imageUri)

                val maxSize = imageView.height
                val outWidth: Int
                val outHeight: Int
                val inWidth: Int = bitmap!!.width
                val inHeight: Int = bitmap.height
                if (inWidth > inHeight) {
                    outWidth = maxSize
                    outHeight = inHeight * maxSize / inWidth
                } else {
                    outHeight = maxSize
                    outWidth = inWidth * maxSize / inHeight
                }
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false)
                preview!!.setImageBitmap(resizedBitmap)
                runDetection(bitmap)
            }
        }
    }

    private fun runDetection(bitmap: Bitmap){
        when(mlkitType) {
            MLKIT_IMAGE_LABELING -> {
                presenter.runImageLabeling(bitmap)
            }

            MLKIT_TEXT_DETECTION -> {
                presenter.runTextRecognition(bitmap)
            }
        }
    }

    private fun showDetectionMenu(){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        dialogBuilder.setTitle("Choose Image From")
        val rv = inflater.inflate(R.layout.text_detection_dialog, null, false)
        dialogBuilder.setView(rv)


        cameraBtn = rv.findViewById(R.id.button_text_detection_camera)
        galleryBtn = rv.findViewById(R.id.button_text_detection_gallery)
        cameraBtn.setOnClickListener{
            requestRead()
            b.dismiss()
        }
        galleryBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
            b.dismiss()
        }

        b = dialogBuilder.create()
        b.show()
    }

    @Throws(IOException::class)
    fun getBitmapFromContentUri(contentResolver: ContentResolver?, imageUri: Uri?): Bitmap? {
        val decodedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            ?: return null
        val orientation: Int? = contentResolver?.let { getExifOrientationTag(it, imageUri!!) }
        var rotationDegrees = 0
        var flipX = false
        var flipY = false
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipX = true
            ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                rotationDegrees = 90
                flipX = true
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees = 180
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipY = true
            ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees = -90
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                rotationDegrees = -90
                flipX = true
            }
            ExifInterface.ORIENTATION_UNDEFINED, ExifInterface.ORIENTATION_NORMAL -> {
            }
            else -> {
            }
        }
        return rotateBitmap(decodedBitmap, rotationDegrees, flipX, flipY)
    }

    private fun getExifOrientationTag(resolver: ContentResolver, imageUri: Uri): Int {
        if (ContentResolver.SCHEME_CONTENT != imageUri.scheme
            && ContentResolver.SCHEME_FILE != imageUri.scheme) {
            return 0
        }
        var exif: ExifInterface? = null
        try {
            resolver.openInputStream(imageUri).use { inputStream ->
                if (inputStream == null) {
                    return 0
                }
                exif = ExifInterface(inputStream)
            }
        } catch (e: IOException) {
            return 0
        }
        return exif!!.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean): Bitmap? {
        val matrix = Matrix()

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror the image along the X or Y axis.
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    private fun setUpNewImageListener() {
        button_mlkit_detection.setOnClickListener {
            showDetectionMenu()
        }
    }

    private fun requestRead() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA
            )
        }
        else {
            startCameraIntentForResult()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Permission is required.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCameraIntentForResult() { // Clean up last time's image
        imageUri = null
        preview!!.setImageBitmap(null)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(
                takePictureIntent,
                REQUEST_IMAGE_CAPTURE
            )
        }
    }

    private fun resizeImage(selectedImage: Uri): Bitmap? {
        return getBitmapFromUri(selectedImage)?.let {
            val scaleFactor = Math.max(
                it.width.toFloat() / imageView.width.toFloat(),
                it.height.toFloat() / imageView.height.toFloat()
            )

            Bitmap.createScaledBitmap(
                it,
                (it.width / scaleFactor).toInt(),
                (it.height / scaleFactor).toInt(),
                true
            )
        }
    }

    private fun getBitmapFromUri(filePath: Uri): Bitmap? {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, filePath)
    }
}