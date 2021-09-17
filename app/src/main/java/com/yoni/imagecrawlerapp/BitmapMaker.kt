package com.yoni.imagecrawlerapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL


class BitmapMaker {

    //축소된 사이즈로 비트맵 생성
    fun makeSampleBitmap(imgUrl: String, context: Context):Bitmap? {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        val url = URL(imgUrl)

        //핸드폰 해상도에 맞게 - 행과 열에 3개씩 들어간다고 가정
        val reqWidth = context.resources.displayMetrics.widthPixels/3
        val reqHeight = context.resources.displayMetrics.heightPixels/3

        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight)

        options.inJustDecodeBounds = false
        return BitmapFactory.decodeStream(url.openStream(),null,options)
    }


    //샘플사이즈 계산
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


}