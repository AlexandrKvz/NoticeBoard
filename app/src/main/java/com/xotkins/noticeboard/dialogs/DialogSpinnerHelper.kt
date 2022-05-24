package com.xotkins.noticeboard.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.adapters.RcViewDialogSpinnerAdapter
import com.xotkins.noticeboard.utils.CityHelper

class DialogSpinnerHelper {

    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView){
        val builder = AlertDialog.Builder(context) //создаём диалог
        val dialog = builder.create()
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null) //надуваем разметку
        val adapter = RcViewDialogSpinnerAdapter( tvSelection, dialog) //создаём адаптер из класса RcViewDialogSpinner()
        val rcView = rootView.findViewById<RecyclerView>(R.id.rcSpView) //находим элемент rcSpView
        val rcSearchView = rootView.findViewById<SearchView>(R.id.svSpinner) //находим элемент rcSpView
        rcView.layoutManager = LinearLayoutManager(context) //указываем разметку списком
        rcView.adapter = adapter //подключаем адаптер
        dialog.setView(rootView)
        adapter.updateAdapter(list)
        setSearchView(adapter, list, rcSearchView)
        dialog.show()

    }

    private fun setSearchView(adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, rcSearchView: SearchView?) {
        rcSearchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = CityHelper.filterListData(list, newText)
                adapter.updateAdapter(tempList)
                return true
            }
        })
    }


}