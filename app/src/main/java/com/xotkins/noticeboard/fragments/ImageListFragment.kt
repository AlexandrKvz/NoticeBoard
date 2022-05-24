package com.xotkins.noticeboard.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.activity.EditAnnouncementsActivity
import com.xotkins.noticeboard.adapters.SelectImageRvAdapter
import com.xotkins.noticeboard.databinding.ListImageFragmentBinding
import com.xotkins.noticeboard.dialoghelper.ProgressDialog
import com.xotkins.noticeboard.interfaces.AdapterCallback
import com.xotkins.noticeboard.interfaces.FragmentCloseInterface
import com.xotkins.noticeboard.utils.ImageManager
import com.xotkins.noticeboard.utils.ImagePicker
import com.xotkins.noticeboard.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFragment(private val fragCloseInterface: FragmentCloseInterface): BaseAdsFragment(), AdapterCallback {
    val adapter = SelectImageRvAdapter(this)
    private val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private  var job: Job? = null
    private var addImageItem: MenuItem? = null
    lateinit var binding: ListImageFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ListImageFragmentBinding.inflate(layoutInflater)
        adView = binding.adView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        binding.apply {
            touchHelper.attachToRecyclerView(rcViewSelectImage)
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
            rcViewSelectImage.adapter = adapter
        }
    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){ //ф-ция для обновления
        adapter.updateAdapter(bitmapList, true)//передаём данные для картинок
    }

    override fun onDetach() {
        super.onDetach()
        fragCloseInterface.onFragmentClose(adapter.mainArray)
        job?.cancel() // закрывает крутину, если мы вышли с фрагмента
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    fun resizeSelectedImages(newList: ArrayList<Uri>, needClear: Boolean, activity: Activity){
        job = CoroutineScope(Dispatchers.Main).launch { //создаём крутину и запускаем её на основном потоке, чтобы можно было запустить ф-цию fun imageResize в фоновом потоке, но нужно обязательно закрывать её
            val dialog = ProgressDialog.createProgressDialog(activity)//открывается прогресс бар
            val bitmapList = ImageManager.imageResize(newList, activity)// загружаются картинки
            dialog.dismiss()//как картинки нарисовались, прогресс бар закрывается
            adapter.updateAdapter(bitmapList, needClear)//передаём данные для картинок
            if(adapter.mainArray.size > 2) addImageItem?.isVisible = false //если картинок больше 2 прячем кнопку
        }
    }

    private fun setUpToolBar() {
        binding.apply {
            tb.inflateMenu(R.menu.menu_choose_image)
            val deleteItem = tb.menu.findItem(R.id.delete_image)
            addImageItem = tb.menu.findItem(R.id.add_image)
            if(adapter.mainArray.size > 2) addImageItem?.isVisible = false //если картинок больше 2 прячем кнопку
            tb.setNavigationOnClickListener {
                showInterAd()
            }
            deleteItem.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                addImageItem?.isVisible = true
                true
            }
            addImageItem?.setOnMenuItemClickListener {
                val imageCount =
                    ImagePicker.MAX_IMAGE_CONST - adapter.mainArray.size //указываем что добавить картинок можно только до 5
                ImagePicker.addImages(activity as EditAnnouncementsActivity, imageCount)
                true
            }
        }
    }

    fun updateAdapter(newList: ArrayList<Uri>, activity: Activity){ //ф-ция для обновления адаптера(обновления картинок после редактирования)
        resizeSelectedImages(newList, false, activity)
    }

    fun setSingleImage(uri: Uri, position: Int){ //ф-ция выбираем элемент при редактировании его и перезаписываем на новый
        val pBar = binding.rcViewSelectImage[position].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch { //создаём крутину и запускаем её на основном потоке, чтобы можно было запустить ф-цию fun imageResize в фоновом потоке, но нужно обязательно закрывать её
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity)
            pBar.visibility = View.GONE
            adapter.mainArray[position] = bitmapList[0]
            adapter.notifyItemChanged(position)
            }
        }
    }
