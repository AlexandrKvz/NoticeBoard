package com.xotkins.noticeboard.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.io.File

object ImageManager { // менеджер для контроля загружаемых картинок, чтобы не перегружать базу данных
    private const val MAX_IMAGE_SIZE = 1000
    const val WIDTH = 0
    const val HEIGHT = 1

    fun getImageSize(uri: String): List<Int>{ //выдаёт размер картинки
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true // берём только стороны
        }
        BitmapFactory.decodeFile(uri, options) //показываем какой файл берём
        if(imageRotation(uri) == 90) return listOf(options.outHeight, options.outWidth) // показываем какие параметры берём
        else return listOf(options.outWidth, options.outHeight) // показываем какие параметры берём

    }

    private fun imageRotation(uri: String): Int{ //ф-ция для определения ориентации картинки/фото на смартфоне(ширина, высота)
        val rotation: Int
        val imageFile = File(uri) //получаем доступ к файлу по ссылке
        val exif = ExifInterface(imageFile.absolutePath) //создаём переменную и присваиваем ей место нахождения этой картинки
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) // получаем ориентацию картинки/фото
        rotation =  if(orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270){
            90
        }else{
            0
        }
        return rotation
    }

    fun chooseScaleType(im: ImageView, bitMap: Bitmap){// ф-ция для выбора centralScrop, central inside (Чтобы вертикальную не растягивать, а горизонтальную растянуть)
        if(bitMap.width > bitMap.height){
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        }else{
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

        //суспенд показывает что ф-ция будет запускаться в фоном потоке
    suspend fun imageResize(uris: List<String>): List<Bitmap> = withContext(Dispatchers.IO){//ф-ция для сжатия картинок до определенного размера
            val tempList = ArrayList<List<Int>>() //создаём массив элементов с двумя параметрами, ширина и высота
            val bitmapList = ArrayList<Bitmap>() //создаём массив элементов с двумя параметрами, ширина и высота

            for(n in uris.indices){ //считает до конца массива
            val size = getImageSize(uris[n]) //получаем массив с параметрами
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
               bitmapList.add(Picasso.get().load(File(uris[i])).resize(tempList[i][WIDTH], tempList[i][HEIGHT]).get())//здесь мы записываем изменённые размеры в битмап
           }
        }
            return@withContext bitmapList
    }
}