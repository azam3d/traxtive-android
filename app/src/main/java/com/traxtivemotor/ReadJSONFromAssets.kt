package com.traxtivemotor

import android.content.Context
import android.os.Parcelable
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.BufferedReader
import java.io.InputStreamReader
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Motorcycle(
    val motorId: String? = null,
    val brand: String? = null,
    val imageUrl: String? = null,
    val model: String? = null,
    val plateNumber: String? = null,
    val userId: String? = null
) : Parcelable

data class Motorcycle2(
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

data class Profile2(
    val dateOfBirth: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val gender: String? = null,
    val mobileNumber: String? = null,
    val userName: String? = null
)

data class ServiceUpdate(
    val date: String? = null,
    val motorId: String? = null,
    val nextMileage: String? = null,
)

data class ServiceUpdate2(
    val date: String? = null,
    val nextMileage: String? = null,
)

@Parcelize
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
    val year: String? = null
): Parcelable

data class Item(
    val name: String? = null,
    val price: String? = null
)

data class Service2(
    val date: String? = null,
    val mileage: String? = null,
    val total: String? = null,
    val remark: String? = null,
    val workshop: String? = null,
    val year: String? = null,
//    val prices: List<Item>? = null
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
        val database = Firebase.database

        run lit@{
//            motorcyclesData.motorcycles.forEach { (userId, motorcycleMap) ->
//                println("User ID: $userId")
//                totalCount += motorcycleMap.size
//
//                motorcycleMap.forEach { (key, motorcycle) ->
//                    println("- key: $key")
//                    println("- motorcycle: $motorcycle")
//
//                    val motor = Motorcycle(motorcycle.brand, motorcycle.imageUrl, motorcycle.model, motorcycle.plateNumber)
//                    database.reference.child("motorcycles").child(userId).child(key).setValue(motor)
//                }
////                addMotor(userId)
////                return@lit
//            }

//            motorcyclesData.userProfile.forEach { (userId, userMap) ->
//                println("User ID: $userId")
//
//                userMap.forEach { (userId2, profile) ->
//                    println("- user ID 2: $userId2")
//                    println("- profile: $profile")
//
//                    val profile2 = Profile2(profile.dateOfBirth, profile.email, profile.fullName, profile.gender, profile.mobileNumber, profile.userName)
//                    database.reference.child("userProfile").child(userId).setValue(profile2) // remove userId
//                }
////                return@lit
//            }

//            motorcyclesData.serviceUpdate.forEach { (userId, motorMap) ->
//                println("userId serviceUpdate: $userId")
//
//                motorMap.forEach { (motorId, motorMap2) ->
//                    println("- motorId: $motorId")
//
//                    motorMap2.forEach { (motor, serviceUpdate) ->
//                        println("  - motor: $motor")
//                        println("  - serviceUpdate: $serviceUpdate")
//
//                        val update = ServiceUpdate2(serviceUpdate.date, serviceUpdate.nextMileage)
//                        database.reference.child("serviceUpdate").child(userId).child(motorId).setValue(update)
//                    }
//                }
////                return@lit
//            }

            motorcyclesData.services.forEach { (userId, motorcycleMap) ->
                println("\n\n\n")
                println("userId service: $userId")

                motorcycleMap.forEach { (motorId, serviceMap) ->
                    println("- motorId: $motorId")

                    serviceMap.forEach { (serviceId, service) ->
                        println("  - key: $serviceId")
                        println("  - service: $service")
                        println("  - price01: ${service.price01?.isBlank()}")
                        println("  - price05: ${service.price05?.isBlank()}")

                        val service2 = Service2(service.date, service.mileage, service.price, service.remark, service.workshop, service.year)
                        database.reference.child("services").child(userId).child(motorId).child(serviceId).setValue(service2)

                        val serviceList = mutableListOf<Item>()
                        if (service.service01?.isNotBlank() == true) {
                            val item01 = Item(service.service01, service.price01!!)
                            println(item01)
                            serviceList.add(item01)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item01)
                        }
                        if (service.service02?.isNotBlank() == true) {
                            val item02 = Item(service.service02, service.price02!!)
                            println(item02)
                            serviceList.add(item02)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item02)
                        }
                        if (service.service03?.isNotBlank() == true) {
                            val item03 = Item(service.service03, service.price03!!)
                            println(item03)
                            serviceList.add(item03)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item03)
                        }
                        if (service.service04?.isNotBlank() == true) {
                            val item04 = Item(service.service04, service.price04!!)
                            println(item04)
                            serviceList.add(item04)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item04)
                        }
                        if (service.service05?.isNotBlank() == true) {
                            val item05 = Item(service.service05, service.price05!!)
                            println(item05)
                            serviceList.add(item05)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item05)
                        }
                        if (service.service06?.isNotBlank() == true) {
                            val item06 = Item(service.service06, service.price06!!)
                            println(item06)
                            serviceList.add(item06)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item06)
                        }
                        if (service.service07?.isNotBlank() == true) {
                            val item07 = Item(service.service07, service.price07!!)
                            println(item07)
                            serviceList.add(item07)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item07)
                        }
                        if (service.service08?.isNotBlank() == true) {
                            val item08 = Item(service.service08, service.price08!!)
                            println(item08)
                            serviceList.add(item08)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item08)
                        }
                        if (service.service09?.isNotBlank() == true) {
                            val item09 = Item(service.service09, service.price09!!)
                            println(item09)
                            serviceList.add(item09)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item09)
                        }
                        if (service.service10?.isNotBlank() == true) {
                            val item10 = Item(service.service10, service.price10!!)
                            println(item10)
                            serviceList.add(item10)

                            database.reference.child("services").child(userId).child(motorId).child(serviceId).child("items").push().setValue(item10)
                        }
                        println(serviceList)
                    }
                }
                println("\n\n\n")
//                return@lit
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