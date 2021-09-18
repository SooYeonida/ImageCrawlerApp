package com.yoni.imagecrawlerapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import android.util.Log
import android.util.LruCache
import com.jakewharton.disklrucache.DiskLruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


object ImageCache {
    //메모리캐시
    private var memoryLruCache: LruCache<String, Bitmap>? = null

    //디스크캐시
    private const val DISK_CACHE_SIZE : Long = 1024 * 1024 * 10 // 10MB
    private const val DISK_CACHE_SUBDIR = "thumbnails"
    private var diskLruCache: DiskLruCache? = null
    private val diskCacheLock = ReentrantLock()
    private val diskCacheLockCondition: Condition = diskCacheLock.newCondition()
    private var diskCacheStarting = true

    private const val IO_BUFFER_SIZE = 8 * 1024
    private val mCompressFormat = Bitmap.CompressFormat.JPEG
    private const val mCompressQuality = 70

    //캐시 초기화
    fun initializeCache(context: Context) {
        //메모리캐시
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryLruCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                val bitmapByteCount = bitmap.rowBytes * bitmap.height
                return bitmapByteCount / 1024
            }
        }

        //디스크캐시
        val cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR)
        CoroutineScope(Dispatchers.IO).launch {
            diskCacheLock.withLock {
                diskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE)
                diskCacheStarting = false
                diskCacheLockCondition.signalAll()
            }
        }
    }


    private fun getDiskCacheDir(context: Context, uniqueName: String): File {
        val cachePath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !isExternalStorageRemovable()) {
                    if(context.externalCacheDir==null){
                        context.cacheDir.path
                    }
                else{
                        context.externalCacheDir?.path
                    }
            } else {
                context.cacheDir.path
            }

        Log.d("TTT","cache path: $cachePath")
        return File(cachePath + File.separator + uniqueName)
    }

    fun addBitmapToCache(key: String?, value: Bitmap){
        if(getBitmapFromMemoryCache(key)==null){
            memoryLruCache?.put(key, value)
        }

        synchronized(diskCacheLock){
            diskLruCache?.apply {
                if(!containsKey(key)){
                    put(key, value)
                }
            }
        }
    }

    private fun containsKey(key: String?): Boolean {
        var contained = false
        var snapshot: DiskLruCache.Snapshot? = null
        try {
            snapshot = diskLruCache!![key]
            contained = snapshot != null
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            snapshot?.close()
        }
        return contained
    }

    private fun put(key: String?, data: Bitmap) {
        var editor: DiskLruCache.Editor? = null
        try {
            editor = diskLruCache!!.edit(key)
            if (editor == null) {
                return
            }
            if (writeBitmapToFile(data, editor)) {
                diskLruCache!!.flush()
                editor.commit()
                if (BuildConfig.DEBUG) {
                    Log.d("cache_DISK_", "image put on disk cache $key")
                }
            } else {
                editor.abort()
                if (BuildConfig.DEBUG) {
                    Log.d("cache_DISK_", "ERROR on: image put on disk cache $key")
                }
            }
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) {
                Log.d("cache_DISK_", "ERROR on: image put on disk cache $key")
            }
            try {
                editor?.abort()
            } catch (ignored: IOException) {
            }
        }
    }

    @Throws(IOException::class, FileNotFoundException::class)
    private fun writeBitmapToFile(bitmap: Bitmap, editor: DiskLruCache.Editor): Boolean {
        var out: OutputStream? = null
        return try {
            out = BufferedOutputStream(editor.newOutputStream(0),IO_BUFFER_SIZE)
            bitmap.compress(mCompressFormat, mCompressQuality, out)
        } finally {
            out?.close()
        }
    }

    fun getBitmapFromCache(key: String):Bitmap?{
        var bitmap : Bitmap? = getBitmapFromMemoryCache(key)
        if(bitmap == null){
            bitmap = getBitmapFromDiskCache(key)
             Log.d("cache_DISK_", "image read from disk $key")
        }
        else{
             Log.d("cache_MEMORY_","image read from memory $key")
        }
        return bitmap
    }

    private fun getBitmapFromMemoryCache(key: String?): Bitmap? {
        return if (key != null) {
            memoryLruCache?.get(key)
        } else {
            null
        }
    }

    private fun getBitmapFromDiskCache(key: String): Bitmap? {
        diskCacheLock.withLock {
            // Wait while disk cache is started from background thread
            while (diskCacheStarting) {
                try {
                    diskCacheLockCondition.await()
                } catch (e: InterruptedException) {
                }

            }
            return getBitmapFromSnapshot(key)
        }
    }


    private fun getBitmapFromSnapshot(key: String): Bitmap? {
        var bitmap: Bitmap? = null
        var snapshot: DiskLruCache.Snapshot? = null
        try {
            snapshot = diskLruCache?.get(key)
            if (snapshot == null) {
                return null
            }
            val `in` = snapshot.getInputStream(0)
            if (`in` != null) {
                val buffIn = BufferedInputStream(`in`,IO_BUFFER_SIZE)
                bitmap = BitmapFactory.decodeStream(buffIn)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            snapshot?.close()
        }

        return bitmap
    }



}