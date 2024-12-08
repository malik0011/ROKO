import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.google.android.material.snackbar.Snackbar
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import CompressFragment
import GalleryFragment
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roko.Utils.CompressedImageAdapter
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CompressFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    private var compressionLevel: Int = 50
    private lateinit var imagePreview: ImageView
    private lateinit var mainActivity: MainActivity

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
                    // Save original image to "OriginalPhotos"
                    val originalFolder = File(requireContext().filesDir, "OriginalPhotos")
                    if (!originalFolder.exists()) originalFolder.mkdirs()
                    val originalFileName = "img_${System.currentTimeMillis()}.jpg"
                    val originalFile = File(originalFolder, originalFileName)
                    tempFile.copyTo(originalFile, overwrite = true)

                    // Compress the image and save to "CompressedPhotos"
                    val compressedFolder = File(requireContext().filesDir, "CompressedPhotos")
                    if (!compressedFolder.exists()) compressedFolder.mkdirs()
                    
                    val compressedImageFile = Compressor.compress(requireContext(), tempFile) {
                        quality(compressionLevel)
                    }.also { compressedFile ->
                        compressedFile.copyTo(File(compressedFolder, originalFileName), overwrite = true)
                    }

                    activity?.runOnUiThread {
                        // Create content URI using FileProvider
                        val contentUri = androidx.core.content.FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.provider",
                            compressedImageFile
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

                        // Refresh the gallery fragment
                        (activity as? MainActivity)?.let { mainActivity ->
                            mainActivity.galleryFragment.setUpRecyclerView()
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