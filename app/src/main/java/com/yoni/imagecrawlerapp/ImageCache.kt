package com.yoni.imagecrawlerapp

import android.graphics.Bitmap
import android.util.LruCache


object ImageCache {
    private var imagesWarehouse: LruCache<String, Bitmap>? = null

    fun initializeCache() {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        println("cache size = $cacheSize")
        imagesWarehouse = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                val bitmapByteCount = bitmap.rowBytes * bitmap.height
                return bitmapByteCount / 1024
            }
        }
    }

    fun addImageToWarehouse(key: String?, value: Bitmap?) {
        if (getImageFromWarehouse(key)==null) {
            imagesWarehouse?.put(key, value)
        }
    }

    fun getImageFromWarehouse(key: String?): Bitmap? {
        return if (key != null) {
            imagesWarehouse?.get(key)
        } else {
            null
        }
    }

    fun removeImageFromWarehouse(key: String?) {
        imagesWarehouse?.remove(key)
    }

    fun clearCache() {
        if (imagesWarehouse != null) {
            imagesWarehouse!!.evictAll()
        }
    }


}