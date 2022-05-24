package com.xotkins.noticeboard.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.xotkins.noticeboard.R

class ImageAdapter: RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    val mainArray = ArrayList<Bitmap>() //создаём список


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {//ф-ция для отображения
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int { //ф-ция, передаём размер нашего массива, т.е. сколько он будет отображать
        return  mainArray.size
    }


    class ImageHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var imItem: ImageView

        fun setData(bitmap: Bitmap){ //находим наш imItem
            imItem = itemView.findViewById(R.id.imItem)
            imItem.setImageBitmap(bitmap)
        }
    }

    fun update(newList: ArrayList<Bitmap>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }
}