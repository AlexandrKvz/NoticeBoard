package com.xotkins.noticeboard.activity

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}