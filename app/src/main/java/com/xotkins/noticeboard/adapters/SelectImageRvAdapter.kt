package com.xotkins.noticeboard.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.activity.EditAnnouncementsActivity
import com.xotkins.noticeboard.databinding.SelectImageFragItemBinding
import com.xotkins.noticeboard.interfaces.AdapterCallback
import com.xotkins.noticeboard.utils.ImageManager
import com.xotkins.noticeboard.utils.ImagePicker
import com.xotkins.noticeboard.utils.ItemTouchMoveCallback

class SelectImageRvAdapter(val adapterCallback: AdapterCallback): RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val binding = SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(binding, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPosition: Int, targetPosition: Int) {//ф-ция для движения элемента
        val targetItem = mainArray[targetPosition]
        mainArray[targetPosition] = mainArray[startPosition] //так же меняем позицию элементаи в массиве и в holder
        mainArray[startPosition] = targetItem //тут возвращаем элемент на старт позицию, который перетащили
        notifyItemMoved(startPosition, targetPosition)

    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(private val binding: SelectImageFragItemBinding, val context: Context, val adapter: SelectImageRvAdapter) : RecyclerView.ViewHolder(binding.root) { //ф-ция находим наши итемы и заполняем


        fun setData(bitMap: Bitmap){
            binding.imEditImage.setOnClickListener {
               ImagePicker.getSingleImage(context as EditAnnouncementsActivity)
                context.editImagePosition = adapterPosition
            }
            binding.imDeleteImage.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for(n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n) //цикл для сохранения анимация при удалении
                adapter.adapterCallback.onItemDelete() //ф-ция для появления кнопки после удаления одной картинки 5-1 = 4 кнопка появилась
            }
            binding.tvTitle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition]
            ImageManager.chooseScaleType(binding.imageContent, bitMap) //запускаем ф-цию для выбора centralScrop
            binding.imageContent.setImageBitmap(bitMap)
        }
    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean){
        if(needClear)mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }


}