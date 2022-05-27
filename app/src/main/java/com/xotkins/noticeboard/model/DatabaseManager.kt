package com.xotkins.noticeboard.model

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xotkins.noticeboard.utils.FilterManager

class DatabaseManager {
    val database = Firebase.database.getReference(MAIN_NODE)//главный ключ базы данных
    val databaseStorage = Firebase.storage.getReference(MAIN_NODE)//главный ключ сторадж
    val authentication = Firebase.auth

    fun publishAnnouncement(announcement: Announcement, finishWorkListener: FinishWorkListener, ) { //идёт запись в базу данных под определенные ключи

        if (authentication.uid != null) database.child(announcement.key ?: "empty")
            .child(authentication.uid!!).child(ANNOUNCEMENT_NODE)
            .setValue(announcement).addOnCompleteListener {

                val announcementFilter = FilterManager.createFilter(announcement) //здесь заполняем фильтр
                database.child(announcement.key ?: "empty")
                    .child(FILTER_NODE).setValue(announcementFilter).addOnCompleteListener {
                        finishWorkListener.onFinish(it.isSuccessful) //слушатель чтобы закрыть окно реадктирования, после загрузки данных в базу данных
                    }
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
                        if(it.isSuccessful) listener.onFinish(true)
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
                    if(it.isSuccessful) listener.onFinish(true)
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

    fun getAllAnnouncementsFirstPage(filter: String, readDataCallback: ReadDataCallback?){//ф-ция для фильтрации всех объявления на первой странице
        val query = if(filter.isEmpty()) {
            database.orderByChild("announcementFilter/time").limitToLast(ANNOUNCEMENT_LIMIT)//показываем первую страницу всех объявлений
        }else {
            getAllAnnouncementsByFilterFirstPage(filter)
        }
        readDataFromDatabase(query, readDataCallback)
    }

    private fun getAllAnnouncementsByFilterFirstPage(tempFilter: String): Query{//ф-ция для фильтрации всех объявления на первой странице
        val orderBy = tempFilter.split("|")[0]//указываем с какой позиции разделяем наш фильтр
        val filter = tempFilter.split("|")[1]//указываем с какой позиции разделяем наш фильтр
        return database.orderByChild("announcementFilter/$orderBy")
            .startAfter(filter).endAt(filter + "\uf8ff").limitToLast(ANNOUNCEMENT_LIMIT) //
    }

    fun getAllAnnouncementsNextPage(time: String, filter: String, readDataCallback: ReadDataCallback?){//ф-ция для фильтрации всех объявления по времени слудущая страница, след 2
        if(filter.isEmpty()) {
            val query = database.orderByChild("announcementFilter/time").endBefore(time).limitToLast(ANNOUNCEMENT_LIMIT)
            readDataFromDatabase(query, readDataCallback)
        }else{
            getAllAnnouncementsByFilterNextPage(filter, time, readDataCallback)
        }
    }

    private fun getAllAnnouncementsByFilterNextPage(tempFilter: String, time: String, readDataCallback: ReadDataCallback?){//ф-ция для фильтрации всех объявления на след странице после скролла
        val orderBy = tempFilter.split("|")[0]//указываем с какой позиции разделяем наш фильтр
        val filter = tempFilter.split("|")[1]//указываем с какой позиции разделяем наш фильтр
        val query = database.orderByChild("announcementFilter/$orderBy")
            .endBefore(filter + "_$time").limitToLast(ANNOUNCEMENT_LIMIT) //
        readNextPageFromDatabase(query, filter, orderBy, readDataCallback)
    }

    fun getAllAnnouncementsFromCategoryFirstPage(category: String, filter: String, readDataCallback: ReadDataCallback?){//ф-ция для фильтрации всех объявления по категориям на первой странице
        val query = if(filter.isEmpty()) {
            database.orderByChild("announcementFilter/cat_time").startAfter(category).endAt(category + "_\uf8ff").limitToLast(ANNOUNCEMENT_LIMIT) //нужно взять объявления с категорией из категории
        }else{
            getAllAnnouncementsFromCategoryByFilterFirstPage(category, filter)
        }
        readDataFromDatabase(query, readDataCallback)
    }

    private fun getAllAnnouncementsFromCategoryByFilterFirstPage(category: String, tempFilter: String): Query{//ф-ция для фильтрации всех объявления по категориям на первой странице
        val orderBy = "cat_" + tempFilter.split("|")[0]//указываем с какой позиции разделяем наш фильтр
        val filter = category + "_" + tempFilter.split("|")[1]//указываем с какой позиции разделяем наш фильтр
        return database.orderByChild("announcementFilter/$orderBy")
            .startAfter(filter).endAt(filter + "\uf8ff").limitToLast(ANNOUNCEMENT_LIMIT) //
    }

    fun getAllAnnouncementsFromCategoryNextPage(category: String, time: String, filter: String, readDataCallback: ReadDataCallback?){//ф-ция для фильтрации всех объявления по категориям на след странице
        if(filter.isEmpty()){
            val query = database.orderByChild("announcementFilter/cat_time")
                .endBefore(category + "_" + time).limitToLast(ANNOUNCEMENT_LIMIT) //нужно взять объявления с категорией из категории
            readDataFromDatabase(query, readDataCallback)
        }else{
            getAllAnnouncementsFromCategoryByFilterNextPage(category, time, filter, readDataCallback)
        }
    }

    private fun getAllAnnouncementsFromCategoryByFilterNextPage(category: String, time: String, tempFilter: String, readDataCallback: ReadDataCallback?){//ф-ция для фильтрации всех объявления по категориям на первой странице
        val orderBy = "cat_" + tempFilter.split("|")[0]//указываем с какой позиции разделяем наш фильтр
        val filter = category + "_" + tempFilter.split("|")[1]//указываем с какой позиции разделяем наш фильтр
        val query = database.orderByChild("announcementFilter/$orderBy")
            .endBefore(filter + "_" + time).limitToLast(ANNOUNCEMENT_LIMIT) //нужно взять объявления с категорией из категории
        readNextPageFromDatabase(query, filter, orderBy, readDataCallback)
    }

    fun deleteAnnouncement(announcement: Announcement, listener: FinishWorkListener){//ф-ция для удаления объявления
        if(announcement.key == null || announcement.uid == null) return
        database.child(announcement.key).child(announcement.uid).removeValue().addOnCompleteListener {
            listener.onFinish(true)
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

    private fun readNextPageFromDatabase(query: Query, filter: String, orderBy: String, readDataCallback: ReadDataCallback?){//ф-ция для считывания с базы данных после скролла, чтобы не загружало такие объявления по второму раза
        query.addListenerForSingleValueEvent(object: ValueEventListener{ //делаем обновления базы данных не в реальном времени, а когда нажимаем слушатель
            val announcementArray = ArrayList<Announcement>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for(item in snapshot.children) { //создаём цикл, где будет перебирать все узлы в ключе main -> ....
                    var announcement: Announcement? = null
                    item.children.forEach { //второй цикл пробегает все узлы в ключе main -> индетификатор объявления -> .....
                        if(announcement == null) announcement = it.child(ANNOUNCEMENT_NODE).getValue(Announcement::class.java) //запись объявления
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java) //запись в ключ info
                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString()//здесь в ключе мы берём определенный фильтр
                    Log.d("MyLog", "Filter value: $filterNodeValue")
                    val favCounter = item.child(FAVS_NODE).childrenCount //запись в ключ favs
                    val isFav = authentication.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) } //здесь показывает тру или фалс, добавлено или нет в избранные
                    announcement?.isFav = isFav != null
                    announcement?.favCounter = favCounter.toString() //записываем значение счётчика
                    announcement?.viewsCounter = infoItem?.viewsCounter ?: "0" //записываем изначальное значение 0
                    announcement?.emailsCounter = infoItem?.emailsCounter ?: "0"//записываем изначальное значение 0
                    announcement?.callsCounter = infoItem?.callsCounter ?: "0"//записываем изначальное значение 0
                    if(announcement != null && filterNodeValue.startsWith(filter))announcementArray.add(announcement!!)
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
        fun onFinish(isDone: Boolean)
    }

    companion object{
        const val ANNOUNCEMENT_NODE = "announcement"
        const val FILTER_NODE = "announcementFilter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
        const val ANNOUNCEMENT_LIMIT = 2
        const val ANNOUNCEMENT_FILTER_TIME = "announcementFilter/time"
        const val ANNOUNCEMENT_FILTER_CAT_TIME = "announcementFilter/cat_time"
    }
}