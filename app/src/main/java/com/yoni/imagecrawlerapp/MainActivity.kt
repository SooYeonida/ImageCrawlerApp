package com.yoni.imagecrawlerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yoni.imagecrawlerapp.Adapter.ImageAdapter
import com.yoni.imagecrawlerapp.Data.CacheData
import com.yoni.imagecrawlerapp.Network.NetworkDialog
import com.yoni.imagecrawlerapp.Network.NetworkStatus
import com.yoni.imagecrawlerapp.Parser.ImageUrlParser
import com.yoni.imagecrawlerapp.Data.UrlData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ImageAdapter
    private val networkCheck = NetworkStatus(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NetworkDialog.initDialog(this)
        loadImage()
    }


    fun loadImage(){
        if(networkCheck.isConnectionOn()) {
            CoroutineScope(Dispatchers.IO).launch {
                requestImageUrl()
                withContext(Dispatchers.Main) {
                    initRecyclerView()
                }
            }
        }
        else{
            showNetworkDialog()
        }
    }

    fun showNetworkDialog(){
        NetworkDialog.show()
    }

    private fun requestImageUrl(){
        CacheData.initializeCache(applicationContext) //캐시 init
        ImageUrlParser(this).parseImageUrl()//이미지 url 파싱 요청
    }

    private fun initRecyclerView(){
        adapter = ImageAdapter(applicationContext, UrlData.urlList)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }

}