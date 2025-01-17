package com.traxtivemotor

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.MutableLiveData
import coil.compose.AsyncImage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.traxtivemotor.ui.theme.TraxtiveTheme
import kotlinx.coroutines.launch

class MotorcyclesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parseJson(baseContext,"traxtive-motor.json")

        enableEdgeToEdge()
        setContent {
            TraxtiveTheme {
                val name = remember { mutableStateOf("email") }
                val motorcyclesLiveData = remember { MutableLiveData<List<Motorcycle>>() }

                fetchFirebaseData(name, motorcyclesLiveData)

//                PagerAnimateToItem(name = name.value, motorcycles = motorcyclesLiveData)
            }
        }
    }
}

fun fetchFirebaseData(name: MutableState<String>, motorcycleLiveData: MutableLiveData<List<Motorcycle>>) {
    val database = Firebase.database
//    val userId = Firebase.auth.currentUser?.uid
    val motorcycleRef = database.getReference("motorcycles-v2")
//    val userRef = motorcycleRef.child(userId!!)
//    val userRef = motorcycleRef.child("000hoj3BEpgrvUIDwg7xhAr1vUu1")
//    Log.d("Firebase", "userId: $userRef")
//
//    userRef.addValueEventListener(object : ValueEventListener {
//        override fun onDataChange(snapshot: DataSnapshot) {
//            val motorcycles2 = mutableListOf<Motorcycle>()
//
//            for (motorcycleSnapshot in snapshot.children) {
//                val motorcycles = motorcycleSnapshot.getValue(Motorcycle::class.java)
//                motorcycles?.let {
//                    Log.d("Firebase", "Motorcycle: $it")
////                    name.value = it.brand ?: "Unknown"
//                    motorcycles2.add(it)
//                }
//            }
//            motorcycleLiveData.value = motorcycles2
//            Log.d("Firebase", "Motorcycles: $motorcycles2")
//        }
//
//        override fun onCancelled(error: DatabaseError) {
//            Log.w("Firebase", "Failed to read value.", error.toException())
//        }
//    })

    // Add motorcycle
//    val motorcycle = Motorcycle("Yamaha", "https://i.ibb.co/tQZThN2/yamaha.png", "Meow", "KWC123", userId)
//    if (userId != null) {
//        database.reference.child("motorcycles").child(userId).push().setValue(motorcycle)
//    }

//    val serviceUpdateRef = database.getReference("serviceUpdate-v2")
//    val userRef2 = serviceUpdateRef.child("000hoj3BEpgrvUIDwg7xhAr1vUu1")
//
//    userRef2.addValueEventListener(object : ValueEventListener {
//        override fun onDataChange(snapshot: DataSnapshot) {
//            for (motorcycleSnapshot in snapshot.children) {
//                val serviceUpdate = motorcycleSnapshot.getValue(ServiceUpdate::class.java)
//                serviceUpdate?.let {
//                    Log.d("Firebase", "Service update: $it")
//                }
//            }
//        }
//
//        override fun onCancelled(error: DatabaseError) {
//            Log.w("Firebase", "Failed to read value.", error.toException())
//        }
//    })

//    val profileRef = database.getReference("userProfile-v2")
//    val userRef3 = profileRef.child("000hoj3BEpgrvUIDwg7xhAr1vUu1")
//
//    userRef3.addValueEventListener(object : ValueEventListener {
//        override fun onDataChange(snapshot: DataSnapshot) {
//            val serviceUpdate = snapshot.getValue(Profile::class.java)
//            serviceUpdate?.let {
//                Log.d("Firebase", "Profile: $it")
//            }
//        }
//
//        override fun onCancelled(error: DatabaseError) {
//            Log.w("Firebase", "Failed to read value.", error.toException())
//        }
//    })

//    val servicesRef = database.getReference("services-v2")
//    val userRef4 = servicesRef.child("000hoj3BEpgrvUIDwg7xhAr1vUu1")
//
//    userRef4.addValueEventListener(object : ValueEventListener {
//        override fun onDataChange(snapshot: DataSnapshot) {
//            for (motorcycleSnapshot in snapshot.children) {
//                for (serviceSnapshot in motorcycleSnapshot.children) {
//                    println("\n\n\n")
//
//                    val serviceUpdate = serviceSnapshot.getValue(Service2::class.java)
//                    serviceUpdate?.let {
//                        println("- service: $it")
////                        Log.d("Firebase", "Motorcycle: $it")
//                    }
//                    for (itemSnapshot in serviceSnapshot.child("items").children) {
//                        val item = itemSnapshot.getValue(Item::class.java)
//                        item?.let {
//                            println("- item: $it")
//                        }
//                    }
//                    println("\n\n\n")
//                }
//            }
//        }
//
//        override fun onCancelled(error: DatabaseError) {
//            Log.w("Firebase", "Failed to read value.", error.toException())
//        }
//    })
}

@Composable
fun PagerAnimateToItem(name: String, motorcycles: MutableLiveData<List<Motorcycle>>) {
    val mContext = LocalContext.current
    val motorcyclesList by motorcycles.observeAsState()
    val motor = motorcyclesList ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp, bottom = 50.dp)
            .background(Color.Transparent)
    ) {
        val pagerState = rememberPagerState(pageCount = { motor.size })

        Column {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 3,
                contentPadding = PaddingValues(horizontal = 64.dp),
//                itemSpacing = (-32.dp),
                modifier = Modifier
                    .weight(0.5f)
                    .background(Color.Transparent)
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = Color.Blue,
                    )
            ) { page ->
                Box(
                    modifier = Modifier
                        .zIndex(page * 2f)
//                        .padding(start = 64.dp, end = 64.dp)
                        .graphicsLayer {
                            val startOffset = pagerState.startOffsetForPage(page)
                            translationX = size.width * (startOffset * .85f)

                            val blur = (startOffset * 20).coerceAtLeast(.1f)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                renderEffect = RenderEffect
                                    .createBlurEffect(
                                        blur, blur, Shader.TileMode.DECAL
                                    )
                                    .asComposeRenderEffect()
                            }

                            val scale = 1f - (startOffset * .1f)
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(16.dp)) // must be the last modifier
                        .width(200.dp)
                        .border(
                            width = 2.dp,
                            color = Color.Blue,
                        )
                        .clickable {
                            Log.d("Pager", "Clicked on page $page")
                            mContext.startActivity(Intent(mContext, ServiceDetails::class.java))
                        }
                ) {
                    Image(
                        painter = painterResource(id = locations[page].image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    motor[page].imageUrl?.let {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray,
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                        ) {
                            BikeImage(imageUrl = it)
                        }
                    }
                }

            }
            Row(
                modifier = Modifier
                    .weight(0.3f)
                    .padding(horizontal = 16.dp)
                    .height(86.dp),
            ) {
                val verticalState = rememberPagerState(pageCount = {
                    locations.count()
                })
                VerticalPager(
                    state = verticalState, modifier = Modifier
                        .weight(1f)
                        .height(86.dp),
                    userScrollEnabled = false, horizontalAlignment = Alignment.Start
                ) { page ->
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = locations[page].title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Thin,
                                fontSize = 28.sp
                            ),
                            modifier = Modifier.clickable {
                                Log.d("Firebase", "Clicked")
                                mContext.startActivity(Intent(mContext, AddNewService::class.java))
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = locations[page].subtitle,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        motorcyclesList?.get(page)?.brand?.let { brand ->
                            Text(
                                text = brand,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    snapshotFlow {
                        Pair(
                            pagerState.currentPage,
                            pagerState.currentPageOffsetFraction
                        )
                    }.collect { (page, offset) ->
                        verticalState.scrollToPage(page, offset)
                    }
                }
            }
        }

        val coroutineScope = rememberCoroutineScope()
        Button(onClick = {
            coroutineScope.launch {
                pagerState.animateScrollToPage(5)
            }
        }, modifier = Modifier.align(Alignment.BottomCenter)) {
            Text("Jump to Page 5")
        }
    }
}

@Composable
fun BikeImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Bike Image",
        modifier = Modifier.size(200.dp),
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true)
@Composable
fun PagerPreview() {
    TraxtiveTheme {
        PagerAnimateToItem(name = "sss", motorcycles = MutableLiveData())
    }
}

fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction

fun PagerState.startOffsetForPage(page: Int) = offsetForPage(page).coerceAtLeast(0f)