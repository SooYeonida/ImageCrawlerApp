package com.yoni.imagecrawlerapp.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yoni.imagecrawlerapp.Bitmap.BitmapMaker
import com.yoni.imagecrawlerapp.Data.CacheData
import com.yoni.imagecrawlerapp.Data.UrlData
import com.yoni.imagecrawlerapp.databinding.GridItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ImageAdapter(private val mContext: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var mBinding: GridItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mBinding = GridItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ItemViewHolder(mBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return UrlData.urlList.size
    }

    inner class ItemViewHolder(private val binding: GridItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.urlPosition = position
        }
    }

}

@BindingAdapter("imgFromUrl")
fun setImage(imageView: ImageView, urlPosition: Int) {
    var bitmap: Bitmap? = CacheData.getBitmapFromCache(UrlData.keyList[urlPosition])
    if (bitmap != null) {
        imageView.setImageBitmap(bitmap)
    } else {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                if (bitmap == null) {
                    bitmap = BitmapMaker().makeSampleBitmap(UrlData.urlList[urlPosition], imageView.context)
                }
                CacheData.addBitmapToCache(UrlData.keyList[urlPosition], bitmap!!)
            }
            imageView.setImageBitmap(bitmap)
        }
    }
}

