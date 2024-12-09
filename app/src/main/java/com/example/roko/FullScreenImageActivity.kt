package com.example.roko

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.roko.utils.StorageHelper
import java.io.File

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var decompressButton: Button
    private lateinit var imageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        imageView = findViewById(R.id.fullScreenImageView)
        decompressButton = findViewById(R.id.btnDecompress)

        // Get the image name from the intent
        imageName = intent.getStringExtra("IMAGE_NAME") ?: ""

        // Load the image into the ImageView
        val imageFile = File(StorageHelper.getCompressedPhotosDirectory(), imageName)
        Glide.with(this).load(imageFile).into(imageView)

        // Set up the decompress button
        decompressButton.setOnClickListener {
            decompressImage(imageName)
        }
    }

    private fun decompressImage(imageName: String) {
        // Call the decompress method from MainActivity
        (application as MainActivity).decompressImage(imageName)
        Toast.makeText(this, "Decompression initiated!", Toast.LENGTH_SHORT).show()
    }
} 