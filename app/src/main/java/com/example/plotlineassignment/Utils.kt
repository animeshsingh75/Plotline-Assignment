package com.example.plotlineassignment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

const val PERMISSION_ALL = 1

object Utils {
    fun showBitmap(context: Context, bitmap: Bitmap, imageView: ImageView) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 90, outputStream)
        val data = outputStream.toByteArray()
        Glide.with(context).load(data).into(imageView)
    }

    private val permission = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    fun requestPermission(context: Context) {
        if (!hasPermissions(context)) {
            ActivityCompat.requestPermissions(context as Activity, permission, PERMISSION_ALL)
        }
    }

    private fun hasPermissions(context: Context): Boolean {
        return permission.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}