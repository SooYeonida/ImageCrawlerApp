# ImageCrawlerApp
Jsoup을 사용한 이미지 크롤링 및 이미지 라이브러리없이 이미지 캐싱

> 개발 기간: 2021.09.13 ~ 2021.09.19   
개발 환경:
Android Studio Version : 4.0.1   
Compile Sdk Version : 30   
Min Sdk Version : 23   
개발 언어 : Kotlin   

### 개발 구조 
![image](https://user-images.githubusercontent.com/50612841/133923351-578c96c1-d855-42c9-96bb-d1ebef87e177.png)   
네트워크 확인 및 처리 부분을 제외한 이미지 로드 관련 클래스들의 관계도.



### 개발 기능
(Coroutine 사용 비동기 처리 기반)   
* Jsoup 이용 사이트 이미지 Url 크롤링
* Url to Bitmap, Bitmap 샘플링(크기 축소)
* RecyclerView 사용 이미지 처리
* 이미지 캐싱 (LruCache 이용한 메모리캐시, DiskLruCache 이용한 디스크캐시)



### 개발 과정 
*  **Jsoup 이용 사이트 이미지 Url 크롤링**
![image](https://user-images.githubusercontent.com/50612841/134287625-3a21af48-3a95-4a99-bda3-f3d70eccb1f1.png)

크롤링하고자 하는 사이트를 개발자도구를 사용하여 살펴본 이미지. Java HTML Parser인 Jsoup을 사용하여 해당 사이트의 이미지들을 파싱함.    
처음에는 div.item-wrapper -> a[href]로 타고 들어가서 Url을 파싱해주었는데, Url을 Decoding해서 Bitmap으로 만드는 작업에서 다음과 같은 에러 발생. 


![image](https://user-images.githubusercontent.com/50612841/133923684-e3845730-02cc-4e91-b768-f291197c559a.png)   

디버깅 해보니 Bitmap 생성과정이 문제가 아니라, 이미지가아닌 사이트 Url을 파싱해서 생긴 이슈였음.  
파싱한 Url은 이미지의 상세페이지 Url이었고, 실제 이미지는 <a> 밑에 <img>까지 타고 들어가야했음.
            
```kotlin
val con = Jsoup.connect("https://gettyimagesgallery.com/collection/sasha/").timeout(10000)
            Log.d("parse Url", "connection setup and parsing start")
            val elements = con.get().select("div.item-wrapper img.jq-lazy")
            if (!elements.isEmpty()) {
                for (e in elements) {
                    val url: String = e.attr("data-src")
                    //DiskLruCache key 형식 제한 때문에 잘라야함
                    println("url: "+url)
                    val key = url.split("-")[2].replace("[^0-9]".toRegex(), "")
                    UrlData.keyList.add(key)
                    UrlData.urlList.add(url)
                }
                Log.d("parse Url", "parsing done")
            }
            else{
                //데이터 못받아올 경우
                Log.d("parse Url", "data load fail")
                (mContext as MainActivity).showNetworkDialog()
            }
```

![image](https://user-images.githubusercontent.com/50612841/133923915-6be1739d-a06a-4665-96fc-f885e56dd5ed.png)  
            
경로를 올바르게 설정하고 크롤링한 결과 이미지 Url을 제대로 파싱할 수 있었고, 에러없이 제대로 Bitmap 생성되는 것을 확인했음. 

            
            
* **RecyclerView 사용 이미지 로드**
            
```kotlin
Glide.with(context).load(UrlData.urlList[position])
                .override(600,600)
                .into(img)
```
            
성능비교를 위해 본격적인 구현에 앞서 Glide로 먼저 이미지를 띄워보았음. 실행 결과, 이미지를 처음 받아오고 스크롤 하면 매끄럽고 빠르게 이미지가 로드되는 것을 확인할 수 있었음. 
Glide 자체가 메모리캐시와 디스크캐시를 처리해주기 때문에 볼 수 있는 결과였음. 
            
            

```kotlin
 CoroutineScope(Dispatchers.Main).launch {
                img.setImageBitmap(null)
                bitmap = withContext(Dispatchers.IO) {
                    BitmapMaker.loadImage(url)
                }
                img.setImageBitmap(bitmap)
            }
```
            
RecyclerView Adapter에서 View가 bind될때마다 위 사진처럼 처리를 해주었음. Url로 매번 Bitmap을 생성하여 뷰에 띄우는 형식으로 처리해주었는데, 실행 결과 사진 띄우는 과정이 느리고 스크롤 시 데이터가 섞이며 버벅이는 문제가 발생하였음. 
List형식이 아니라 Grid형식인데다가 매번 Bitmap을 생성하고 띄우기 때문에 생기는 문제였음.
            
            

```kotlin
 override fun getItemViewType(position: Int): Int {
        return position
    }
```
            
데이터가 섞이는 문제를 해결할 방법을 찾다가 getItemViewType함수를 사용하여 처리하는 방법을 보았는데, 데이터가 섞이면서 로드되는 문제는 없어지나 RecyclerView의 재활용성이라는 장점을 못 쓰는 방법이었기 때문에 사용하지 않았음.

            
* **메모리캐시, 디스크캐시, Bitmap 샘플링 사용하여 RecyclerView에 이미지 로드**
```kotlin
bitmap = ImageCache.getBitmapFromMemoryCache(url)
            if(bitmap!=null){
                img.setImageBitmap(bitmap)
            }
            else{
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO){
                        bitmap = BitmapMaker.getImage(url,context) 
                        ImageCache.addBitmapFromMemoryCache(url, bitmap)
                    }
                    img.setImageBitmap(bitmap)
                }
            }
```
            
            
버벅임을 해결하기 위해서 안드로이드 개발 문서를 참고해서 LruCache를 이용한 메모리 캐시를 구현했음. 
메모리캐시에 Url에 해당하는 Bitmap이 있으면 바로 뷰에 띄우고, 없으면 Bitmap을 생성해서 띄우는 방식으로 처리했음. 결과는 전보다 나아지긴 했지만 여전히 스크롤시 버벅임이 존재했음. 
            

![image](https://user-images.githubusercontent.com/50612841/133924601-6d2664c6-2bfc-4266-b3ed-930424e5b965.png)  
            
개발 문서를 참고해보니 GridView같은 요소는 메모리캐시를 쉽게 채울 수 있고 다른 작업에 의해서 캐시가 삭제될 수 있다는 점을 알 수 있었음. 
해당 프로젝트에서 구현한 것은 GridView에 여러 Bitmap을 로드하는 것이기 때문에, Bitmap 생성과정과 더불어 메모리캐시가 금방차고 데이터가 삭제되는 과정의 반복으로 버벅임이 생기는 것 같았음. 

            
```kotlin
bitmap = CacheData.getBitmapFromCache(UrlData.keyList[position])
            if (bitmap != null) {
                img.setImageBitmap(bitmap)
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        if (bitmap == null) {
                            bitmap = BitmapMaker().makeSampleBitmap(imgUrlList[position], context)
                        }
                        CacheData.addBitmapToCache(UrlData.keyList[position], bitmap!!)
                    }
                    img.setImageBitmap(bitmap)
                }
            }
```
```kotlin
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
```
            
문서를 참고해서 디스크캐시를 구현하고 View bind시에 메모리캐시에 Bitmap이 있으면 뷰에 띄우고 없으면 디스크캐시를 확인하는 식으로 코드를 변경했음. 
만약 디스크캐시에도 데이터가 없는 경우에는 Bitmap을 생성하여 뷰에 띄우도록 했음.

            
```kotlin
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
```
            
또한 개발문서를 읽던 도중, Bitmap을 효율적으로 로드하는 방법에 대해서 읽게되었고 해당 부분도 추가해주었음. 
Url로 Bitmap을 생성할 경우 원본 크기 그대로 생성하는 것이 아닌, 축소된 사이즈로 Bitmap을 생성해서 저장했음.

Bitmap 샘플링과 메모리캐시, 디스크캐시를 사용하여 이미지 처리를한 결과 한번 데이터가 로드되면 그 후로 아주 매끄럽게 버벅임 없이 스크롤 되는 모습 확인할 수 있었음.   
            
![1 resize](https://user-images.githubusercontent.com/50612841/134019876-ca530297-76bf-44b1-a3e9-7123806ebc1a.gif)
![2 resize](https://user-images.githubusercontent.com/50612841/134019883-20337413-eeeb-4583-aad6-1f30b01feae1.gif)
![3 resize](https://user-images.githubusercontent.com/50612841/134019886-80292f2f-b29b-40e5-94cd-60e87254c75b.gif)
    
            
* **네트워크 처리**    
![image](https://user-images.githubusercontent.com/50612841/133924793-5f890dcf-50e2-4b86-ac9d-9a9581ce13c7.png).  
사이트에 Url을 크롤링할 때 네트워크가 안좋은 경우에는 이미지 Url들을 못받아와서 화면에 아무것도 뜨지 않는 문제가 발생했음. 해당 문제 해결을 위해 요청 전 네트워크를 확인하고 안좋을 경우 다이얼로그를 띄우는 형식으로 처리.  
            
* **화면 돌릴 때 데이터 재로드 방지**.  
기기 화면을 세로로 보다가 가로로 회전시키는 경우, Activity가 Destroy됐다가 다시 Create되어서 이미지가 이미 로드되었음에도 불구하고 다시 받아오는 문제가 발생. 값을 계속 바꾸는 부분이 존재하지 않고 이미지만 유지하면 되는 작업이므로 android:configChanges="screenSize|orientation 옵션을 추가하여 화면 회전 시에도 이미지가 유지되도록 하였음.

                    
### 배운점
* **캐싱에 대한 이해**  
안드로이드 프로젝트를 개발하면서 Glide와 같은 편한 이미지 라이브러리를 사용했기때문에 라이브러리가 제공하는 편한 기능을 당연하게 여겼다. 하지만 라이브러리를 쓰지않고 직접 문제를 직면하고 해결하고자하는 과정이 생각보다 쉽지 않았다. 알고쓰는 것과 모르고 쓰는 것은 다르다고, Glide의 동작방식에 대해서 이해하게 되었고 왜 많은 개발자들이 라이브러리를 쓰는지 이점에 대해 정확히 이해할 수 있게 되었다.

* **Jsoup을 이용한 크롤링**  
사실 크롤링은 학부시절에 파이썬을 이용해서 간단히해본 경험밖에 없었는데, 이번 프로젝트를 통해서 Java로도 크롤링이 가능하다는 사실을 알게되었다. Jsoup은 생각과 달리 사용법이 간단했고 Jsoup을 이용해서 직접 크롤링하고 크롤링한 정보를 기반으로 앱을 구상하는 작업이 재미있었다. 크롤링과정에서도 약간의 시행착오를 겪었기때문에 다음에 다시 Jsoup을 이용하여 파싱할 일이 생긴다면 좀 더 잘 할 수 있을 것같다는 생각이 들었다. 

