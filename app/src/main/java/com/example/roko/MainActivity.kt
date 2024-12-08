package com.example.roko

import CompressFragment
import GalleryFragment
import InfoFragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
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
import android.view.View
import android.widget.LinearLayout
import com.example.roko.utils.StorageHelper

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
        
        // Start with splash screen
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.main, SplashFragment())
                .commit()
        }
    }

    fun setupMainContent() {
        // Find the main content container
        val mainContent = findViewById<LinearLayout>(R.id.mainContent)
        mainContent.visibility = View.VISIBLE

        // Set up ViewPager
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Initialize fragments
        compressFragment = CompressFragment()
        galleryFragment = GalleryFragment()

        // Set up ViewPager adapter
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> compressFragment
                    1 -> galleryFragment
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }
        }

        viewPager.adapter = pagerAdapter

        // Set up TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Compress"
                1 -> "Gallery"
                else -> ""
            }
        }.attach()

        // Create app directories
        StorageHelper.createAppDirectories()
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

    fun decompressImage(imageName: String) {
        val originalFile = File(StorageHelper.getOriginalPhotosDirectory(), imageName)
        val compressedFile = File(StorageHelper.getCompressedPhotosDirectory(), imageName)

        if (originalFile.exists()) {
            originalFile.copyTo(compressedFile, overwrite = true)
            runOnUiThread {
                // Notify the user
                Toast.makeText(this, "Image decompressed successfully!", Toast.LENGTH_SHORT).show()
                // Refresh gallery
                galleryFragment.setUpRecyclerView()
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "Original image not found!", Toast.LENGTH_SHORT).show()
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