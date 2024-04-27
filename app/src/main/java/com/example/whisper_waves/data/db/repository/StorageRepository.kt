package com.example.whisper_waves.data.db.repository

import android.net.Uri
import com.example.whisper_waves.data.db.remote.FirebaseStorageSource
import com.example.whisper_waves.data.Result

class StorageRepository {
    private val firebaseStorageService = FirebaseStorageSource()

    fun updateUserProfileImage(userID: String, byteArray: ByteArray, b: (Result<Uri>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.uploadUserImage(userID, byteArray).addOnSuccessListener {
            b.invoke((Result.Success(it)))
        }.addOnFailureListener {
            b.invoke(Result.Error(it.message))
        }
    }


    fun updateUserProfileImage1(userID: String, byteArray: ByteArray, b: (Result<Uri>) -> Unit) {
        val filename = "sent_image_${System.currentTimeMillis()}.jpg" // Generate a unique filename
        b.invoke(Result.Loading)
        firebaseStorageService.uploadUserImage1(userID, filename, byteArray).addOnSuccessListener {
            b.invoke((Result.Success(it)))
        }.addOnFailureListener {
            b.invoke(Result.Error(it.message))
        }
    }

    fun uploadUserAudio(userID: String, byteArray: ByteArray, b: (Result<Uri>) -> Unit) {
        val filename = "sent_audio_${System.currentTimeMillis()}.mp3" // Generate a unique filename
        b.invoke(Result.Loading)
        firebaseStorageService.uploadUserAudio1(userID, filename, byteArray).addOnSuccessListener {
            b.invoke((Result.Success(it)))
        }.addOnFailureListener {
            b.invoke(Result.Error(it.message))
        }
    }
}