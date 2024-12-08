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
        
        // Set up toolbar as action bar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        window.statusBarColor = getColor(R.color.status_bar_blue)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Create app directories
        StorageHelper.createAppDirectories()

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