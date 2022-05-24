package com.xotkins.noticeboard.utils



import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import com.xotkins.noticeboard.activity.EditAnnouncementsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


object ImagePicker {
    const val MAX_IMAGE_CONST = 3
    var job: Job? = null

    private fun getOptions(imageCounter: Int): Options {
        val options = Options.init()
            .setCount(imageCounter)                                        //Number of images to restict selection count
            .setFrontfacing(false)                                         //Front Facing camera on start
            .setMode(Options.Mode.Picture)                                 //Option to select only pictures or videos or both
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
            .setPath("/pix/images")                                        //Custom Path For media Storage
        return options
    }

    fun launcher(edAct: EditAnnouncementsActivity, launcher: ActivityResultLauncher<Intent>?, imageCounter: Int) { //принимает данные с EditAdsActivity
        PermUtil.checkForCamaraWritePermissions(edAct) {
            val intent = Intent(edAct, Pix::class.java).apply {
                putExtra("options", getOptions(imageCounter)) //передаём параметры, которая требует библиотека Pix
    }
            launcher?.launch(intent)
        }
    }

    fun getLauncherMultiSelectImages(edAct: EditAnnouncementsActivity): ActivityResultLauncher<Intent> { //ф-ция слушатель
        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    if (returnValues?.size!! > 1 && edAct.chooseImageFragment == null) {//добавляем больше одной картинки
                        edAct.openChooseImageFragment(returnValues)
                    } else if (returnValues.size == 1 && edAct.chooseImageFragment == null) { //добавляем 1 картинку
                        job = CoroutineScope(Dispatchers.Main).launch {
                            edAct.binding.pBarLoad.visibility = View.VISIBLE// делаем видимым
                            val bitmapList = ImageManager.imageResize(returnValues)//загружается картинка
                            edAct.binding.pBarLoad.visibility = View.GONE//как загрузилась картинка делаем невидимым
                            edAct.imageAdapter.update(bitmapList as ArrayList<Bitmap>)//обновляем адаптер
                        }
                    } else if (edAct.chooseImageFragment != null) {
                        edAct.chooseImageFragment?.updateAdapter(returnValues)
                    }
                }
            }
        }
    }

    fun getLauncherForSingleImage(edAct: EditAnnouncementsActivity): ActivityResultLauncher<Intent> {//ф-ция слушатель
        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val uris = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    edAct.chooseImageFragment?.setSingleImage(uris?.get(0)!!, edAct.editImagePosition)
                }
            }
        }
    }
}