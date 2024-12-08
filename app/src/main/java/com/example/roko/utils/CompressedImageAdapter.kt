package com.example.roko.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.roko.R
import java.io.File

class CompressedImageAdapter(
    private val images: List<File>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CompressedImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val tvImageName: TextView = view.findViewById(R.id.tvImageName)
        val tvImageSize: TextView = view.findViewById(R.id.tvImageSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compressed_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = images[position]
        
        // Load image
        Glide.with(holder.imageView)
            .load(image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.imageView)

        // Set image name
        holder.tvImageName.text = image.name

        // Set image size
        val size = formatFileSize(image.length())
        holder.tvImageSize.text = size

        holder.itemView.setOnClickListener {
            onItemClick(image.name)
        }
    }

    override fun getItemCount() = images.size

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.1f KB", size / 1024f)
            else -> String.format("%.1f MB", size / (1024f * 1024f))
        }
    }
}