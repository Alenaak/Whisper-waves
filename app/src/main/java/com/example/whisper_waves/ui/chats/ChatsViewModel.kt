package com.example.whisper_waves.ui.chats

import androidx.lifecycle.*
import com.example.whisper_waves.data.Event
import com.example.whisper_waves.data.Result
import com.example.whisper_waves.data.db.entity.Chat
import com.example.whisper_waves.data.model.ChatWithUserInfo
import com.example.whisper_waves.data.db.entity.UserFriend
import com.example.whisper_waves.data.db.entity.UserInfo
import com.example.whisper_waves.data.db.remote.FirebaseReferenceValueObserver
import com.example.whisper_waves.data.db.repository.DatabaseRepository
import com.example.whisper_waves.ui.DefaultViewModel
import com.example.whisper_waves.util.addNewItem
import com.example.whisper_waves.util.convertTwoUserIDs
import com.example.whisper_waves.util.updateItemAt


class ChatsViewModelFactory(private val myUserID: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatsViewModel(myUserID) as T
    }
}

class ChatsViewModel(val myUserID: String) : DefaultViewModel() {

    private val repository: DatabaseRepository = DatabaseRepository()
    private val firebaseReferenceObserverList = ArrayList<FirebaseReferenceValueObserver>()
    private val _updatedChatWithUserInfo = MutableLiveData<ChatWithUserInfo>()
    private val _selectedChat = MutableLiveData<Event<ChatWithUserInfo>>()

    var selectedChat: LiveData<Event<ChatWithUserInfo>> = _selectedChat
    val chatsList = MediatorLiveData<MutableList<ChatWithUserInfo>>()

    init {
        chatsList.addSource(_updatedChatWithUserInfo) { newChat ->
            val chat = chatsList.value?.find { it.mChat.info.id == newChat.mChat.info.id }
            if (chat == null) {
                chatsList.addNewItem(newChat)
            } else {
                chatsList.updateItemAt(newChat, chatsList.value!!.indexOf(chat))
            }
        }
        setupChats()
    }

    override fun onCleared() {
        super.onCleared()
        firebaseReferenceObserverList.forEach { it.clear() }
    }

    private fun setupChats() {
        loadFriends()
    }

    private fun loadFriends() {
        repository.loadFriends(myUserID) { result: Result<List<UserFriend>> ->
            onResult(null, result)
            if (result is Result.Success) result.data?.forEach { loadUserInfo(it) }
        }
    }

    private fun loadUserInfo(userFriend: UserFriend) {
        repository.loadUserInfo(userFriend.userID) { result: Result<UserInfo> ->
            onResult(null, result)
            if (result is Result.Success) result.data?.let { loadAndObserveChat(it) }
        }
    }

    private fun loadAndObserveChat(userInfo: UserInfo) {
        val observer = FirebaseReferenceValueObserver()
        firebaseReferenceObserverList.add(observer)
        repository.loadAndObserveChat(convertTwoUserIDs(myUserID, userInfo.id), observer) { result: Result<Chat> ->
            if (result is Result.Success) {
                _updatedChatWithUserInfo.value = result.data?.let { ChatWithUserInfo(it, userInfo) }
            } else if (result is Result.Error) {
                chatsList.value?.let {
                    val newList = mutableListOf<ChatWithUserInfo>().apply { addAll(it) }
                    newList.removeIf { it2 -> result.msg.toString().contains(it2.mUserInfo.id) }
                    chatsList.value = newList
                }
            }
        }
    }

    fun selectChatWithUserInfoPressed(chat: ChatWithUserInfo) {
        _selectedChat.value = Event(chat)
    }
}