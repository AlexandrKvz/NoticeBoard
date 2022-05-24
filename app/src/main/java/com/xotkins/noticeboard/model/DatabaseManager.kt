package com.xotkins.noticeboard.model

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DatabaseManager {
    val database = Firebase.database.getReference(MAIN_NODE)//главный ключ
    val authentication = Firebase.auth

    fun publishAnnouncement(announcement: Announcement, finishWorkListener: FinishWorkListener){ //идёт запись в базу данных
       if(authentication.uid != null)database.child(announcement.key ?: "empty")
           .child(authentication.uid!!).child(ANNOUNCEMENT_NODE)
           .setValue(announcement).addOnCompleteListener {
               finishWorkListener.onFinish() //слушать чтобы закрыть окно реадктирования, после загрузки данных в базу данных
           }
    }

    fun announcementViewed(announcement: Announcement) {//ф-ция счётчика просмотра объявления
        var counter = announcement.viewsCounter.toInt() //создаём переменную куда записываем значения
        counter++ //увеличиваем счётчик на 1
        if (authentication.uid != null) database.child(announcement.key ?: "empty")
            .child(INFO_NODE).setValue(InfoItem(counter.toString(), announcement.emailsCounter, announcement.callsCounter)) //записываем данные счётчиков
    }

    fun onFavClick(announcement: Announcement, listener: FinishWorkListener){//ф-ция для отслеживания добавляем или удаляем
        if(announcement.isFav){
            removeToFavs(announcement, listener)
        }else{
            addToFavs(announcement, listener)
        }
    }

    private fun addToFavs(announcement: Announcement, listener: FinishWorkListener){//ф-ция для записывания в избранных, создаём путь и записываем
        announcement.key?.let {
            authentication.uid?.let {
                    uid -> database.child(it)
                .child(FAVS_NODE)
                .child(uid)
                .setValue(uid).addOnCompleteListener {
                        if(it.isSuccessful) listener.onFinish()
            } }
        }
    }

    private fun removeToFavs(announcement: Announcement, listener: FinishWorkListener){//ф-ция для удаления избранных, создаём путь и записываем
        announcement.key?.let {
            authentication.uid?.let {
                    uid -> database.child(it)
                .child(FAVS_NODE)
                .child(uid)
                .removeValue().addOnCompleteListener {
                    if(it.isSuccessful) listener.onFinish()
                }
            }
        }
    }

    fun getMyFavs(readDataCallback: ReadDataCallback?){//ф-ция для фильтрации мои объявления
        val query = database.orderByChild("/favs/${authentication.uid}").equalTo(authentication.uid)
        readDataFromDatabase(query, readDataCallback)
    }

    fun getMyAnnouncements(readDataCallback: ReadDataCallback?){//ф-ция для фильтрации мои объявления
        val query = database.orderByChild(authentication.uid + "/announcement/uid").equalTo(authentication.uid)
        readDataFromDatabase(query, readDataCallback)
    }

    fun getAllAnnouncements(readDataCallback: ReadDataCallback?){//ф-ция для фильтрации всех объявления
        val query = database.orderByChild(authentication.uid + "/announcement/price")
        readDataFromDatabase(query, readDataCallback)
    }

    fun deleteAnnouncement(announcement: Announcement, listener: FinishWorkListener){//ф-ция для удаления объявления
        if(announcement.key == null || announcement.uid == null) return
        database.child(announcement.key).child(announcement.uid).removeValue().addOnCompleteListener {
            listener.onFinish()
        }
    }

    private fun readDataFromDatabase(query: Query, readDataCallback: ReadDataCallback?){//ф-ция для считывания с базы данных
        query.addListenerForSingleValueEvent(object: ValueEventListener{ //делаем обновления базы данных не в реальном времени, а когда нажимаем слушатель
            val announcementArray = ArrayList<Announcement>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for(item in snapshot.children) { //создаём цикл, где будет перебирать все узлы в ключе main -> ....
                    var announcement: Announcement? = null
                    item.children.forEach { //второй цикл пробегает все узлы в ключе main -> индетификатор объявления -> .....
                        if(announcement == null) announcement = it.child(ANNOUNCEMENT_NODE).getValue(Announcement::class.java) //запись объявления
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java) //запись в ключ info
                    val favCounter = item.child(FAVS_NODE).childrenCount //запись в ключ favs
                    val isFav = authentication.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) } //здесь показывает тру или фалс, добавлено или нет в избранные
                    announcement?.isFav = isFav != null
                    announcement?.favCounter = favCounter.toString() //записываем значение счётчика
                    announcement?.viewsCounter = infoItem?.viewsCounter ?: "0" //записываем изначальное значение 0
                    announcement?.emailsCounter = infoItem?.emailsCounter ?: "0"//записываем изначальное значение 0
                    announcement?.callsCounter = infoItem?.callsCounter ?: "0"//записываем изначальное значение 0
                    if(announcement != null)announcementArray.add(announcement!!)
                }
                readDataCallback?.readData(announcementArray) //интерфейс для считывания
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Announcement>)
    }

    interface FinishWorkListener{
        fun onFinish()
    }

    companion object{
        const val ANNOUNCEMENT_NODE = "announcement"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
    }
}