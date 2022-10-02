package com.example.plotlineassignment

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.plotlineassignment.databinding.ItemImageLayoutBinding
import com.example.plotlineassignment.db.Image

class ConvertedImageAdapter(
    private val data: List<Image>,
) :
    RecyclerView.Adapter<ConvertedImageAdapter.ImageListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageListViewHolder {
        val binding =
            ItemImageLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ImageListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageListViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ImageListViewHolder(
        private val binding: ItemImageLayoutBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Image) {
            val context = binding.detectEdgesIv.context
            Glide.with(context).load(image.originalImage)
                .placeholder(R.drawable.ic_launcher_background).into(binding.originalImageIv)
            Glide.with(context).load(image.convertedImage)
                .placeholder(R.drawable.ic_launcher_background).into(binding.detectEdgesIv)
        }
    }
}