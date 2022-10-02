package com.example.plotlineassignment

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.plotlineassignment.Utils.requestPermission
import com.example.plotlineassignment.Utils.showBitmap
import com.example.plotlineassignment.databinding.ActivityMainBinding
import com.example.plotlineassignment.db.Image
import com.example.plotlineassignment.db.ImageDB
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var result: Bitmap
    private lateinit var resultBitmap: Bitmap
    private val db: ImageDB? by lazy {
        ImageDB.getInstance(applicationContext)
    }

    private var mLauncher = registerForActivityResult(
        StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            if (intent != null) {
                val uri = it.data?.data
                val bitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    uri
                )
                showBitmap(this, bitmap, binding.originalImageIv)
                result = bitmap
            }
        }
    }

    private var cameraLauncher = registerForActivityResult(
        StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            if (intent != null) {
                val extras = it.data?.extras
                val imageBitmap = extras?.getParcelable<Bitmap>("data") as Bitmap
                showBitmap(this, imageBitmap, binding.originalImageIv)
                result = imageBitmap
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermission(this)
        binding.pickBtn.setOnClickListener {
            pickImage()
        }
        binding.cameraBtn.setOnClickListener {
            clickImage()
        }
        binding.convertBtn.setOnClickListener {
            if (this::result.isInitialized) {
                if (!binding.linkEdTv.text.isNullOrEmpty()) {
                    convertToEdgeFromUrl(binding.linkEdTv.text.toString())
                } else {
                    detectEdges(result)
                }
            } else {
                Toast.makeText(this, "Please Select an image", Toast.LENGTH_LONG).show()
            }

        }

        binding.convertedBtn.setOnClickListener {
            startActivity(Intent(this, ConvertedImagesActivity::class.java))
        }
    }

    private fun clickImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        mLauncher.launch(intent)
    }

    private fun convertToEdgeFromUrl(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = Glide.with(this@MainActivity)
                .asBitmap()
                .load(url)
                .submit()
                .get()
            withContext(Dispatchers.Main) {
                showBitmap(this@MainActivity, bitmap, binding.originalImageIv)
                detectEdges(bitmap)
            }
        }
    }

    private fun detectEdges(bitmap: Bitmap) {
        binding.loadingPb.visibility = View.VISIBLE
        OpenCVLoader.initDebug()
        val rgba = Mat()
        Utils.bitmapToMat(bitmap, rgba)
        val edges = Mat(rgba.size(), CvType.CV_8UC1)
        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4)
        Imgproc.Canny(edges, edges, 80.0, 100.0)
        resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(edges, resultBitmap)
        CoroutineScope(Dispatchers.IO).launch {
            uploadFirebase(bitmap, resultBitmap)
        }
    }

    private suspend fun uploadFirebase(bitmap: Bitmap, resultBitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            var url: String? = null
            var resultUrl: String? = null
            val storage: FirebaseStorage = FirebaseStorage.getInstance()
            val baos = ByteArrayOutputStream()
            val resultBaos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, resultBaos)
            val data = baos.toByteArray()
            val dataResult = resultBaos.toByteArray()
            val random = (0..1000).random()
            val randomResult = (0..1000).random()
            val ref = storage.reference.child("profile_pics/$random")
            val resultRef = storage.reference.child("$randomResult")
            val uploadTask: UploadTask = ref.putBytes(data)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    url = task.result.toString()
                    val uploadTaskResult: UploadTask = resultRef.putBytes(dataResult)
                    uploadTaskResult.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { taskResult ->
                        if (!taskResult.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@Continuation resultRef.downloadUrl
                    }).addOnCompleteListener { taskResult ->
                        if (taskResult.isSuccessful) {
                            resultUrl = taskResult.result.toString()
                            pushToDB(url, resultUrl)
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Please try again later",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this@MainActivity, "Please try again later", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun pushToDB(originalUrl: String?, resultUrl: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val imageDao = db?.imageDao()
            imageDao?.insert(Image(UUID.randomUUID().toString(), originalUrl, resultUrl))
            withContext(Dispatchers.Main) {
                binding.loadingPb.visibility = View.GONE
                showBitmap(this@MainActivity, resultBitmap, binding.detectEdgesIv)
            }
        }
    }
}