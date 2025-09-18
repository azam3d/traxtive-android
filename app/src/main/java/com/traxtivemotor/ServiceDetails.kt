package com.traxtivemotor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.traxtivemotor.ui.theme.TraxtiveTheme

class ServiceDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val motorIdFromIntent = intent.getStringExtra("motorId")
        val motorFromIntent = intent.getParcelableExtra<Motorcycle>("motor")
        val serviceFromIntent = intent.getParcelableExtra<Service2>("service")
        val serviceIdFromIntent = intent.getStringExtra("serviceId")

        setContent {
            TraxtiveTheme(dynamicColor = false) {
                var serviceState by remember { mutableStateOf(serviceFromIntent) }

                // This launcher handles the result from AddNewService
                val editServiceLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val updatedService = result.data?.getParcelableExtra<Service2>("updatedService")
                        if (updatedService != null) {
                            serviceState = updatedService // Update the state to trigger recomposition
                        }
                        // serviceIdFromIntent remains the ID for this detail screen
                    }
                }

                // Ensure required IDs are present, otherwise this screen is not valid
                if (motorIdFromIntent == null || serviceIdFromIntent == null) {
                    // Handle error, perhaps finish activity or show an error message
                    Text("Error: Missing motor or service ID.")
                    return@TraxtiveTheme
                }
                val currentUserId = Firebase.auth.currentUser?.uid
                if (currentUserId == null) {
                    Text("Error: User not logged in.")
                    return@TraxtiveTheme
                }


                TopBarNavigation(navigateBack = { finish() })
                ServiceItems(motorFromIntent, serviceState, currentUserId, motorIdFromIntent, serviceIdFromIntent)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    TextButton(
                        onClick = {
                            println("Edit button clicked")
                            val intent = Intent(this@ServiceDetails, AddNewService::class.java)
                            intent.putExtra("motorId", motorIdFromIntent)
                            intent.putExtra("motor", motorFromIntent)
                            intent.putExtra("serviceId", serviceIdFromIntent) // Pass the ID of the service to edit
                            intent.putExtra("service", serviceState)       // Pass current service data for prefilling
                            editServiceLauncher.launch(intent)
                        },
                        modifier = Modifier.align(Alignment.TopEnd),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Edit", fontSize = 16.sp)
                    }
                }
//            ChatScreen() // Commented out as per original
            }
        }
    }

    @Composable
    fun ServiceItems(motor: Motorcycle?, service: Service2?, userId: String, motorId: String, serviceId: String) {
        val mContext = LocalContext.current
        var showDialog by remember { mutableStateOf(false) }

        Column(
            // Removed key = service, recomposition handled by serviceState
            modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
        ) {
            MotorHeader(motor)

            // Service Date and Workshop
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.LightGray)
                    .background(Color.White)
            ) {
                Row(modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            text = service?.date ?: "N/A",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = service?.workshop ?: "N/A",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp)
                        )
                    }
                    Text(
                        text = "${service?.mileage ?: "0"} km",
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Service Descriptions and Total
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.LightGray)
                    .background(Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp), // Use heightIn for flexibility
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            "Service Descriptions",
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        )
                    }
                    val itemsList = service?.items ?: emptyList()
                    if (itemsList.isEmpty()) {
                        item {
                            Text(
                                "No service items recorded.",
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(itemsList.size) { index ->
                            ServiceItemCard(index, itemsList[index])
                        }
                    }
                    item {
                        Row(modifier = Modifier
                            .padding(vertical = 16.dp)
                            .padding(start = 4.dp, end = 4.dp, top = 8.dp) // Adjusted padding
                            .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "RM${service?.total ?: "0.00"}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp)
                            )
                        }
                    }
                }
            }

            // Remarks
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.LightGray)
                    .background(Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Remarks",
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                    Text(
                        text = service?.remark?.takeIf { it.isNotBlank() } ?: "No remarks.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Remove Service Button
            TextButton(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Red
                )
            ) {
                Text("Remove this service")
            }

            // Confirmation Dialog for Remove
            if (showDialog) { // More idiomatic way to show/hide dialog
                ConfirmationDialog(
                    showDialog = showDialog, // This prop might be redundant if controlling visibility here
                    onDismiss = { showDialog = false },
                    onYesClick = {
                        val database = Firebase.database
                        database.reference.child("services").child(userId).child(motorId).child(serviceId).removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Optionally, set a result for a calling activity if ServiceDetails itself was launched for result
                                    (mContext as? Activity)?.setResult(Activity.RESULT_OK) // Indicate success
                                    (mContext as? Activity)?.finish() // Close ServiceDetails
                                } else {
                                    // Handle error
                                    println("Error removing service: ${task.exception?.message}")
                                    showDialog = false // Still dismiss dialog
                                }
                            }
                    },
                    onNoClick = {
                        showDialog = false
                        println("User clicked No on remove service")
                    },
                    title = "Remove Service",
                    text = "Are you sure you want to remove this service?",
                    confirmText = "Remove Service",
                    dismissText = "Cancel"
                )
            }
        }
    }

    // --- ChatScreen and related composables (unchanged as per original structure) ---
    data class ChatMessage(
        val message: String,
        val isFromUser: Boolean
    )

    @Composable
    fun ChatScreen() {
        var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
        var currentMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = currentMessage,
                    onValueChange = { currentMessage = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Type a message") }
                )
                IconButton(
                    onClick = {
                        if (currentMessage.isNotBlank()) {
                            messages = messages + ChatMessage(currentMessage, true)
                            currentMessage = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message"
                    )
                }
            }
        }
    }

    @Composable
    fun ChatBubble(message: ChatMessage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isFromUser)
                Arrangement.End else Arrangement.Start
        ) {
            Surface(
                color = if (message.isFromUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = message.message,
                    modifier = Modifier.padding(16.dp),
                    color = if (message.isFromUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun LoginPreview() { // Renamed from ServiceDetailsPreview to avoid conflict if that exists
        TraxtiveTheme {
            // Provide mock data for preview
            ChatScreen() // Original preview was ChatScreen
        }
    }
}

@Composable
fun ServiceItemCard(index: Int, item: Item?) { // Assuming Item is compatible with Item2 or vice-versa
    Row(modifier = Modifier
        .padding(vertical = 8.dp)
        .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${index + 1}. ${item?.name ?: "N/A"}",
            modifier = Modifier.padding(4.dp)
        )
        Text(
            text = "RM${item?.price ?: "0.00"}",
            modifier = Modifier.padding(4.dp)
        )
    }
    HorizontalDivider(thickness = 1.dp, color = Color(230, 239, 252, 255))
}
