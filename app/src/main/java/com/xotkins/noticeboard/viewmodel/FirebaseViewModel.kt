package com.xotkins.noticeboard.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.model.DatabaseManager

class FirebaseViewModel: ViewModel() {
    private val databaseManager = DatabaseManager()
    val liveAnnouncementsData = MutableLiveData<ArrayList<Announcement>>()


    fun loadAllAnnouncementsFirstPage(filter: String){//ф-ция для загрузке всех объявлений на первой странице
        databaseManager.getAllAnnouncementsFirstPage(filter, object: DatabaseManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAnnouncementsData.value = list
            }
        })
    }

    fun loadAllAnnouncementsNextPage(time: String, filter: String){//ф-ция для загрузке всех объявлений на второй странице после скролла
        databaseManager.getAllAnnouncementsNextPage(time, filter, object: DatabaseManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAnnouncementsData.value = list
            }
        })
    }

    fun loadAllAnnouncementsFromCategoryFirstPage(category: String, filter: String){//ф-ция для загрузке всех объявлений в категории на первой странице
        databaseManager.getAllAnnouncementsFromCategoryFirstPage(category, filter, object: DatabaseManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAnnouncementsData.value = list
            }
        })
    }

    fun loadAllAnnouncementsFromCategoryNextPage(category: String, time: String, filter: String){//ф-ция для загрузке всех объявлений в категории на второй странице после скролла
        databaseManager.getAllAnnouncementsFromCategoryNextPage(category, time, filter, object: DatabaseManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAnnouncementsData.value = list
            }
        })
    }

    fun loadMyFavs(){//ф-ция для фильтрации избранных
        databaseManager.getMyFavs(object: DatabaseManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAnnouncementsData.value = list
            }

        })
    }

    fun loadMyAnnouncements(){//ф-ция для загрузки моих объявлений
        databaseManager.getMyAnnouncements(object: DatabaseManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAnnouncementsData.value = list
            }
        })
    }

    fun deleteAnnouncement(announcement: Announcement){//ф-ция для удаления объявления
        databaseManager.deleteAnnouncement(announcement, object: DatabaseManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedListAnnouncement = liveAnnouncementsData.value //выбираем объявления для уделания и записываем
                updatedListAnnouncement?.remove(announcement)//удаляем
                liveAnnouncementsData.postValue(updatedListAnnouncement)//обновляем данные
            }
        })
    }

    fun announcementViewed(announcement: Announcement){//ф-ция для счётчика просмотров
        databaseManager.announcementViewed(announcement)
    }

    fun onFavClick(announcement: Announcement){//ф-ция для добавления объявления в избранные
        databaseManager.onFavClick(announcement, object: DatabaseManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedListAnnouncement = liveAnnouncementsData.value
                val position = updatedListAnnouncement?.indexOf(announcement) //записываем позицию объявления на которое нажали
                if(position != -1){
                    position?.let{
                        val favCounter = if(announcement.isFav)announcement.favCounter!!.toInt() - 1 else announcement.favCounter!!.toInt() + 1 //счётчик в реальном времени, если добавляем в избранные +1, если убираем то - 1
                        updatedListAnnouncement[position] = updatedListAnnouncement[position].copy(isFav = !announcement.isFav, favCounter = favCounter.toString()) //(isFav = !announcement.isFav) здесь меняем значения на противоположное, в итоге создаём копию объявления с измененным значением и адаптер обновит объявление
                    }
                }
                liveAnnouncementsData.postValue(updatedListAnnouncement)
            }
        })
    }
}