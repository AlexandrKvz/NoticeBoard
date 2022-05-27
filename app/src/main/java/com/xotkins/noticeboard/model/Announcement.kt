package com.xotkins.noticeboard.model

import java.io.Serializable

data class Announcement(
    val country: String? = null,
    val city: String? = null,
    val index: String? = null,
    val number: String? = null,
    val email: String? = null,
    val withSend: String? = null,
    val category: String? = null,
    val title: String? = null,
    val price: String? = null,
    val description: String? = null,

    val image1: String? = null,
    val image2: String? = null,
    val image3: String? = null,
    val key: String? = null,
    val uid: String? = null,
    val time: String = "0",


    var favCounter: String? = "0",
    var isFav: Boolean = false,

    var viewsCounter: String = "0",
    var emailsCounter: String = "0",
    var callsCounter: String = "0"
):Serializable
