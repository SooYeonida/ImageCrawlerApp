package com.yoni.imagecrawlerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.yoni.imagecrawlerapp.Adapter.ImageAdapter
import com.yoni.imagecrawlerapp.Data.CacheData
import com.yoni.imagecrawlerapp.Network.NetworkDialog
import com.yoni.imagecrawlerapp.Network.NetworkStatus
import com.yoni.imagecrawlerapp.Parser.ImageUrlParser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var mAdapter: ImageAdapter
    private val mNetworkCheck = NetworkStatus(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NetworkDialog.initDialog(this)
        loadImage()
    }

    fun loadImage(){
        loading.visibility = View.VISIBLE
        if(mNetworkCheck.isConnectionOn()) {
            CoroutineScope(Dispatchers.IO).launch {
                requestImageUrl()
                withContext(Dispatchers.Main) {
                    initRecyclerView()
                    loading.visibility = View.GONE
                }
            }
        }
        else{
            Log.d("network", "network fail")
            showNetworkDialog()
        }
    }

    fun showNetworkDialog(){
        loading.visibility = View.GONE
        NetworkDialog.show()
    }

    private fun requestImageUrl(){
        CacheData.initializeCache(applicationContext) //캐시 init
        ImageUrlParser(this).parseImageUrl()//이미지 url 파싱 요청
    }

    private fun initRecyclerView(){
        mAdapter = ImageAdapter(applicationContext)
        recyclerView.adapter = mAdapter
        recyclerView.setHasFixedSize(true)
    }

}