package com.xotkins.noticeboard.adapters

import androidx.recyclerview.widget.DiffUtil
import com.xotkins.noticeboard.model.Announcement


class DiffUtilHelper(val oldList: List<Announcement>, val newList: List<Announcement>): DiffUtil.Callback() {//хелпер для анимации удаления
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].key == newList[newItemPosition].key
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}