package com.yoni.imagecrawlerapp

import android.util.Log
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.ArrayList

object ImageUrlParser {
    val urlList: ArrayList<String> = ArrayList()
    val keyList: ArrayList<String> = ArrayList()

    lateinit var con : Connection
    lateinit var elements : Elements

    fun parseImageUrl(){
        try{
            con = Jsoup.connect("https://gettyimagesgallery.com/collection/sasha/").timeout(5000)
            Log.d("parse Url", "connection setup and parsing start")
            elements = con.get().select("div.item-wrapper img.jq-lazy")
            if (!elements.isEmpty()) {
                for (e in elements) {
                    val url: String = e.attr("data-src")
                    //DiskLruCache key 형식 제한 때문에 잘라야함
                    val key = url.split("-")[2].replace("[^0-9]".toRegex(), "")
                    keyList.add(key)
                    urlList.add(url)
                }
                Log.d("parse Url", "parsing done")
            }
            else{
                NetworkDialog.show()
            }

        }catch (e:Exception){
            e.printStackTrace()
            NetworkDialog.show()
        }
    }

}