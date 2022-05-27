package com.xotkins.noticeboard.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.databinding.ActivityFilterBinding
import com.xotkins.noticeboard.dialogs.DialogSpinnerHelper
import com.xotkins.noticeboard.utils.CityHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClickSelectCountry()
        onClickSelectCity()
        actionBarSettings()
        onClickDoneFilter()
        onClickClear()
        getFilter()
    }

    private fun getFilter() = with(binding){ //здесь мы принимаем данные
        val filter = intent.getStringExtra(FILTER_KEY)
        if(filter != null && filter != "empty"){
            val filterArray = filter.split("_") //здесь мы стринг превращаем в массив разделяя по нижнему подчеркиванию
            if(filterArray[0] != "empty") tvCountry.text = filterArray[0] //если не равно стандартному значению записываем
            if(filterArray[1] != "empty") tvCity.text = filterArray[1]//если не равно стандартному значению записываем
            if(filterArray[2] != "empty") edIndex.setText(filterArray[2]) //если не равно пусто записываем
            checkBoxWithSend.isChecked = filterArray[3].toBoolean() //проверяем по состоянию тру или фалс
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }


    private fun onClickSelectCountry() = with(binding){ //ф-ция для выбора страны
        tvCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, tvCountry)
            if(tvCity.text.toString() != getString(R.string.select_city)){
                tvCity.text = getString(R.string.select_city)
            }
        }
    }

    private fun onClickSelectCity() = with(binding){ //ф-ция для выбора города
        tvCity.setOnClickListener {
            val selectedCountry = tvCountry.text.toString()
            if(selectedCountry != getString(R.string.select_country)){
                val listCity = CityHelper.getAllCities(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCity, tvCity)
            }else {
                Toast.makeText(this@FilterActivity, R.string.no_country_selected, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onClickDoneFilter() = with(binding){ //ф-ция приминения фильтра
        btFilter.setOnClickListener {
            val intent = Intent().apply {
                putExtra(FILTER_KEY, createFilter()) //записываем данные
            }
            setResult(RESULT_OK, intent) //отправляем данные
            finish()
        }
    }

    private fun onClickClear() = with(binding){ //ф-ция приминения фильтра
        btClear.setOnClickListener {
            tvCountry.text = getString(R.string.select_country)
            tvCity.text = getString(R.string.select_city)
            edIndex.setText("")
            checkBoxWithSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }

    private fun createFilter(): String = with(binding){//ф-ция создания фильтра
        val sBuilder = StringBuilder()
        val arrayTempFilter = listOf(tvCountry.text,
            tvCity.text,
            edIndex.text,
            checkBoxWithSend.isChecked.toString()// создаём массив, в который будем записывать данные
        )
        for((i, s) in arrayTempFilter.withIndex()) {//здесь поочереди записываем в наш массив данные
            if(s != getString(R.string.select_country) && s != getString(R.string.select_city) && s.isNotEmpty()){
                sBuilder.append(s)//если условие выполнилось тогда записываем данные
                if(i != arrayTempFilter.size - 1)sBuilder.append("_")//если не равен числу 3 добавляем нижнее подчеркивание
            }else{
                sBuilder.append("empty")//если что то не заполнили, то будет записываться empty
                if(i != arrayTempFilter.size - 1)sBuilder.append("_")
            }
        }
        return sBuilder.toString()//возвращает готовый стринг
    }

    fun actionBarSettings(){
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    companion object{
        const val  FILTER_KEY = "filter_key"
    }
}