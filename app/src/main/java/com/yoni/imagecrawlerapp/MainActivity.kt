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
            println("이미지 파싱 시작 ")
            ImageUrlParser.parseImageUrl()//이미지 url 파싱
            ImageCache.initializeCache(applicationContext) // test

            //데이터 리사이클러뷰 로드
            withContext(Dispatchers.Main) {
                adapter = ImageAdapter(applicationContext, ImageUrlParser.urlList)
                recyclerView.adapter = adapter
                recyclerView.setHasFixedSize(true)
            }

        }
    }

}