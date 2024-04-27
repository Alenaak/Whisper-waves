package com.example.whisper_waves.data.model

import com.example.whisper_waves.data.db.entity.Chat
import com.example.whisper_waves.data.db.entity.UserInfo

data class ChatWithUserInfo(
    var mChat: Chat,
    var mUserInfo: UserInfo
)
