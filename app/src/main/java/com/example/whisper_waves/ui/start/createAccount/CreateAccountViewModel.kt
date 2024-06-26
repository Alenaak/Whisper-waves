package com.example.whisper_waves.ui.start.createAccount

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.whisper_waves.data.Event
import com.example.whisper_waves.data.Result
import com.example.whisper_waves.data.db.entity.User
import com.example.whisper_waves.data.db.repository.AuthRepository
import com.example.whisper_waves.data.db.repository.DatabaseRepository
import com.example.whisper_waves.data.model.CreateUser
import com.example.whisper_waves.ui.DefaultViewModel
import com.example.whisper_waves.util.isTextValid
import com.google.firebase.auth.FirebaseUser

class CreateAccountViewModel : DefaultViewModel() {

    private val dbRepository = DatabaseRepository()
    private val authRepository = AuthRepository()
    private val mIsCreatedEvent = MutableLiveData<Event<FirebaseUser>>()

    val isCreatedEvent: LiveData<Event<FirebaseUser>> = mIsCreatedEvent
    val displayNameText = MutableLiveData<String>() // Two way
    val emailText = MutableLiveData<String>() // Two way
    val passwordText = MutableLiveData<String>() // Two way
    val isCreatingAccount = MutableLiveData<Boolean>()
    private val hiddenEmailSuffix = "@gmail.com"


    private fun createAccount() {
        isCreatingAccount.value = true
        val completeDisplayName = getCompleteDisplayName()
        val createUser =
            CreateUser(displayNameText.value!!, completeDisplayName, passwordText.value!!)

        Log.d("CompleteuserName", completeDisplayName)
        authRepository.createUser(createUser) { result: Result<FirebaseUser> ->
            onResult(null, result)
            if (result is Result.Success) {
                mIsCreatedEvent.value = Event(result.data!!)
                dbRepository.updateNewUser(User().apply {
                    info.id = result.data.uid
                    info.displayName = createUser.displayName
                })
            }
            if (result is Result.Success || result is Result.Error) isCreatingAccount.value = false
        }
    }

    fun createAccountPressed() {
        if (!isTextValid(2, displayNameText.value)) {
            mSnackBarText.value = Event("Display name is too short")
            return
        }

        /*  if (!isEmailValid(emailText.value.toString())) {
              mSnackBarText.value = Event("Invalid email format")
              return
          }*/


        if (!isTextValid(6, passwordText.value)) {
            mSnackBarText.value = Event("Password is too short")
            return
        }

        createAccount()
    }

    private fun getCompleteDisplayName(): String {
        return "${emailText.value}$hiddenEmailSuffix"
    }
}
