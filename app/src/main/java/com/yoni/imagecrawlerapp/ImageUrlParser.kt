package com.yoni.imagecrawlerapp

import android.util.Log
import org.jsoup.Jsoup
import java.util.ArrayList

object ImageUrlParser {
    val urlList :ArrayList<String> = ArrayList()

    fun parseImageUrl(){
        val doc = Jsoup.connect("https://gettyimagesgallery.com/collection/sasha/").get()
        Log.d("TTT","connection setup and parsing start")
        val elements = doc.select("div.item-wrapper img.jq-lazy")
        for( e in elements){
            val url : String = e.attr("data-src")
            urlList.add(url)
        }
        Log.d("TTT","parsing done")
    }

}