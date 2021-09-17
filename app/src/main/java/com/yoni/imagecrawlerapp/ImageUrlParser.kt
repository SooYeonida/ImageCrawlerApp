package com.yoni.imagecrawlerapp

import android.util.Log
import org.jsoup.Jsoup
import java.lang.Exception
import java.util.ArrayList

object ImageUrlParser {
    val urlList :ArrayList<String> = ArrayList()
    val keyList :ArrayList<String> = ArrayList()

    fun parseImageUrl(){
        try {
            val doc = Jsoup.connect("https://gettyimagesgallery.com/collection/sasha/").get()
            Log.d("parse Url", "connection setup and parsing start")
            val elements = doc.select("div.item-wrapper img.jq-lazy")
            if(elements.isEmpty()){
                //재연결 시도?
            }
            else {
                for (e in elements) {
                    val url: String = e.attr("data-src")
                    //DiskLruCache key 형식 제한 때문에 잘라야함
                    val key = url.split("-")[2].replace("[^0-9]".toRegex(), "")
                    keyList.add(key)
                    urlList.add(url)
                }
                Log.d("parse Url", "parsing done")
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}