package com.xotkins.noticeboard.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.xotkins.noticeboard.activity.MainActivity
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.accounthelper.AccountHelper
import com.xotkins.noticeboard.constants.DialogConst
import com.xotkins.noticeboard.databinding.SignDialogBinding

class DialogHelper( activity: MainActivity) {
    val activity = activity
    val accountHelper = AccountHelper(activity)

    fun createSignDialog(index:Int){
        val builder = AlertDialog.Builder(activity)
        val rootDialogElement = SignDialogBinding.inflate(activity.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        setDialogState(index, rootDialogElement)

        val dialog = builder.create()
        rootDialogElement.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, rootDialogElement, dialog)
        }
        rootDialogElement.btForgetPassword.setOnClickListener {
            setOnClickResetPassword(rootDialogElement, dialog)
        }
        rootDialogElement.btGoogleSignIn.setOnClickListener {
           accountHelper.signInWithGoogle()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setOnClickResetPassword(rootDialogElement: SignDialogBinding, dialog: AlertDialog?) { //сброс пароля
        if(rootDialogElement.edSignEmail.text.isNotEmpty()){
            activity.mAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString()).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    Toast.makeText(activity, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG).show()
                }
            }
            dialog?.dismiss()
        }else{
            rootDialogElement.tvDialogMessage.visibility = View.VISIBLE
        }
    }
    private fun setOnClickSignUpIn(index: Int, rootDialogElement: SignDialogBinding, dialog: AlertDialog?) { //вход/регистрация
        dialog?.dismiss()
        if(index == DialogConst.SIGN_UP_STATE){
            accountHelper.signUpWithEmail(rootDialogElement.edSignEmail.text.toString(), rootDialogElement.edSignPassword.text.toString())
        }else{
            accountHelper.signInWithEmail(rootDialogElement.edSignEmail.text.toString(), rootDialogElement.edSignPassword.text.toString())
        }
    }
    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) {//вспылвающий диалог
        if(index == DialogConst.SIGN_UP_STATE){
            rootDialogElement.tvSignTitle.text = activity.resources.getString(R.string.acc_sign_up)
            rootDialogElement.btSignUpIn.text = activity.resources.getString(R.string.sign_up_action)
        }else{
            rootDialogElement.tvSignTitle.text = activity.resources.getString(R.string.acc_sign_in)
            rootDialogElement.btSignUpIn.text = activity.resources.getString(R.string.sign_in_action)
            rootDialogElement.btForgetPassword.visibility = View.VISIBLE
        }
    }
}