package com.traxtivemotor

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseImageManager {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    // Method 1: Upload with callbacks
    fun uploadImage(
        imageUri: Uri,
        motorId: String?,
        folder: String = "service_receipt",
        onProgress: (Double) -> Unit = {},
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUserId = Firebase.auth.currentUser?.uid
        val filename = "${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child("$folder/$currentUserId/$motorId/$filename")

        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            onProgress(progress)
        }.addOnSuccessListener {
            // Get download URL
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
                Log.d("FirebaseImageManager", "Image uploaded successfully: $uri")
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    // Method 2: Upload with coroutines (modern approach)
    suspend fun uploadImageSuspend(
        imageUri: Uri,
        folder: String = "images"
    ): String {
        val filename = "${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child("$folder/$filename")

        // Upload the file
        imageRef.putFile(imageUri).await()

        // Get and return the download URL
        return imageRef.downloadUrl.await().toString()
    }

    // Method 3: Upload byte array (useful for compressed images)
    suspend fun uploadImageBytes(
        byteArray: ByteArray,
        folder: String = "images"
    ): String {
        val filename = "${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child("$folder/$filename")

        // Upload bytes
        imageRef.putBytes(byteArray).await()

        // Get and return the download URL
        return imageRef.downloadUrl.await().toString()
    }

    // Delete image
    suspend fun deleteImage(imageUrl: String) {
        val imageRef = storage.getReferenceFromUrl(imageUrl)
        imageRef.delete().await()
    }

    // Download image to local file
    suspend fun downloadImage(imageUrl: String, localFile: java.io.File) {
        val imageRef = storage.getReferenceFromUrl(imageUrl)
        imageRef.getFile(localFile).await()
    }
}