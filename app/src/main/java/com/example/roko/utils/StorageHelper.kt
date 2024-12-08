package com.example.roko.utils

import android.os.Environment
import java.io.File

object StorageHelper {
    private const val APP_FOLDER = "ROKO"
    private const val ORIGINAL_FOLDER = "Original Photos"
    private const val COMPRESSED_FOLDER = "Compressed Photos"

    fun getOriginalPhotosDirectory(): File {
        val baseDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_FOLDER)
        val originalDir = File(baseDir, ORIGINAL_FOLDER)
        if (!originalDir.exists()) {
            originalDir.mkdirs()
        }
        return originalDir
    }

    fun getCompressedPhotosDirectory(): File {
        val baseDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_FOLDER)
        val compressedDir = File(baseDir, COMPRESSED_FOLDER)
        if (!compressedDir.exists()) {
            compressedDir.mkdirs()
        }
        return compressedDir
    }

    fun createAppDirectories() {
        getOriginalPhotosDirectory()
        getCompressedPhotosDirectory()
    }

    fun getAppFolder(): File {
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_FOLDER)
    }

    fun clearAppDirectories() {
        getOriginalPhotosDirectory().deleteRecursively()
        getCompressedPhotosDirectory().deleteRecursively()
        createAppDirectories()
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in 
            setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
} 