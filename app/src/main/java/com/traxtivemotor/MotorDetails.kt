package com.traxtivemotor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.traxtivemotor.ui.theme.TraxtiveTheme

class MotorDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TraxtiveTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(230, 239, 252, 255)
                ) {
                    val servicesLiveData = remember { MutableLiveData<List<Service>>() }

                    fetchFirebaseData(servicesLiveData)
                    AllServices(servicesLiveData)
                }
            }
        }
    }
}

fun fetchFirebaseData(servicesLiveData: MutableLiveData<List<Service>>) {
    val database = Firebase.database

    println("fetchFirebaseData")

//    val userId = Firebase.auth.currentUser?.uid
//    val userRef = motorcycleRef.child(userId!!)

    val servicesRef = database.getReference("services")
    val userRef = servicesRef.child("w5uBnQl7GOdCDSYABBtqPOhjJRr1")

    userRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var totalCount = 0
            var bigTotal = 0
            val services = mutableListOf<Service>()

            for (motorcycleSnapshot in snapshot.children) {
                for (serviceSnapshot in motorcycleSnapshot.children) {
                    totalCount++
                    if (totalCount > bigTotal) {
                        bigTotal = totalCount
                    }
                    val serviceUpdate = serviceSnapshot.getValue(Service::class.java)
                    serviceUpdate?.let {
                        println("- service: $it")
                        services.add(it)
                    }
                    println("\n\n\n")
                }
                totalCount = 0
            }
            servicesLiveData.value = services
            println("bigTotal: $bigTotal")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Firebase", "Failed to read value.", error.toException())
        }
    })
}

@Composable
fun AllServices(servicesLiveData: MutableLiveData<List<Service>>) {
    val mContext = LocalContext.current
    val services by servicesLiveData.observeAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxWidth()
        .wrapContentHeight()
        .padding(vertical = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Yamaha Lagenda",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Thin,
                fontSize = 32.sp
            )
        )

        Box(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        Log.d("Firebase", "Clicked")
                        mContext.startActivity(Intent(mContext, ServiceDetails::class.java))
                    },
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 25.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Service History",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Thin,
                                fontSize = 32.sp
                            )
                        )
                    }
                }
                services?.let {
                    items(it.size) { index ->
                        val service = it[index]
                        PlantCard(service.date!!, service.workshop!!, service.mileage!!)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
//                Add New Service
            }, modifier = Modifier
                .padding(top = 24.dp)
                .size(width = 240.dp, height = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255))) {
                Text("+ Add New Service")
            }

            TextButton(
                modifier = Modifier.padding(top = 8.dp)
                    .size(width = 240.dp, height = 48.dp),
                onClick = {
//                Delete Motorcycle
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Red
                )
            ) {
                Text("Delete Motorcycle")
            }
        }
    }
}

@Composable
fun PlantCard(date: String, workshop: String, mileage: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = workshop,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                )
            )
        }
        Text(
            text = mileage,
            modifier = Modifier
                .padding(8.dp)
        )
    }
    HorizontalDivider(thickness = 1.dp, color = Color(230, 239, 252, 255))
}
