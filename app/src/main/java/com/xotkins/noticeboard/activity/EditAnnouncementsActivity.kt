package com.xotkins.noticeboard.activity


import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.OnCompleteListener
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.adapters.ImageAdapter
import com.xotkins.noticeboard.databinding.ActivityEditAnnouncementsBinding
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.model.DatabaseManager
import com.xotkins.noticeboard.dialogs.DialogSpinnerHelper
import com.xotkins.noticeboard.interfaces.FragmentCloseInterface
import com.xotkins.noticeboard.fragments.ImageListFragment
import com.xotkins.noticeboard.utils.CityHelper
import com.xotkins.noticeboard.utils.ImageManager
import com.xotkins.noticeboard.utils.ImagePicker
import java.io.ByteArrayOutputStream


open class EditAnnouncementsActivity : AppCompatActivity(), FragmentCloseInterface {
    private val databaseManager = DatabaseManager()
    lateinit var binding: ActivityEditAnnouncementsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    var chooseImageFragment: ImageListFragment? = null
    var editImagePosition = 0
    private var imageIndex = 0
    private var isEditState = false
    private var announcement: Announcement? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        imageChangeCounter()
    }

    private fun checkEditState() {//ф-ция для проверки
        if (isEditState()) {
            isEditState = true
            announcement = intent.getSerializableExtra(MainActivity.ANNOUNCEMENTS_DATA) as Announcement//если редактирование(true),то запускаем ф-цию fillViews
            if (announcement != null) fillViews(announcement!!)
        }
    }

    private fun isEditState(): Boolean {//ф-ция для проверки состояния
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false) //если мы получаем false, то мы нажали кнопку создание нового объявление,
        // если true, то нажали кнопку для редактирования и заполняем данные которые уже были в объявлении
    }

    private fun fillViews(announcement: Announcement) = with(binding) {//ф-ция для заполнения данных, если нажали кнопку редактирование объявления
            tvCountry.text = announcement.country
            tvCity.text = announcement.city
            edIndex.setText(announcement.index)
            edNumber.setText(announcement.number)
            edEmail.setText(announcement.email)
            checkBoxWithSend.isChecked = announcement.withSend.toBoolean()
            tvCategory.text = announcement.category
            edTitle.setText(announcement.title)
            edPrice.setText(announcement.price)
            edDescription.setText(announcement.description)
            ImageManager.fillImageArray(announcement, imageAdapter)
            updateImageCounter(0)
        }

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
        updateImageCounter(binding.vpImages.currentItem)

    }

    //OnClicks
    fun onClickSelectCountry(view: View) { //ф-ция для выбора страны
        val listCountry = CityHelper.getAllCountries(this)
        dialog.showSpinnerDialog(this, listCountry, binding.tvCountry)
        if (binding.tvCity.text.toString() != getString(R.string.select_city)) {
            binding.tvCity.text = getString(R.string.select_city)
        }
    }

    fun onClickSelectCity(view: View) { //ф-ция для выбора города
        val selectedCountry = binding.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.select_country)) {
            val listCity = CityHelper.getAllCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, binding.tvCity)
        } else {
            Toast.makeText(this, R.string.no_country_selected, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCategory(view: View) {//ф-ция выбора категории
        val listCategory = resources.getStringArray(R.array.category)
            .toMutableList() as ArrayList //массив превращаем в ArrayList
        dialog.showSpinnerDialog(this, listCategory, binding.tvCategory)
    }

    fun onClickGetImages(view: View) { //запуск ф-ции для картинок
        if (imageAdapter.mainArray.size == 0) {//если картинок нет, то открывается окно для добавления
            ImagePicker.getMultiImages(this, 3)
        } else {
            openChooseImageFragment(null) //для редактирования
            chooseImageFragment?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View) { //ф-ция опубликовать объявление
        if(isFieldsEmpty()){
            showToast("Все поля должны быть заполнены!")
            return
        }
        binding.progressLayout.visibility = View.VISIBLE
        announcement = fillAnnouncement()
            uploadImages()
    }

    private fun isFieldsEmpty(): Boolean = with(binding){//ф-ция для пустых полей, чтобы не дать опубликовать пустое объявления (без заполнения)
        return tvCountry.text.toString() == getString(R.string.select_country)
                || tvCity.text.toString() == getString(R.string.select_city)
                || tvCategory.text.toString() == getString(R.string.select_category)
                || edTitle.text.isEmpty()
                || edEmail.text.isEmpty()
                || edNumber.text.isEmpty()
                || edPrice.text.isEmpty()
                || edDescription.text.isEmpty()
                || edIndex.text.isEmpty()
    }

    private fun onPublishFinish(): DatabaseManager.FinishWorkListener{ //ф-ция для запуска интерфейса
        return object: DatabaseManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {//если загрузка успешно то прогрессБар закроется, если не успешно то прогрессБар всё равно закроется
                binding.progressLayout.visibility = View.GONE
                if(isDone)finish()
            }
        }
    }

    private fun fillAnnouncement(): Announcement{ //ф-ция для заполнения в базу данных для публикации
        val announcementTemp: Announcement
        binding.apply {
            announcementTemp = Announcement(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                edIndex.text.toString(),
                edNumber.text.toString(),
                edEmail.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCategory.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                announcement?.image1 ?: "empty", //при редактировании возвращает фото
                announcement?.image2 ?: "empty",
                announcement?.image3 ?: "empty",
                announcement?.key ?: databaseManager.database.push().key, //используется ключ для редактирование, чтобы не создавалось новая копия
                databaseManager.authentication.uid,
                announcement?.time ?: System.currentTimeMillis().toString(),
               "0")

        }
        return announcementTemp
    }

    override fun onFragmentClose(list: ArrayList<Bitmap>) { //ф-ция закрытия фрагмента и обновление
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFragment = null
        updateImageCounter(binding.vpImages.currentItem)
    }

    fun openChooseImageFragment(newList: ArrayList<Uri>?){//ф-ция для запуска фрагмента
        chooseImageFragment = ImageListFragment(this)
        if(newList != null)chooseImageFragment?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fragmentManager = supportFragmentManager.beginTransaction()
        fragmentManager.replace(R.id.place_holder, chooseImageFragment!!)
        fragmentManager.commit()
    }

    private fun uploadImages() { //ф-ция для загрузки в Storage и публикации объявления / ф-ция запускается столько раз сколько есть картинок
        if (imageIndex == 3) { //если индекст равен 3, то загружается объявление с текстом, если нет то подгружает сначала картинки до равному индексу и после публикует
            databaseManager.publishAnnouncement(announcement!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAnnouncement() //здесь записывается урл старой картинки, если она была, если нет , то пустота
        if (imageAdapter.mainArray.size > imageIndex) {//если размер массива больше индекса
            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex]) //загружаем картинки
            if(oldUrl.startsWith("http")){ //если начинается ссылка с http значит ссылка
                updateImage(byteArray, oldUrl){//то изменяем картинку
                    nextImage(it.result.toString())
                }
            }else{
                uploadImage(byteArray) {//загружаем новую картинку
                    // databaseManager.publishAnnouncement(announcement!!, onPublishFinish())
                    nextImage(it.result.toString())
                }
            }
        } else {// это часть для удаления
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    private fun nextImage(uri: String){//ф-ция для перебора картинок
        setImageUriToAnnouncement(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAnnouncement(uri: String){//ф-ция для проверки картинок
        when(imageIndex){
            0 -> announcement = announcement?.copy(image1 = uri) //копируем объявление и записываем его
            1 -> announcement = announcement?.copy(image2 = uri) //копируем объявление и записываем его
            2 -> announcement = announcement?.copy(image3 = uri) //копируем объявление и записываем его
        }
    }

    private fun getUrlFromAnnouncement(): String{ //ф-ция чтобы взять поочереди сыллку и индекс
        return listOf(announcement?.image1!!, announcement?.image2!!, announcement?.image3!!)[imageIndex] //создаём список из ссылок, которые были и у узнаем на какой позиции ссылка
    }

    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray{//ф-ция для того чтобы поочереди картинки превращать в byteArray
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream) //тут сжимаем
        return outStream.toByteArray() //превращаем в byteArray
    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>){ //ф-ция для того чтобы поочереди загружать картинки
        val imStorageRef = databaseManager.databaseStorage
            .child(databaseManager.authentication.uid!!)
            .child("image_${System.currentTimeMillis()}") //указываем куда будем записывать картинки
        val upTask = imStorageRef.putBytes(byteArray)//здесь мы получаем ссылку
        upTask.continueWithTask{
            task -> imStorageRef.downloadUrl //здесь мы скачиваем ссылку, которую получили
        }.addOnCompleteListener(listener)//делаем addOnCompleteListener интерфейсом, чтобы запустить её в uploadImages()
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>){//ф-ция для удаления картинок из Storage
        databaseManager.databaseStorage.storage.getReferenceFromUrl(oldUrl).delete().addOnCompleteListener(listener) //здесь мы берём ссылку с Storage, которую хотим удалить

    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>){ //ф-ция для обновления картинок
        val imStorageRef = databaseManager.databaseStorage.storage.getReferenceFromUrl(url) //здесь мы берём ссылку с Storage
        val upTask = imStorageRef.putBytes(byteArray)//здесь мы получаем ссылку
        upTask.continueWithTask{
                task -> imStorageRef.downloadUrl //здесь мы скачиваем ссылку, которую получили
        }.addOnCompleteListener(listener)//делаем addOnCompleteListener интерфейсом, чтобы запустить её в uploadImages()
    }

    private fun imageChangeCounter(){//счётчик для viewPager
        binding.vpImages.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    fun updateImageCounter(counter: Int){//ф-ция обновления счётчика
        var index = 1
        val itemCounter = binding.vpImages.adapter?.itemCount
        if(itemCounter == 0) index = 0
        val imageCounter = "${counter + index}/$itemCounter" //здесь показываем на какой позиции картинка, например 1/3, 2/3, 3/3
        binding.tvImageCounter.text = imageCounter //тут показываем наш счётчик на данной позиции
    }
}