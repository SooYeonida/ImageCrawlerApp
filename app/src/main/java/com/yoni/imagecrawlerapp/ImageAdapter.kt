package com.yoni.imagecrawlerapp

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ImageAdapter(private val context: Context, private val imgUrlList: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(imgUrlList[position])
            }
        }
    }

    //안하는게 좋음. 재활용성의 장점을 못씀..
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return imgUrlList.size;
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById<ImageView>(R.id.img)
        private var bitmap: Bitmap? = null

        fun bind(url: String) {

            //성능 비교용 glide
//            Glide.with(context).load(url)
//                .override(600,600)
////                .skipMemoryCache(true)
////                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(img)

            //원래 구현
//            CoroutineScope(Dispatchers.Main).launch {
//                bitmap = withContext(Dispatchers.IO) {
//                    ImageLoader.loadImage(url)
//                }
//                img.setImageBitmap(bitmap)
//                //bitmap이 남아있어서 이상해보임
//            }

            if(bitmap!=null){
                println("캐싱!")
                img.setImageBitmap(bitmap)
            }
            else{
                //img.setImageBitmap(null)
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO){
                        bitmap = BitmapMaker.loadImage(url)
                        ImageCache.addImageToWarehouse(url, bitmap)
                    }
                    img.setImageBitmap(bitmap)
                }
            }
           bitmap = null
        }
    }

}

