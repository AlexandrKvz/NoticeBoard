package com.xotkins.noticeboard.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.xotkins.noticeboard.adapters.ImageAdapter
import com.xotkins.noticeboard.model.Announcement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object ImageManager { // менеджер для контроля загружаемых картинок, чтобы не перегружать базу данных
    private const val MAX_IMAGE_SIZE = 1000
    const val WIDTH = 0
    const val HEIGHT = 1

    fun getImageSize(uri: Uri, activity: Activity): List<Int>{ //выдаёт размер картинки
        val inStream = activity.contentResolver.openInputStream(uri)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true // берём только стороны
        }
        BitmapFactory.decodeStream(inStream,null, options) //показываем какой файл берём
        return listOf(options.outWidth, options.outHeight) // показываем какие параметры берём
    }

    fun chooseScaleType(im: ImageView, bitMap: Bitmap){// ф-ция для выбора centralScrop, central inside (Чтобы вертикальную не растягивать, а горизонтальную растянуть)
        if(bitMap.width > bitMap.height){
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        }else{
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }
        //суспенд показывает что ф-ция будет запускаться в фоном потоке
    suspend fun imageResize(uris: List<Uri>, activity: Activity): List<Bitmap> = withContext(Dispatchers.IO){//ф-ция для сжатия картинок до определенного размера
            val tempList = ArrayList<List<Int>>() //создаём массив элементов с двумя параметрами, ширина и высота
            val bitmapList = ArrayList<Bitmap>() //создаём массив элементов с двумя параметрами, ширина и высота

            for(n in uris.indices){ //считает до конца массива
            val size = getImageSize(uris[n], activity) //получаем массив с параметрами
            val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat() // тут мы указываем первый параметр делим на второй, обязательно с плавающей точкой (ширина / высота), зависит как указано в fun getImageSize

            if(imageRatio > 1){ // определяем большую сторону, т.е. иными словами картинка горизонтальная или вертикальная, если больше одного горизонтальная
                if(size[WIDTH] > MAX_IMAGE_SIZE){ //если ширина больше максимального размера то ---
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt())) //делаем ширину максимального размера по константе, а высоту находим соотношением максимальный размер делим imageRatio
                }else{ //или
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))//просто записываем, как они есть
                }
            }else{
                if(size[HEIGHT] > MAX_IMAGE_SIZE){ //если высота больше максимального размера то ---
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE)) //ширину умножаем на imageRatio, а высоту делаем максимального размера по константе
                }else{ //или
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))//просто записываем, как они есть
                }
            }
        }
        for(i in uris.indices) {//берём список и перебираем его для сжатия размера всех элементов
        kotlin.runCatching {
               bitmapList.add(Picasso.get().load(uris[i]).resize(tempList[i][WIDTH], tempList[i][HEIGHT]).get())//здесь мы записываем изменённые размеры в битмап
           }
        }
            return@withContext bitmapList
    }

    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO){//ф-ция для получения картинок с storage
        val bitmapList = ArrayList<Bitmap>() //создаём массив элементов
        for(i in uris.indices) {//берём список и перебираем его для получения битмапов
            kotlin.runCatching {
                bitmapList.add(Picasso.get().load(uris[i]).get())//здесь мы записываем ссылки битмап
            }
        }
        return@withContext bitmapList //возвращаем заполненый битмап
    }

    fun fillImageArray(announcement: Announcement, adapter: ImageAdapter){ //ф-ция для заполнения картинок
        val listUris = listOf(announcement.image1, announcement.image2, announcement.image3) //получаем ссылки загруженных картинок
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = getBitmapFromUris(listUris) //здесь ссылки превращаем в bitmap
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }
}