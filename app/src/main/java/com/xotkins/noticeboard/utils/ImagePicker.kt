package com.xotkins.noticeboard.utils



import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.activity.EditAnnouncementsActivity
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


object ImagePicker {
    const val MAX_IMAGE_CONST = 3
    var job: Job? = null

    private fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun getMultiImages(edAct: EditAnnouncementsActivity, imageCounter: Int) { //ф-ция для добавления нескольких картинок
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)){result ->
            when(result.status){
                PixEventCallback.Status.SUCCESS ->{
                    getMultiSelectedImages(edAct, result.data)
                    closePixFragment(edAct)
                }
            }
        }
    }

    fun addImages(edAct: EditAnnouncementsActivity, imageCounter: Int) { //ф-ция для добавления картинок, когда уже 1 есть
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)){result ->
            when(result.status){
                PixEventCallback.Status.SUCCESS ->{
                    openChooseImageFragment(edAct)
                    edAct.chooseImageFragment?.updateAdapter(result.data as ArrayList<Uri>, edAct)
                }
            }
        }
    }

    fun getSingleImage(edAct: EditAnnouncementsActivity) { //ф-ция для добавления одной картинки
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)){result ->
            when(result.status){
                PixEventCallback.Status.SUCCESS ->{
                    openChooseImageFragment(edAct)
                    singleImage(edAct, result.data[0])
                }
            }
        }
    }

    private fun openChooseImageFragment(edAct: EditAnnouncementsActivity){
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder, edAct.chooseImageFragment!!).commit()
    }

    private fun closePixFragment(edAct: EditAnnouncementsActivity){
        val flist = edAct.supportFragmentManager.fragments
                flist.forEach {
                    if(it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    fun getMultiSelectedImages(edAct: EditAnnouncementsActivity, uris: List<Uri>, ) { //ф-ция слушатель
        if (uris.size!! > 1 && edAct.chooseImageFragment == null) {//добавляем больше одной картинки
            edAct.openChooseImageFragment(uris as ArrayList<Uri>)
        } else if (uris.size == 1 && edAct.chooseImageFragment == null) { //добавляем 1 картинку
            job = CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.pBarLoad.visibility = View.VISIBLE// делаем видимым
                val bitmapList = ImageManager.imageResize(uris, edAct)//загружается картинка
                edAct.binding.pBarLoad.visibility = View.GONE//как загрузилась картинка делаем невидимым
                edAct.imageAdapter.update(bitmapList as ArrayList<Bitmap>)//обновляем адаптер
                closePixFragment(edAct)
            }
        }
    }




    private fun singleImage(edAct: EditAnnouncementsActivity, uri: Uri){//ф-ция слушатель
        edAct.chooseImageFragment?.setSingleImage(uri, edAct.editImagePosition)

    }
}
