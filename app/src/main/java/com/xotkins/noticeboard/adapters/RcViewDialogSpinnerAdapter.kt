package com.xotkins.noticeboard.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xotkins.noticeboard.R


class RcViewDialogSpinnerAdapter(var tvSelection: TextView, var dialog: AlertDialog): RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {
    private val mainList = ArrayList<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder { //рисуется разметка(сколько элементов будет, столько раз запустится)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item, parent, false)
        return SpViewHolder(view, tvSelection, dialog)
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.setData(mainList[position])
    }

    override fun getItemCount(): Int { //сколько будет рисовать элементов
        return mainList.size
    }

    class SpViewHolder(itemView: View, var tvSelection: TextView, var dialog: AlertDialog) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var itemText = ""
        fun setData(text: String){
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem)
            tvSpItem.text = text
            itemText = text
            itemView.setOnClickListener (this)
        }
        override fun onClick(view: View?) { //функиця для выбора страны из списка и отображается на tvCountry
            tvSelection.text = itemText
            dialog.dismiss()
        }
    }

    fun updateAdapter(list: ArrayList<String>){ //функция для обновления списка
        mainList.clear() //очищаем список
        mainList.addAll(list) //загружаем новый список
        notifyDataSetChanged() //указываем что данные изменились
    }
}