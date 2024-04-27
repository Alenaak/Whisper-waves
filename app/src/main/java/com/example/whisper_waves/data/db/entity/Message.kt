package com.example.whisper_waves.data.db.entity

import com.google.firebase.database.PropertyName
import java.util.*


data class Message(
    @get:PropertyName("senderID") @set:PropertyName("senderID") var senderID: String = "",
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("imageRef") @set:PropertyName("imageRef") var imageRef: String? = null,
    @get:PropertyName("audioRef") @set:PropertyName("audioRef") var audioRef: String? = null,
    @get:PropertyName("messageType") @set:PropertyName("messageType") var messageType: MessageType = MessageType.TEXT,
    @get:PropertyName("epochTimeMs") @set:PropertyName("epochTimeMs") var epochTimeMs: Long = Date().time,
    @get:PropertyName("seen") @set:PropertyName("seen") var seen: Boolean = false,
    @get:PropertyName("status") @set:PropertyName("status") var status: MessageStatus = MessageStatus.SENT,
    val id: String = UUID.randomUUID().toString()
) {
    enum class MessageStatus {
        SENT,
        DELIVERED,
        SEEN ,
        NOT_SENT
    }
    enum class MessageType {
        TEXT,
        IMAGE
    }
}
