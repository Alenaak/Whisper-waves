package com.example.whisper_waves.data.db.repository

import com.example.whisper_waves.data.db.entity.Chat
import com.example.whisper_waves.data.db.entity.Message
import com.example.whisper_waves.data.db.entity.User
import com.example.whisper_waves.data.db.entity.UserFriend
import com.example.whisper_waves.data.db.entity.UserInfo
import com.example.whisper_waves.data.db.entity.UserNotification
import com.example.whisper_waves.data.db.entity.UserRequest

import com.example.whisper_waves.data.db.remote.FirebaseDataSource
import com.example.whisper_waves.data.db.remote.FirebaseReferenceChildObserver
import com.example.whisper_waves.data.db.remote.FirebaseReferenceValueObserver
import com.example.whisper_waves.data.Result
import com.example.whisper_waves.util.wrapSnapshotToArrayList
import com.example.whisper_waves.util.wrapSnapshotToClass
import com.google.firebase.database.FirebaseDatabase


class DatabaseRepository {
    private val firebaseDatabaseService = FirebaseDataSource()
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun updateUserStatus(userID: String, status: String) {
        firebaseDatabaseService.updateUserStatus(userID, status)
    }

    fun updateNewMessage(messagesID: String, message: Message) {
        firebaseDatabaseService.pushNewMessage(messagesID, message)
    }

    fun updateNewUser(user: User) {
        firebaseDatabaseService.updateNewUser(user)
    }

    fun updateNewFriend(myUser: UserFriend, otherUser: UserFriend) {
        firebaseDatabaseService.updateNewFriend(myUser, otherUser)
    }

    fun updateNewSentRequest(userID: String, userRequest: UserRequest) {
        firebaseDatabaseService.updateNewSentRequest(userID, userRequest)
    }

    fun updateNewNotification(otherUserID: String, userNotification: UserNotification) {
        firebaseDatabaseService.updateNewNotification(otherUserID, userNotification)
    }

    fun updateChatLastMessage(chatID: String, message: Message) {
        firebaseDatabaseService.updateLastMessage(chatID, message)
    }

    fun updateNewChat(chat: Chat){
        firebaseDatabaseService.updateNewChat(chat)
    }

    fun updateUserProfileImageUrl(userID: String, url: String){
        firebaseDatabaseService.updateUserProfileImageUrl(userID, url)
    }
    fun addmediaUrl(userID: String, url: String){
        firebaseDatabaseService.addMediaSent(userID, url)
    }
    fun removeNotification(userID: String, notificationID: String) {
        firebaseDatabaseService.removeNotification(userID, notificationID)
    }

    fun removeFriend(userID: String, friendID: String) {
        firebaseDatabaseService.removeFriend(userID, friendID)
    }

    fun removeSentRequest(otherUserID: String, myUserID: String) {
        firebaseDatabaseService.removeSentRequest(otherUserID, myUserID)
    }

    fun removeChat(chatID: String) {
        firebaseDatabaseService.removeChat(chatID)
    }

    fun removeMessages(messagesID: String){
        firebaseDatabaseService.removeMessages(messagesID)
    }

    fun loadUser(userID: String, b: ((Result<User>) -> Unit)) {
        firebaseDatabaseService.loadUserTask(userID).addOnSuccessListener {
            b.invoke(Result.Success(wrapSnapshotToClass(User::class.java, it)))
        }.addOnFailureListener { b.invoke(Result.Error(it.message)) }
    }

    fun loadUserInfo(userID: String, b: ((Result<UserInfo>) -> Unit)) {
        firebaseDatabaseService.loadUserInfoTask(userID).addOnSuccessListener {
            b.invoke(Result.Success(wrapSnapshotToClass(UserInfo::class.java, it)))
        }.addOnFailureListener { b.invoke(Result.Error(it.message)) }
    }

    fun loadChat(chatID: String, b: ((Result<Chat>) -> Unit)) {
        firebaseDatabaseService.loadChatTask(chatID).addOnSuccessListener {
            b.invoke(Result.Success(wrapSnapshotToClass(Chat::class.java, it)))
        }.addOnFailureListener { b.invoke(Result.Error(it.message)) }
    }

    fun loadUsers(b: ((Result<MutableList<User>>) -> Unit)) {
        b.invoke(Result.Loading)
        firebaseDatabaseService.loadUsersTask().addOnSuccessListener {
            val usersList = wrapSnapshotToArrayList(User::class.java, it)
            b.invoke(Result.Success(usersList))
        }.addOnFailureListener { b.invoke(Result.Error(it.message)) }
    }

    fun loadFriends(userID: String, b: ((Result<List<UserFriend>>) -> Unit)) {
        b.invoke(Result.Loading)
        firebaseDatabaseService.loadFriendsTask(userID).addOnSuccessListener {
            val friendsList = wrapSnapshotToArrayList(UserFriend::class.java, it)
            b.invoke(Result.Success(friendsList))
        }.addOnFailureListener { b.invoke(Result.Error(it.message)) }
    }

    fun loadNotifications(userID: String, b: ((Result<MutableList<UserNotification>>) -> Unit)) {
        b.invoke(Result.Loading)
        firebaseDatabaseService.loadNotificationsTask(userID).addOnSuccessListener {
            val notificationsList = wrapSnapshotToArrayList(UserNotification::class.java, it)
            b.invoke(Result.Success(notificationsList))
        }.addOnFailureListener { b.invoke(Result.Error(it.message)) }
    }

    fun loadAndObserveUser(userID: String, observer: FirebaseReferenceValueObserver, b: ((Result<User>) -> Unit)) {
        firebaseDatabaseService.attachUserObserver(User::class.java, userID, observer, b)
    }

    fun loadAndObserveUserInfo(userID: String, observer: FirebaseReferenceValueObserver, b: ((Result<UserInfo>) -> Unit)) {
        firebaseDatabaseService.attachUserInfoObserver(UserInfo::class.java, userID, observer, b)
    }

    fun loadAndObserveUserNotifications(userID: String, observer: FirebaseReferenceValueObserver, b: ((Result<MutableList<UserNotification>>) -> Unit)){
        firebaseDatabaseService.attachUserNotificationsObserver(UserNotification::class.java, userID, observer, b)
    }

    fun loadAndObserveMessagesAdded(messagesID: String, observer: FirebaseReferenceChildObserver, b: ((Result<Message>) -> Unit)) {
        firebaseDatabaseService.attachMessagesObserver(Message::class.java, messagesID, observer, b)
    }

    fun loadAndObserveChat(chatID: String, observer: FirebaseReferenceValueObserver, b: ((Result<Chat>) -> Unit)) {
        firebaseDatabaseService.attachChatObserver(Chat::class.java, chatID, observer, b)
    }


}

