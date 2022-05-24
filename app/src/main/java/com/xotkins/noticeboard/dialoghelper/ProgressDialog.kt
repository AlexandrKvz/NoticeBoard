package com.xotkins.noticeboard.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.xotkins.noticeboard.databinding.ProgressDialogLayoutBinding
import com.xotkins.noticeboard.databinding.SignDialogBinding

object ProgressDialog {

    fun createProgressDialog(activity: Activity): AlertDialog{
        val builder = AlertDialog.Builder(activity)
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(activity.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}