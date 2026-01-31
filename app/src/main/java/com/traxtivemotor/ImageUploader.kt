package com.traxtivemotor

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class ImageUploader {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    fun uploadImage(imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        // Create a unique filename
        val filename = "images/${System.currentTimeMillis()}.jpg"
        val imageRef = storageRef.child(filename)

        // Upload the file
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}