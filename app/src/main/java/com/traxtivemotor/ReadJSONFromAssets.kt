package com.traxtivemotor

import android.content.Context
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.BufferedReader
import java.io.InputStreamReader
import com.google.gson.Gson

data class Motorcycle(
    val brand: String? = null,
    val imageUrl: String? = null,
    val model: String? = null,
    val plateNumber: String? = null,
    val userId: String? = null
)

data class Traxtive(
    val motorcycles: Map<String, Map<String, Motorcycle>>,
    val serviceUpdate: Map<String, Map<String, Map<String, ServiceUpdate>>>,
    val services: Map<String, Map<String, Map<String, Service>>>,
    val userProfile: Map<String, Map<String, Profile>>
)

data class Profile(
    val dateOfBirth: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val gender: String? = null,
    val mobileNumber: String? = null,
    val userId: String? = null,
    val userName: String? = null
)

data class ServiceUpdate(
    val date: String? = null,
    val motorId: String? = null,
    val nextMileage: String? = null,
)

data class Service(
    val date: String? = null,
    val mileage: String? = null,
    val motorId: String? = null,
    val price: String? = null,
    val price01: String? = null,
    val price02: String? = null,
    val price03: String? = null,
    val price04: String? = null,
    val price05: String? = null,
    val price06: String? = null,
    val price07: String? = null,
    val price08: String? = null,
    val price09: String? = null,
    val price10: String? = null,
    val remark: String? = null,
    val service01: String? = null,
    val service02: String? = null,
    val service03: String? = null,
    val service04: String? = null,
    val service05: String? = null,
    val service06: String? = null,
    val service07: String? = null,
    val service08: String? = null,
    val service09: String? = null,
    val service10: String? = null,
    val workshop: String? = null,
    val year: String? = null,
)

fun parseJson(context: Context, path: String) {
    val identifier = "Firebase"

    try {
        val file = context.assets.open(path)
        Log.d(identifier, "Found File: $file.")

        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()

        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
            }
        }
        Log.d(identifier, "getJSON stringBuilder: $stringBuilder.")

        val jsonString = stringBuilder.toString()
        Log.d(identifier, "JSON as String: $jsonString.")

        val gson = Gson()
        val motorcyclesData = gson.fromJson(jsonString, Traxtive::class.java)

        var totalCount = 0
        run lit@{
//            motorcyclesData.motorcycles.forEach { (userId, motorcycleMap) ->
//                println("User ID: $userId")
//                totalCount += motorcycleMap.size
//                motorcycleMap.forEach { (key, motorcycle) ->
//                    println("Key: $key")
//                    println("Motorcycle: $motorcycle")
//                }
////                addMotor(userId)
////                return@lit
//            }
//            motorcyclesData.userProfile.forEach { (userId, userMap) ->
//                println("User ID: $userId")
//
//        //                userMap.forEach { userId2 ->
//        //                    println("user ID 2: $userId2")
//        //                }
//            }
            motorcyclesData.serviceUpdate.forEach { (motorId, motorMap) ->
                println("Motor ID serviceUpdate: $motorId")
            }
            motorcyclesData.services.forEach { (motorId, motorMap) ->
                println("Motor ID service: $motorId")
            }
        }
//        println("totalCount: $totalCount")
    } catch (e: Exception) {
        Log.d(identifier, "Error reading JSON: $e.")
        e.printStackTrace()
    }
}

fun addMotor(userId: String) {
    val database = Firebase.database
    val motorcycle = Motorcycle("Yamaha", "https://i.ibb.co/tQZThN2/yamaha.png", "Meow", "KWC123", userId)
    database.reference.child("motorcycles").child(userId).push().setValue(motorcycle)
}