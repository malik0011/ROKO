package com.example.roko

import CompressFragment
import GalleryFragment
import InfoFragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.roko.Utils.CompressedImageAdapter
import com.google.android.material.snackbar.Snackbar
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private var compressionLevel: Int = 50 // Default compression level (50%)
    private lateinit var imagePreview: ImageView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    lateinit var compressFragment: CompressFragment
    lateinit var galleryFragment: GalleryFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Set up toolbar as action bar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        window.statusBarColor = getColor(R.color.status_bar_blue)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewPager()
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        compressFragment = CompressFragment()
        galleryFragment = GalleryFragment()

        val pagerAdapter = ViewPagerAdapter(this)
        pagerAdapter.addFragment(compressFragment, "Compress")
        pagerAdapter.addFragment(galleryFragment, "Gallery")

        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Compress" else "Gallery"
        }.attach()
    }

    val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            compressFragment.updateSelectedImage(it)
            Snackbar.make(findViewById(R.id.main), "Image selected successfully!", 
                Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(android.R.color.holo_green_dark))
                .setTextColor(getColor(android.R.color.white))
                .show()
        }
    }

    // ... rest of your existing methods (compressImage, decompressImage, etc.) ..
//.
    fun decompressImage(imageName: String) {
        val originalFolder = File(filesDir, "OriginalPhotos")
        val originalFile = File(originalFolder, imageName)

        val compressedFolder = File(filesDir, "CompressedPhotos")
        val compressedFile = File(compressedFolder, imageName)

        if (originalFile.exists()) {
            originalFile.copyTo(compressedFile, overwrite = true)
            runOnUiThread {
                // Notify the user
                Toast.makeText(this, "Image decompressed successfully!", Toast.LENGTH_SHORT).show()
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "Original image not found!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun compressImage(uri: Uri) {
        val inputFile = File(uri.path ?: return)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Save original image to "OriginalPhotos"
                val originalFolder = File(filesDir, "OriginalPhotos")
                if (!originalFolder.exists()) originalFolder.mkdirs()
                val originalFile = File(originalFolder, inputFile.name)
                inputFile.copyTo(originalFile, overwrite = true)

                // Compress the image and save to "CompressedPhotos"
                val compressedFolder = File(filesDir, "CompressedPhotos")
                if (!compressedFolder.exists()) compressedFolder.mkdirs()
                val compressedImageFile = Compressor.compress(this@MainActivity, inputFile) {
                    quality(compressionLevel)
                }.also { compressedFile ->
                    compressedFile.copyTo(File(compressedFolder, compressedFile.name), overwrite = true)
                }

                runOnUiThread {
                    Snackbar.make(findViewById(android.R.id.content),
                        "Image saved to CompressedPhotos", Snackbar.LENGTH_LONG)
                        .setAction("Open") {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(Uri.fromFile(compressedImageFile), "image/*")
                            startActivity(intent)
                        }.show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Compression failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                showInfoFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInfoFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .add(R.id.fragmentContainer, InfoFragment())
            .addToBackStack(null)
            .commit()
    }
}

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val fragments = mutableListOf<Fragment>()
    private val fragmentTitles = mutableListOf<String>()

    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        fragmentTitles.add(title)
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}