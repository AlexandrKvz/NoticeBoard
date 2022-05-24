package com.xotkins.noticeboard.accounthelper

import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.xotkins.noticeboard.activity.MainActivity
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.constants.FirebaseAuthConstants
import java.lang.Exception

class AccountHelper(activity: MainActivity) {
    private val activity = activity
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) { //регистрация по эд. почте
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.mAuth.currentUser?.delete()?.addOnCompleteListener { //если порльзователь анонимный, то удаляем и регистрируем по почте
                    task ->
                if(task.isSuccessful){
                    activity.mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signUpWithEmailSuccessful(task.result.user!!)
                            } else {
                                signUpWithEmailExceptions(task.exception!!, email, password)
                            }
                        }
                     }
                 }
             }
         }

    private fun signUpWithEmailSuccessful(user: FirebaseUser){
        sendEmailVerification(user)
        activity.uiUpdate(user)
    }

    private fun signUpWithEmailExceptions(exception: Exception, email: String, password: String){
        //  Toast.makeText(activity, activity.resources.getString(R.string.sign_up_error), Toast.LENGTH_LONG).show()
        //Log.d("MyLog", "Exception: ${exception.errorCode}")
        //   Log.d("MyLog", "Exception: ${task.exception}")
        if (exception is FirebaseAuthUserCollisionException) {
            //   Log.d("MyLog", "Exception: ${task.exception}")
            //  Log.d("MyLog", "Exception: ${task.exception}")
            if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                //  Toast.makeText(activity, FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE, Toast.LENGTH_LONG).show()
                linkEmailToG(email, password)
            }
        } else if (exception is FirebaseAuthInvalidCredentialsException) {
            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(activity, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
            }
        }
        if (exception is FirebaseAuthWeakPasswordException) {
            // Log.d("MyLog", "Exception: ${exception.errorCode}")
            if (exception.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                Toast.makeText(activity, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signInWithEmail(email: String, password: String) { //вход по эл. почте
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.mAuth.currentUser?.delete()?.addOnCompleteListener { //если пользователь аноним, то удаляем аноним и входжим по почте
                    task ->
                if (task.isSuccessful) {
                    activity.mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(activity, "Sign in done!", Toast.LENGTH_LONG).show()
                                activity.uiUpdate(task.result?.user)
                            } else {
                                signInWithEmailExceptions(task.exception!!, email, password)
                            }
                        }
                    }
                }
             }
          }

    private fun signInWithEmailExceptions(exception: Exception, email: String, password: String){
        //Log.d("MyLog", "Exception: ${task.exception}")
        if (exception is FirebaseAuthInvalidCredentialsException) {
            //  Log.d("MyLog", "Exception: ${task.exception}")
            //  Log.d("MyLog", "Exception: ${exception.errorCode}")
            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(activity,
                    FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                    Toast.LENGTH_LONG).show()
            } else if (exception.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                Toast.makeText(activity,
                    FirebaseAuthConstants.ERROR_WRONG_PASSWORD,
                    Toast.LENGTH_LONG).show()
            }
        } else if (exception is FirebaseAuthInvalidUserException) {

            if (exception.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                Toast.makeText(activity,
                    activity.resources.getString(R.string.need_register_email),
                    Toast.LENGTH_LONG).show()
            }
            // Log.d("MyLog", "Exception: ${exception.errorCode}")
        }
    }



    private fun linkEmailToG(email: String, password: String){ //обьединять почту с гуглом
        val credential = EmailAuthProvider.getCredential(email, password)
        if(activity.mAuth.currentUser != null){
        activity.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, activity.resources.getString(R.string.link_done), Toast.LENGTH_LONG).show()
                 }
            }
        }else{
            Toast.makeText(activity, activity.resources.getString(R.string.enter_to_g), Toast.LENGTH_LONG).show()
        }
    }

    private fun getSignInClient(): GoogleSignInClient{ //вход по гуглу
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(activity.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun signInWithGoogle(){ //данные для лаунчера, вход по гуглу
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        activity.googleSignInLauncher.launch(intent)
    }

    fun signOutGoogle(){ //выход из гугла аккаунта
        getSignInClient().signOut()

    }
    fun signInFirebaseWithGoogle(token: String){ // Вход по гуглу
        val credential = GoogleAuthProvider.getCredential(token, null)
        activity.mAuth.currentUser?.delete()?.addOnCompleteListener { //если пользователь анонимный, то удаляем аноноим, и регистрируем гугл аккаунт
                task ->
            if(task.isSuccessful){
                activity.mAuth.signInWithCredential(credential).addOnCompleteListener { task->
                    if(task.isSuccessful){
                        Toast.makeText(activity, "Sign in done!", Toast.LENGTH_LONG).show()
                        activity.uiUpdate(task.result?.user)
                    }else {
                        //  Log.d("MyLog", "Google Sign In Exception : ${task.exception}")
                        activity.uiUpdate(task.result?.user)
                    }
                }
            }
        }
    }

    private fun sendEmailVerification(user:FirebaseUser){ //  письмо подтеврждения на почту
        user.sendEmailVerification().addOnCompleteListener {task ->
            if(task.isSuccessful){
                Toast.makeText(activity, activity.resources.getString(R.string.send_verification_email_done), Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(activity, activity.resources.getString(R.string.send_verification_email_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signInAnonymously(listener: Listener){
        activity.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if(task.isSuccessful){
                listener.onComplete()
                Toast.makeText(activity, "Вы вошли как гость", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(activity, "Войти как гость не удалось", Toast.LENGTH_SHORT).show()
            }
        }
    }
    interface Listener{
        fun onComplete()
    }
}