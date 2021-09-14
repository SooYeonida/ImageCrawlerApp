package com.yoni.imagecrawlerapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL

object ImageLoader {

    fun loadImage(imgUrl: String): Bitmap? {
        val url = URL(imgUrl)
        val stream = url.openStream()
        return BitmapFactory.decodeStream(stream)
    }
}