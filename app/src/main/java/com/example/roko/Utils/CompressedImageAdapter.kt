package com.example.roko.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.roko.R
import java.io.File

class CompressedImageAdapter (
    private val images: List<File>,
    private val onDecompress: (String) -> Unit
) : RecyclerView.Adapter<CompressedImageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val decompressButton: Button = view.findViewById(R.id.btnDecompress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compressed_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = images[position]
        Glide.with(holder.itemView.context)
            .load(file)
            .into(holder.imageView)

        holder.decompressButton.setOnClickListener {
            onDecompress(file.name)
        }
    }

    override fun getItemCount(): Int = images.size
}