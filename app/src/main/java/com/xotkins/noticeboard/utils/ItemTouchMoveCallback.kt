package com.xotkins.noticeboard.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchMoveCallback(val adapter: ItemTouchAdapter): ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, ): Int { //ф-ция, как будет двигаться картинка
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlag, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder, ): Boolean { //запускается ф-ция при движении
        adapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
       return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {//ф-ция добавляет прозрачность элементу
        if(actionState != ItemTouchHelper.ACTION_STATE_IDLE)viewHolder?.itemView?.alpha = 0.5f
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {//возвращаем прозначность
        viewHolder.itemView.alpha = 1.0f
        adapter.onClear()
        super.clearView(recyclerView, viewHolder)

    }

    interface ItemTouchAdapter{
        fun onMove(startPosition: Int, targetPosition: Int)
        fun onClear()
    }
}