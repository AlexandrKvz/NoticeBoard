package com.xotkins.noticeboard.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xotkins.noticeboard.adapters.AnnouncementRcAdapter
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.model.DatabaseManager

class FirebaseViewModel: ViewModel() {
    private val databaseManager = DatabaseManager()
    val liveAnnouncementsData = MutableLiveData<ArrayList<Announcement>>()


    fun loadAllAnnouncements(){//ф-ция для загрузке всех объявлений
        databaseManager.getAllAnnouncements(object: DatabaseManager.ReadDataCallback{
            override fun readData(list: ArrayList<Announcement>) {
                liveAnnouncementsData.value = list
            }
        })
    }

    fun loadMyFavs(){
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
            override fun onFinish() {
                val updatedListAnnouncement = liveAnnouncementsData.value //выбираем объявления для уделания и записываем
                updatedListAnnouncement?.remove(announcement)//удаляем
                liveAnnouncementsData.postValue(updatedListAnnouncement)//обновляем данные
            }
        })
    }

    fun announcementViewed(announcement: Announcement){
        databaseManager.announcementViewed(announcement)
    }

    fun onFavClick(announcement: Announcement){//ф-ция для добавления объявления в избранные
        databaseManager.onFavClick(announcement, object: DatabaseManager.FinishWorkListener{
            override fun onFinish() {
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