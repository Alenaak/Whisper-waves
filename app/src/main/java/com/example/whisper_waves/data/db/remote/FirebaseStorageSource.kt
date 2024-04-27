package com.example.whisper_waves.data.db.remote

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata


class FirebaseStorageSource {
    private val storageInstance = FirebaseStorage.getInstance()

    fun uploadUserImage(userID: String, bArr: ByteArray): Task<Uri> {
        val path = "user_photos/$userID/profile_image"
        val ref = storageInstance.reference.child(path)

        return ref.putBytes(bArr).continueWithTask {
            ref.downloadUrl
        }
    }

    fun uploadUserImage1(userID: String, filename: String, bArr: ByteArray): Task<Uri> {
        val path = "user_photos/$userID/$filename"
        val ref = storageInstance.reference.child(path)

        return ref.putBytes(bArr).continueWithTask {
            ref.downloadUrl
        }
    }
    fun uploadUserAudio1(userID: String, filename: String, bArr: ByteArray): Task<Uri> {
        val metadata = StorageMetadata.Builder()
            .setContentType("audio/mpeg") // Set the Content-Type to specify the MIME type of the file
            .build()
        val path = "user_photos/$userID/$filename"
        val ref = storageInstance.reference.child(path)
        return ref.putBytes(bArr, metadata).continueWithTask {
            ref.downloadUrl

        }

    }




}