package com.traxtivemotor

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.traxtivemotor.ui.theme.TraxtiveTheme

class MotorDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra("userId")!!
        val motorId = intent.getStringExtra("motorId")!!
        val motor = intent.getParcelableExtra("motor") as Motorcycle?
        println(userId)
        println(motorId)
        println(motor)

        setContent {
            TraxtiveTheme(dynamicColor = false) {
                val servicesLiveData = remember { MutableLiveData<List<Service2>>() }
                val serviceId = remember { MutableLiveData<List<String>>() }

                TopBarNavigation(navigateBack = { finish() })
                fetchFirebaseData(userId, motorId, servicesLiveData, serviceId)
                AllServices(motor, servicesLiveData, serviceId)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopBarNavigation(navigateBack: () -> Unit) {
    Scaffold(
        containerColor = Color(230, 239, 252, 255),
        topBar = { AppBarSelectionActions("", navigateBack = navigateBack) },
    ) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarSelectionActions(
    title: String?,
    navigateBack: () -> Unit
) {
    val topBarText = title
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.Black, navigationIconContentColor = Color.Black),
        title = {
            Text(title!!)
        },
        navigationIcon = {
            IconButton(onClick = { navigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        },
    )
}

fun fetchFirebaseData(userId: String, motorId: String, servicesLiveData: MutableLiveData<List<Service2>>, serviceIdsLiveData: MutableLiveData<List<String>>) {
    println("fetchFirebaseData")

    val database = Firebase.database
    val servicesRef = database.getReference("services")
    val userRef = servicesRef.child(userId)

    userRef.child(motorId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var totalCount = 0
            var bigTotal = 0
            val services = mutableListOf<Service2>()
            val serviceIds = mutableListOf<String>()

            for (serviceSnapshot in snapshot.children) {
                totalCount++
                if (totalCount > bigTotal) {
                    bigTotal = totalCount
                }
                println("Service ID: ${serviceSnapshot.key}")
                serviceIds.add(serviceSnapshot.key!!)

                val serviceUpdate2 = serviceSnapshot.getValue(Service2::class.java)
                println("- serviceUpdate2: $serviceUpdate2")
                services.add(serviceUpdate2 ?: Service2())

//                    println("\n\n\n")
            }
            totalCount = 0
            servicesLiveData.value = services
            serviceIdsLiveData.value = serviceIds

            println("bigTotal: $bigTotal")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Firebase", "Failed to read value.", error.toException())
        }
    })
}

@Composable
fun AllServices(motor: Motorcycle?, servicesLiveData: MutableLiveData<List<Service2>>, serviceIds: MutableLiveData<List<String>>) {
    val mContext = LocalContext.current
    val services by servicesLiveData.observeAsState(initial = emptyList())

    Column(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(top = 80.dp)
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        MotorHeader(motor)

        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.LightGray)
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Service History",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        )
                    }
                }
                services?.let {
                    items(it.size) { index ->
                        val service = it[index]
                        PlantCard(motor, service, serviceIds.value?.get(index) ?: "")
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                val intent = Intent(mContext, AddNewService::class.java)
                intent.putExtra("motor", motor)
                mContext.startActivity(intent)
            }, modifier = Modifier
                .padding(top = 24.dp)
                .size(width = 240.dp, height = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255))) {
                Text("+ Add New Service")
            }

            TextButton(
                modifier = Modifier
                    .padding(top = 8.dp)
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
fun MotorHeader(motor: Motorcycle?) {
    Row() {
        if (motor?.imageUrl != null) {
            BikeImage(
                motor.imageUrl,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Default.Motorcycle,
                    contentDescription = "Motorbike",
                    tint = Color.Blue
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = motor?.brand + " " + motor?.model,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            motor?.plateNumber?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PlantCard(motor: Motorcycle?, service: Service2, serviceId: String) {
    val mContext = LocalContext.current

    Row(modifier = Modifier
        .padding(vertical = 8.dp)
        .fillMaxWidth()
        .clickable {
            println(serviceId)

            val intent = Intent(mContext, ServiceDetails::class.java)
            intent.putExtra("serviceId", serviceId)
            intent.putExtra("service", service)
            intent.putExtra("motor", motor)
            mContext.startActivity(intent)
        },
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(
                text = service.date!!,
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = service.workshop!!,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
        Text(
            text = "${service.mileage} km",
            modifier = Modifier
                .padding(4.dp)
        )
    }
    HorizontalDivider(thickness = 1.dp, color = Color(230, 239, 252, 255))
}
