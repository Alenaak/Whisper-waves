package com.example.whisper_waves.ui.start.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.whisper_waves.data.model.Login
import com.example.whisper_waves.data.db.repository.AuthRepository
import com.example.whisper_waves.ui.DefaultViewModel
import com.example.whisper_waves.data.Event
import com.example.whisper_waves.data.Result
import com.example.whisper_waves.util.isTextValid
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : DefaultViewModel() {

    private val authRepository = AuthRepository()
    private val _isLoggedInEvent = MutableLiveData<Event<FirebaseUser>>()

    val isLoggedInEvent: LiveData<Event<FirebaseUser>> = _isLoggedInEvent
    val emailText = MutableLiveData<String>() // Two way
    val passwordText = MutableLiveData<String>() // Two way
    val isLoggingIn = MutableLiveData<Boolean>() // Two way
    private val hiddenEmailSuffix = "@gmail.com"
    private fun login() {
        isLoggingIn.value = true
        val completeDisplayName = getCompleteDisplayName()
        Log.d("CompleteuserName", completeDisplayName)
        val login = Login(completeDisplayName, passwordText.value!!)

        authRepository.loginUser(login) { result: Result<FirebaseUser> ->
            onResult(null, result)
            if (result is Result.Success) _isLoggedInEvent.value = Event(result.data!!)
            if (result is Result.Success || result is Result.Error) isLoggingIn.value = false
        }
    }

    fun loginPressed() {
    /*    if (!isEmailValid(emailText.value.toString())) {
            mSnackBarText.value = Event("Invalid email format")
            return
        }
*/


        if (!isTextValid(6, passwordText.value)) {
            mSnackBarText.value = Event("Password is too short")
            return
        }

        login()
    }


    private fun getCompleteDisplayName(): String {
        return "${emailText.value}$hiddenEmailSuffix"
    }
}