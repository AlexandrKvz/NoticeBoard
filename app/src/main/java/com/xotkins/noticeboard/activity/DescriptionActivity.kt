package com.xotkins.noticeboard.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.adapters.ImageAdapter
import com.xotkins.noticeboard.constants.ConstIntentMainActivity
import com.xotkins.noticeboard.databinding.ActivityDescriptionBinding
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    private var announcement: Announcement? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.fbNumber.setOnClickListener {
            call()
        }
        binding.fbEmail.setOnClickListener {
            sendEmail()
        }
    }

    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMainActivity()
        imageChangeCounter()
    }

    private fun updateUI(announcement: Announcement){ //обновляем данные и экран
        ImageManager.fillImageArray(announcement, adapter)
        fillTextViews(announcement)
    }

    private fun fillTextViews(announcement: Announcement) = with(binding){//ф-ция для заполнения всех текстов
        tvTitle.text = announcement.title
        tvDescription.text = announcement.description
        tvPrice.text = announcement.price
        tvNumber.text = announcement.number
        tvEmail.text = announcement.email
        tvCountry.text = announcement.country
        tvCity.text = announcement.city
        tvIndex.text = announcement.index
        tvWithSend.text = isWithSend(announcement.withSend.toBoolean())

    }

    private fun isWithSend(withSend: Boolean): String {
        return if(withSend) getString(R.string.yes) else getString(R.string.yes)
    }

    private fun getIntentFromMainActivity(){//здесь получаем данные с класса Announcement
        announcement = intent.getSerializableExtra(ConstIntentMainActivity.INTENT_MAIN_ANNOUNCEMENT) as Announcement //получаем данные и записываем в переменную
        if(announcement != null)updateUI(announcement!!)
    }

    private fun call(){ //ф-ция для звонка
        val callUri = "tel:${announcement?.number}" //превращаем номер телефона в uri
        val intentCall = Intent(Intent.ACTION_DIAL) //создаём интент, который будем отправлять
        intentCall.data = callUri.toUri() //помещаем этот uri
        startActivity(intentCall) //запускаем активити
    }

    private fun sendEmail(){//ф-ция для отправки сообщения по почте
        val intentEmail = Intent(Intent.ACTION_SEND) //создаём интент, который будем отправлять
        intentEmail.type = "message/rfc822" //спец код для всех случаев по отправке по почте
        intentEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(announcement?.email)) //указываем на какую почту отправляем
            putExtra(Intent.EXTRA_SUBJECT, "Объявление")//указываем заголовок
            putExtra(Intent.EXTRA_TEXT, "Меня интересует ваше объявление!")//указываем какой текст отправляем
        }
        try {
            startActivity(Intent.createChooser(intentEmail, "Открыть с"))
        }catch (e: ActivityNotFoundException){

        }
    }

    private fun imageChangeCounter(){//счётчик для viewPager
        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}" //здесь показываем на какой позиции картинка, например 1/3, 2/3, 3/3
                binding.tvImageCounter.text = imageCounter //тут показываем наш счётчик на данной позиции
            }
        })
    }
}