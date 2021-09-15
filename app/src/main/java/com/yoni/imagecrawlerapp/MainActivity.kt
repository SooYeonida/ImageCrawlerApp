package com.yoni.imagecrawlerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CoroutineScope(Dispatchers.IO).launch {
            ImageUrlParser.parseImageUrl()
            ImageCache.initializeCache() // test
            withContext(Dispatchers.Main) {
                adapter = ImageAdapter(applicationContext, ImageUrlParser.urlList)
                recyclerView.adapter = adapter
            }

        }
    }
}