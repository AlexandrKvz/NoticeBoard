package com.xotkins.noticeboard.utils

import android.content.Context
import android.widget.TextView
import com.xotkins.noticeboard.R
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

object CityHelper {
    fun getAllCountries(context: Context): ArrayList<String>{ //ф-ция для выбора страны
        var tempArray = ArrayList<String>() //создаём массив
        try{
            val inputStream : InputStream = context.assets.open("countriesToCities.json") //находим файл и начинаем переводить в тип стринг
            val size: Int = inputStream.available() // узнаем размер файла
            val bytesArray = ByteArray(size) //создаём массив, куда помещаем считанные байты
            inputStream.read(bytesArray) // считываем и записываем в массив
            val jsonFile = String(bytesArray) //превращаем массив в обьект стринг
            val jsonObject = JSONObject(jsonFile) //указываем какой файл хотим превратить в JSONObject
            val cityNames = jsonObject.names() // здесь получаем все называния стран

            //цикл от 0 до конца массива(до конца всех стран)
            for(n in 0 until cityNames.length()){
                tempArray.add(cityNames.getString(n))
            }

        }catch (e:IOException){

        }
        return tempArray
    }

    fun getAllCities(country: String, context: Context): ArrayList<String>{ //ф-ция для выбора города
        var tempArray = ArrayList<String>() //создаём массив для выбора города
        try{
            val inputStream : InputStream = context.assets.open("countriesToCities.json") //находим файл и начинаем переводить в тип стринг
            val size: Int = inputStream.available() // узнаем размер файла
            val bytesArray = ByteArray(size) //создаём массив, куда помещаем считанные байты
            inputStream.read(bytesArray) // считываем и записываем в массив
            val jsonFile = String(bytesArray) //превращаем массив в обьект стринг
            val jsonObject = JSONObject(jsonFile) //указываем какой файл хотим превратить в JSONObject
            val countriesNames = jsonObject.getJSONArray(country) // здесь получаем все называния стран

            //цикл от 0 до конца массива(до конца всех стран)
            if(countriesNames != null){
                for(n in 0 until countriesNames.length()){
                    tempArray.add(countriesNames.getString(n))
                }
            }
        }catch (e:IOException){

        }
        return tempArray
    }

    fun filterListData(list: ArrayList<String>, searchText: String?): ArrayList<String>{ //функция для фильтрация списка, вводишь и появлется страна для выбора
        val tempList = ArrayList<String>()
        tempList.clear()
        if(searchText == null){
            tempList.add("No result")
            return tempList
        }
        for(selection: String in list){
            if(selection.lowercase(Locale.getDefault()).startsWith(searchText.lowercase(Locale.getDefault()))) //проверяет если есть совпадения то вставляет, проверяется, когда пользователь пишит мелким шрифтом
                tempList.add(selection)
        }
        if(tempList.size == 0)tempList.add("No result")

        return tempList
    }
}