package com.yoni.imagecrawlerapp.Parser

import android.content.Context
import android.util.Log
import com.yoni.imagecrawlerapp.MainActivity
import com.yoni.imagecrawlerapp.Data.UrlData
import org.jsoup.Jsoup

class ImageUrlParser(private val mContext: Context) {
    fun parseImageUrl(){
        try{
            val con = Jsoup.connect("https://gettyimagesgallery.com/collection/sasha/").timeout(5000)
            Log.d("parse Url", "connection setup and parsing start")
            val elements = con.get().select("div.item-wrapper img.jq-lazy")
            if (!elements.isEmpty()) {
                for (e in elements) {
                    val url: String = e.attr("data-src")
                    //DiskLruCache key 형식 제한 때문에 잘라야함
                    val key = url.split("-")[2].replace("[^0-9]".toRegex(), "")
                    UrlData.keyList.add(key)
                    UrlData.urlList.add(url)
                }
                Log.d("parse Url", "parsing done")
            }
            else{
                //데이터 못받아올 경우
                (mContext as MainActivity).showNetworkDialog()
            }
        }catch (e:Exception){
            //time out, unknown host etc
            e.printStackTrace()
            (mContext as MainActivity).showNetworkDialog()
        }
    }

}