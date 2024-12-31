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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.ktx.Firebase
import com.traxtivemotor.ui.theme.TraxtiveTheme
import kotlinx.coroutines.launch
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database

data class Motorcycle(
    val brand: String? = null,
    val imageUrl: String? = null,
    val model: String? = null,
    val plateNumber: String? = null,
    val userId: String? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraxtiveTheme {
                val name = remember { mutableStateOf("email") }

//                Scaffold(
//                    modifier = Modifier.fillMaxSize(),
////                    containerColor = MaterialTheme.colorScheme.background
//                ) { innerPadding ->
//                    PagerAnimateToItem(name = name.value, modifier = Modifier.padding(innerPadding))
//                }
                PagerAnimateToItem(name = name.value)

                val database = Firebase.database
                val myRef = database.getReference("motorcycles")

                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userSnapshot in snapshot.children) {
                            for (motorcycleSnapshot in userSnapshot.children) {
                                val motorcycle = motorcycleSnapshot.getValue(Motorcycle::class.java)
                                motorcycle?.let {
                                    Log.d("Firebase", "Motorcycle: ${it.brand}")
                                    name.value = it.brand ?: "Unknown"
                                }
                                return
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Firebase", "Failed to read value.", error.toException())
                    }
                })
            }
        }
    }
}

@Composable
fun PagerAnimateToItem(name: String) {
    val mContext = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 16.dp)
            .background(Color.Transparent)
    ) {
        val pagerState = rememberPagerState(pageCount = { locations.count() })

        Column {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 2,
                modifier = Modifier
                    .weight(0.7f)
                    .background(Color.Transparent)
                    .fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .zIndex(page * 2f)
                        .padding(start = 64.dp, end = 32.dp)
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
                        .clickable {
                            Log.d("Pager", "Clicked on page $page")
                            mContext.startActivity(Intent(mContext, SignIn::class.java))
                        }
                ) {
                    Image(
                        painter = painterResource(id = locations[page].image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

            }
            Row(modifier = Modifier
                .weight(0.3f)
                .padding(horizontal = 16.dp)
//                .fillMaxWidth()
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
                            )
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
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
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

@Preview(showBackground = true)
@Composable
fun PagerPreview() {
    TraxtiveTheme {
        PagerAnimateToItem(name = "sss")
//        Scaffold(
//            modifier = Modifier.fillMaxSize(),
//            containerColor = MaterialTheme.colorScheme.background
//        ) { innerPadding ->
//
//        }
    }
}

fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction

fun PagerState.startOffsetForPage(page: Int) = offsetForPage(page).coerceAtLeast(0f)