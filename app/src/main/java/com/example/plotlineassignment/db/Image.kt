package com.example.plotlineassignment.db

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Types.BLOB

@Entity(tableName = "image")
data class Image(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "original_image", typeAffinity = BLOB) val originalImage: String?,
    @ColumnInfo(name = "converted_image", typeAffinity = BLOB) val convertedImage: String?
)
