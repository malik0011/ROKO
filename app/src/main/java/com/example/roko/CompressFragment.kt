import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.roko.MainActivity
import com.example.roko.R
import com.example.roko.utils.StorageHelper
import com.google.android.material.snackbar.Snackbar
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CompressFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    private var compressionLevel: Int = 50
    private lateinit var imagePreview: ImageView
    private lateinit var mainActivity: MainActivity
    private lateinit var tvImageSize: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_compress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity

        imagePreview = view.findViewById(R.id.imagePreview)
        tvImageSize = view.findViewById(R.id.tvImageSize)
        val selectImageButton: Button = view.findViewById(R.id.btnSelectImage)
        val compressImageButton: Button = view.findViewById(R.id.btnCompressImage)
        val compressionSeekBar: SeekBar = view.findViewById(R.id.seekBarCompression)
        val tvCompressionLevel: TextView = view.findViewById(R.id.tvCompressionLevel)

        tvCompressionLevel.text = "$compressionLevel%"

        // Image selection
        selectImageButton.setOnClickListener {
            mainActivity.pickImage.launch("image/*")
        }

        // Set compression level
        compressionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                compressionLevel = progress
                tvCompressionLevel.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Compress image
        compressImageButton.setOnClickListener {
            if (selectedImageUri != null) {
                compressImage(selectedImageUri!!, compressionLevel)
            } else {
                Snackbar.make(view, "Please select an image first!", 
                    Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
                    .setTextColor(requireContext().getColor(android.R.color.white))
                    .show()
            }
        }
    }

    fun updateSelectedImage(uri: Uri) {
        selectedImageUri = uri
        Glide.with(this)
            .load(uri)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imagePreview)
            
        // Get and display image size
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val size = inputStream?.available()?.let { formatFileSize(it) } ?: "Unknown size"
            tvImageSize.text = "Original Size: $size"
            inputStream?.close()
        } catch (e: Exception) {
            tvImageSize.text = "Size: Unable to determine"
        }
    }

    private fun formatFileSize(size: Int): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.1f KB", size / 1024f)
            else -> String.format("%.1f MB", size / (1024f * 1024f))
        }
    }

    fun compressImage(uri: Uri, compressionLevel: Int) {
        try {
            // Create a temporary file from the URI
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File(requireContext().cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Save original image to external storage
                    val originalFileName = "img_${System.currentTimeMillis()}.jpg"
                    val originalFile = File(StorageHelper.getOriginalPhotosDirectory(), originalFileName)
                    tempFile.copyTo(originalFile, overwrite = true)

                    // Compress the image and save to external storage
                    val compressedImageFile = Compressor.compress(requireContext(), tempFile) {
                        quality(compressionLevel)
                    }
                    
                    // Save the compressed file
                    val compressedDestination = File(StorageHelper.getCompressedPhotosDirectory(), originalFileName)
                    compressedImageFile.copyTo(compressedDestination, overwrite = true)

                    activity?.runOnUiThread {
                        // Create content URI using FileProvider
                        val contentUri = androidx.core.content.FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.provider",
                            compressedDestination
                        )

                        Snackbar.make(requireView(),
                            "Image compressed successfully!", 
                            Snackbar.LENGTH_LONG)
                            .setAction("Open") {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(contentUri, "image/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, 
                                        "Cannot open image: ${e.message}", 
                                        Toast.LENGTH_SHORT).show()
                                }
                            }.show()

                        // Safely refresh the gallery fragment
                        (activity as? MainActivity)?.let { mainActivity ->
                            try {
                                mainActivity.galleryFragment.setUpRecyclerView()
                            } catch (e: Exception) {
                                // Handle the case where gallery fragment is not ready
                                Log.d("CompressFragment", "Gallery fragment not ready: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Compression failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        resetImagePreview()
                    }
                } finally {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to process image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetImagePreview() {
        imagePreview.setImageResource(R.drawable.ic_image_placeholder)
        selectedImageUri = null
    }
} 