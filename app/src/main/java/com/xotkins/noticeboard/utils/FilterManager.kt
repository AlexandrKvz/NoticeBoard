package com.xotkins.noticeboard.utils

import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.model.AnnouncementFilter
import java.lang.StringBuilder

object FilterManager {
    fun createFilter(announcement: Announcement): AnnouncementFilter{
        return AnnouncementFilter(
            announcement.time,
            "${announcement.category}_${announcement.time}",
            "${announcement.category}_${announcement.country}_${announcement.withSend}_${announcement.time}",
            "${announcement.category}_${announcement.country}_${announcement.city}_${announcement.withSend}_${announcement.time}",
            "${announcement.category}_${announcement.country}_${announcement.city}_${announcement.index}_${announcement.withSend}_${announcement.time}",
            "${announcement.category}_${announcement.index}_${announcement.withSend}_${announcement.time}",
            "${announcement.category}_${announcement.withSend}_${announcement.time}",

            "${announcement.country}_${announcement.withSend}_${announcement.time}",
            "${announcement.country}_${announcement.city}_${announcement.withSend}_${announcement.time}",
            "${announcement.country}_${announcement.city}_${announcement.index}_${announcement.withSend}_${announcement.time}",
            "${announcement.index}_${announcement.withSend}_${announcement.time}",
            "${announcement.withSend}_${announcement.time}"
        )
    }

    fun getFilter(filter: String): String{//ф-ция для определения какой фильтр будет применяться для базы данных
        val sBuilderNode = StringBuilder()
        val sBuilderFilter = StringBuilder()
        val tempArray = filter.split("_") //здесь указываем с помощью чего мы разделяем наш массив
        if(tempArray[0] != "empty") {
            sBuilderNode.append("country_")
            sBuilderFilter.append("${tempArray[0]}_")
        }
        if(tempArray[1] != "empty") {
            sBuilderNode.append("city_")
            sBuilderFilter.append("${tempArray[1]}_")
        }
        if(tempArray[2] != "empty") {
            sBuilderNode.append("index_")
            sBuilderFilter.append("${tempArray[2]}_")
        }
        sBuilderNode.append("withSend_time")
        sBuilderFilter.append(tempArray[3])
        return "$sBuilderNode|$sBuilderFilter"
    }
}