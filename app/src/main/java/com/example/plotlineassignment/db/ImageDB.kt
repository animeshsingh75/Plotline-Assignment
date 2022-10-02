package com.example.plotlineassignment.db

import android.content.Context
import android.util.Log
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Image::class], version = 1)
abstract class ImageDB : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        private val LOCK = Any()
        private const val DATABASE_NAME = "image"
        private var sInstance: ImageDB? = null
        fun getInstance(context: Context): ImageDB? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        ImageDB::class.java, DATABASE_NAME
                    )
                        .build()
                }
            }
            return sInstance
        }
    }
}