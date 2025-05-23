package com.traxtivemotor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.AsyncImage

class PhotoViewer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("PhotoViewer")
        setContent {
//            addPhotoViewer()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun PhotoViewerScreen(
        imageUri: String,
        navController: NavController,
        onDeleteClick: () -> Unit
    ) {
        var scale by remember { mutableStateOf(1f) }
        var rotation by remember { mutableStateOf(0f) }
        var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

        val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
            scale = (scale * zoomChange).coerceIn(0.5f..5f)
            rotation += rotationChange
            offset += offsetChange
        }

        // Enter transition animation
        val enterTransition = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300)
        )

        // Exit transition animation
        val exitTransition = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 300)
        )

        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .systemBarsPadding()
        ) {
            // Image with zoom and rotation
            AsyncImage(
                model = imageUri,
                contentDescription = "Zoomable photo",
                modifier = Modifier
//                    .fillMaxSize()
//                    .transformable(state = state)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        rotationZ = rotation,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )

            // Delete button
            Button(
                onClick = {
                    // Animate out before deleting
                    navController.previousBackStackEntry?.let {
                        onDeleteClick()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete photo",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Delete")
            }
        }
    }

    fun NavGraphBuilder.addPhotoViewer() {
        composable(
            route = "photo/{photoId}",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(300))
            }
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId")

//            PhotoViewerScreen(
//                imageUri = "content://media/external/images/$photoId",
//                navController = navController,
//                onDeleteClick = { /* Handle delete */ }
//            )
        }
    }
}