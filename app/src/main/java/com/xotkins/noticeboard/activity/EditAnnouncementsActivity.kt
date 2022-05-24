package com.xotkins.noticeboard.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.fxn.utility.PermUtil
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.adapters.ImageAdapter
import com.xotkins.noticeboard.databinding.ActivityEditAnnouncementsBinding
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.model.DatabaseManager
import com.xotkins.noticeboard.dialogs.DialogSpinnerHelper
import com.xotkins.noticeboard.interfaces.FragmentCloseInterface
import com.xotkins.noticeboard.fragments.ImageListFragment
import com.xotkins.noticeboard.utils.CityHelper
import com.xotkins.noticeboard.utils.ImagePicker


class EditAnnouncementsActivity : AppCompatActivity(), FragmentCloseInterface {
    private val databaseManager = DatabaseManager()
    lateinit var binding: ActivityEditAnnouncementsBinding
    private val dialog = DialogSpinnerHelper()
    private var isImagesPermissionGranted = false
    lateinit var imageAdapter: ImageAdapter
    var chooseImageFragment: ImageListFragment? = null
    var editImagePosition = 0
    var launcherMultiSelectImage: ActivityResultLauncher<Intent>? = null
    var launcherSingleSelectImage: ActivityResultLauncher<Intent>? = null
    private var isEditState = false
    private var announcement: Announcement? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
    }

    private fun checkEditState(){//ф-ция для проверки
        if(isEditState()){
            isEditState = true
            announcement = intent.getSerializableExtra(MainActivity.ANNOUNCEMENTS_DATA) as Announcement//если редактирование(true),то запускаем ф-цию fillViews
            if(announcement != null)fillViews(announcement!!)
        }
    }

    private fun isEditState(): Boolean{//ф-ция для проверки состояния
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false) //если мы получаем false, то мы нажали кнопку создание нового объявление,
                                                                                // если true, то нажали кнопку для редактирования и заполняем данные которые уже были в объявлении
    }

    private fun fillViews(announcement: Announcement) = with(binding){//ф-ция для заполнения данных, если нажали кнопку редактирование объявления
        tvCountry.text = announcement.country
        tvCity.text = announcement.city
        edIndex.setText(announcement.index)
        edNumber.setText(announcement.number)
        checkBoxWithSend.isChecked = announcement.withSend.toBoolean()
        tvCategory.text = announcement.category
        edTitle.setText(announcement.title)
        edPrice.setText(announcement.price)
        edDescription.setText(announcement.description)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  //  ImagePicker.getImage(this, 5, ImagePicker.REQUEST_CODE_GET_IMAGES)
                } else {
                    Toast.makeText(this,"Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun init(){
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
        launcherMultiSelectImage = ImagePicker.getLauncherMultiSelectImages(this) //ф-ция находится в ImagePicker
        launcherSingleSelectImage = ImagePicker.getLauncherForSingleImage(this) //ф-ция находится в ImagePicker
    }

    //OnClicks
    fun onClickSelectCountry(view: View){ //ф-ция для выбора страны
        val listCountry = CityHelper.getAllCountries(this)
        dialog.showSpinnerDialog(this, listCountry, binding.tvCountry)
        if(binding.tvCity.text.toString() != getString(R.string.select_city)){
            binding.tvCity.text = getString(R.string.select_city)
        }
    }

    fun onClickSelectCity(view: View){ //ф-ция для выбора города
        val selectedCountry = binding.tvCountry.text.toString()
        if(selectedCountry != getString(R.string.select_country)){
            val listCity = CityHelper.getAllCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, binding.tvCity)
        }else {
            Toast.makeText(this, R.string.no_country_selected, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCategory(view: View){//ф-ция выбора категории
        val listCategory = resources.getStringArray(R.array.category).toMutableList() as ArrayList //массив превращаем в ArrayList
        dialog.showSpinnerDialog(this, listCategory, binding.tvCategory)
    }

    fun onClickGetImages(view: View) { //запуск ф-ции для картинок
        if(imageAdapter.mainArray.size == 0){//если картинок нет, то открывается окно для добавления
            ImagePicker.launcher(this, launcherMultiSelectImage, 3)
        }else{
            openChooseImageFragment(null) //для редактирования
            chooseImageFragment?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View){ //ф-ция опубликовать объявление
        val announcementTemp = fillAnnouncement()
        if(isEditState){
            databaseManager.publishAnnouncement(announcementTemp.copy(key = announcement?.key), onPublishFinish())
        }else{
            databaseManager.publishAnnouncement(announcementTemp, onPublishFinish())
        }
    }

    private fun onPublishFinish(): DatabaseManager.FinishWorkListener{ //ф-ция для запуска интерфейса
        return object: DatabaseManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }
        }
    }

    private fun fillAnnouncement(): Announcement{ //ф-ция для заполнения в базу данных для публикации
        val announcement: Announcement
        binding.apply {
            announcement = Announcement(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                edIndex.text.toString(),
                edNumber.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCategory.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                databaseManager.database.push().key,
                databaseManager.authentication.uid,
               "0")
        }
        return announcement
    }

    override fun onFragmentClose(list: ArrayList<Bitmap>) { //ф-ция закрытия фрагмента и обновление
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFragment = null
    }

    fun openChooseImageFragment(newList: ArrayList<String>?){//ф-ция для запуска фрагмента
        chooseImageFragment = ImageListFragment(this, newList)
        binding.scrollViewMain.visibility = View.GONE
        val fragmentManager = supportFragmentManager.beginTransaction()
        fragmentManager.replace(R.id.place_holder, chooseImageFragment!!)
        fragmentManager.commit()
    }
}