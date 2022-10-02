package com.example.plotlineassignment

import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plotlineassignment.databinding.ActivityConvertedImagesBinding
import com.example.plotlineassignment.db.ImageDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConvertedImagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConvertedImagesBinding

    private val db: ImageDB? by lazy{
        ImageDB.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConvertedImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CoroutineScope(Dispatchers.IO).launch {
            val list=db?.imageDao()?.getAll()
            val adapter = list?.let { ConvertedImageAdapter(it) }
            withContext(Dispatchers.Main){
                binding.imageRv.layoutManager = LinearLayoutManager(this@ConvertedImagesActivity)
                binding.imageRv.adapter = adapter
            }
        }

    }
}