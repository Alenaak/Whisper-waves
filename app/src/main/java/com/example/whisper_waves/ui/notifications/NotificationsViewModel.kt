package com.example.whisper_waves.ui.notifications

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.whisper_waves.data.db.entity.Chat
import com.example.whisper_waves.data.db.entity.Message
import com.example.whisper_waves.data.db.entity.UserFriend
import com.example.whisper_waves.data.db.entity.UserInfo
import com.example.whisper_waves.data.db.entity.UserNotification
import com.example.whisper_waves.data.db.repository.DatabaseRepository
import com.example.whisper_waves.ui.DefaultViewModel
import com.example.whisper_waves.data.Result
import com.example.whisper_waves.util.addNewItem
import com.example.whisper_waves.util.removeItem
import com.example.whisper_waves.util.convertTwoUserIDs

class NotificationsViewModelFactory(private val myUserID: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationsViewModel(myUserID) as T
    }
}

class NotificationsViewModel(private val myUserID: String) : DefaultViewModel() {

    private val dbRepository: DatabaseRepository = DatabaseRepository()
    private val updatedUserInfo = MutableLiveData<UserInfo>()
    private val userNotificationsList = MutableLiveData<MutableList<UserNotification>>()

    val usersInfoList = MediatorLiveData<MutableList<UserInfo>>()

    init {
        usersInfoList.addSource(updatedUserInfo) { usersInfoList.addNewItem(it) }
        loadNotifications()
    }

    private fun loadNotifications() {
        dbRepository.loadNotifications(myUserID) { result: Result<MutableList<UserNotification>> ->
            onResult(userNotificationsList, result)
            if (result is Result.Success) result.data?.forEach { loadUserInfo(it) }
        }
    }

    private fun loadUserInfo(userNotification: UserNotification) {
        dbRepository.loadUserInfo(userNotification.userID) { result: Result<UserInfo> ->
            onResult(updatedUserInfo, result)
        }
    }

    private fun updateNotification(otherUserInfo: UserInfo, removeOnly: Boolean) {
        val userNotification = userNotificationsList.value?.find {
            it.userID == otherUserInfo.id
        }

        if (userNotification != null) {
            if (!removeOnly) {
                dbRepository.updateNewFriend(UserFriend(myUserID), UserFriend(otherUserInfo.id))
                val newChat = Chat().apply {
                    info.id = convertTwoUserIDs(myUserID, otherUserInfo.id)
                    lastMessage = Message(seen = true, text = "Say hello!")
                }
                dbRepository.updateNewChat(newChat)
            }
            dbRepository.removeNotification(myUserID, otherUserInfo.id)
            dbRepository.removeSentRequest(otherUserInfo.id, myUserID)

            usersInfoList.removeItem(otherUserInfo)
            userNotificationsList.removeItem(userNotification)
        }
    }

    fun acceptNotificationPressed(userInfo: UserInfo) {
        updateNotification(userInfo, false)
    }

    fun declineNotificationPressed(userInfo: UserInfo) {
        updateNotification(userInfo, true)
    }
}