package com.example.whisper_waves.ui.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import com.example.whisper_waves.R
import com.example.whisper_waves.data.Event
import com.example.whisper_waves.data.db.entity.Chat
import com.example.whisper_waves.data.db.entity.Message
import com.example.whisper_waves.data.db.entity.UserInfo
import com.example.whisper_waves.data.db.remote.FirebaseReferenceChildObserver
import com.example.whisper_waves.data.db.remote.FirebaseReferenceValueObserver
import com.example.whisper_waves.data.db.repository.DatabaseRepository
import com.example.whisper_waves.ui.DefaultViewModel
import com.example.whisper_waves.data.Result
import com.example.whisper_waves.data.db.repository.StorageRepository
import com.example.whisper_waves.util.addNewItem

class ChatViewModelFactory(
    private val applicationContext: Context,
    private val myUserID: String,
    private val otherUserID: String,
    private val chatID: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(applicationContext, myUserID, otherUserID, chatID) as T
    }
}

class ChatViewModel(
    private val applicationContext: Context,
    private val myUserID: String,
    private val otherUserID: String,
    private val chatID: String
) : DefaultViewModel() {
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100
    private val REQUEST_IMAGE_PICK = 200
    private val REQUEST_VIDEO_PICK = 201
    private val dbRepository: DatabaseRepository = DatabaseRepository()

    private val _otherUser: MutableLiveData<UserInfo> = MutableLiveData()
    private val _addedMessage = MutableLiveData<Message>()
    private val fbRefMessagesChildObserver = FirebaseReferenceChildObserver()
    private val fbRefUserInfoObserver = FirebaseReferenceValueObserver()
    private val storageRepository = StorageRepository()
    private val _ImageEvent = MutableLiveData<Event<Unit>>()
    val ImageEvent: LiveData<Event<Unit>> = _ImageEvent
    val messagesList = MediatorLiveData<MutableList<Message>>()
    val newMessageText = MutableLiveData<String>()
    val otherUser: LiveData<UserInfo> = _otherUser

    init {
        setupChat()
        checkAndUpdateLastMessageSeen()
    }

    override fun onCleared() {
        super.onCleared()
        fbRefMessagesChildObserver.clear()
        fbRefUserInfoObserver.clear()
    }

    private fun checkAndUpdateLastMessageSeen() {
        dbRepository.loadChat(chatID) { result: Result<Chat> ->
            if (result is Result.Success && result.data != null) {
                result.data.lastMessage.let {
                    if (!it.seen && it.senderID != myUserID) {
                        it.seen = true
                        dbRepository.updateChatLastMessage(chatID, it)
                    }
                }
            }
        }
    }

    private fun setupChat() {
        dbRepository.loadAndObserveUserInfo(otherUserID, fbRefUserInfoObserver) { result: Result<UserInfo> ->
            onResult(_otherUser, result)
            if (result is Result.Success && !fbRefMessagesChildObserver.isObserving()) {
                loadAndObserveNewMessages()
            }
        }
    }

    private fun loadAndObserveNewMessages() {
        messagesList.addSource(_addedMessage) { messagesList.addNewItem(it) }
        dbRepository.loadAndObserveMessagesAdded(chatID, fbRefMessagesChildObserver) { result: Result<Message> ->
            onResult(_addedMessage, result)
        }
    }

    private fun showNotification(messageText: Message) {
        val channelId = "MessageNotificationChannel"
        val notificationId = 1

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Message Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("New Message")
            .setContentText(messageText.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Show the notification
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }

    fun sendMessagePressed() {
        if (!newMessageText.value.isNullOrBlank()) {
            val newMsg = Message(myUserID, newMessageText.value!!)
            dbRepository.updateNewMessage(chatID, newMsg)
            dbRepository.updateChatLastMessage(chatID, newMsg)
          //  showNotification(newMsg)
            newMessageText.value = null
        }
    }



    fun uploadUserImage(byteArray: ByteArray) {
        storageRepository.updateUserProfileImage1(chatID, byteArray) { result: Result<Uri> ->
            onResult(null, result)
            if (result is Result.Success) {
                val imageUrl = result.data.toString()
                // Send the image URL as a message to the user
                val message = Message(senderID = myUserID, imageRef = imageUrl, status = Message.MessageStatus.SENT)
                dbRepository.updateNewMessage(chatID, message)
                //  dbRepository.updateNewMessage(chatID, message)
                dbRepository.updateChatLastMessage(chatID, message)

                // You can also show a notification or perform any other actions here
            }
        }
    }
    fun uploadUserAudio(byteArray: ByteArray) {
        storageRepository.uploadUserAudio(chatID, byteArray) { result: Result<Uri> ->
            onResult(null, result)
            if (result is Result.Success) {
                val audioUrl = result.data.toString()
                // Send the audio URL as a message to the user
                val message = Message(senderID = myUserID, audioRef = audioUrl, status = Message.MessageStatus.SENT)
                dbRepository.updateNewMessage(chatID, message)
                dbRepository.updateChatLastMessage(chatID, message)

                // You can also show a notification or perform any other actions here
            }
        }
    }



    companion object {
        private const val TAG = "ChatViewModel"
    }
}
