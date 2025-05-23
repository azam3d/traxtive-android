package com.traxtivemotor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.traxtivemotor.ui.theme.TraxtiveTheme

class ServiceDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val motorId = intent.getStringExtra("motorId")
        val motor = intent.getParcelableExtra("motor") as Motorcycle?
        println("motorId: $motorId")
        println("motor: $motor")

        setContent {
            TraxtiveTheme(dynamicColor = false) {
                val serviceId = intent.getStringExtra("serviceId")
                val service = intent.getParcelableExtra("service") as Service2?

                println("serviceId: $serviceId")
                println("service date: ${service?.date}\n")
                println("service items: ${service?.items}\n")
                println("service mileage: ${service?.mileage}\n")
                println("service total: ${service?.total}\n")
                println("service remark: ${service?.remark}\n")
                println("service workshop: ${service?.workshop}\n\n\n")

                TopBarNavigation(navigateBack = { finish() })
                ServiceItems(motor, service)
//            ChatScreen()
            }
        }
    }

    @Composable
    fun ServiceItems(motor: Motorcycle?, service: Service2?) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
        ) {
            MotorHeader(motor)

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
                            text = service?.date!!,
                            style = MaterialTheme.typography.bodySmall,
                        )

                        Text(
                            text = service?.workshop!!,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp)
                        )
                    }

                    Text(
                        text = "${service?.mileage!!} km",
                        modifier = Modifier
                            .padding(4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.LightGray)
                    .background(Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
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
                                "Service Descriptions",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            )
                        }
                    }
                    if (service != null) {
                        service.items?.let { map ->
                            val itemList = map.values.toList()

                            items(itemList.size) { index ->
                                ServiceItemCard(index, itemList[index])
                            }
                        }
                    }
                    item {
                        Row(modifier = Modifier
                            .padding(vertical = 16.dp)
                            .padding(4.dp)
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
                                text = "RM${service?.total}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp)
                            )
                        }
                    }
                }
            }

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
                        modifier = Modifier
                            .padding(start = 8.dp),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )

                    Text(
                        text = "Next service 125456km",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }

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
            // Chat messages
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

            // Message input
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
    fun LoginPreview() {
        TraxtiveTheme {
            ChatScreen()
        }
    }
}

@Composable
fun ServiceItemCard(index: Int, item: Item?) {
    Row(modifier = Modifier
        .padding(vertical = 8.dp)
        .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${index + 1}. ${item?.name}",
            modifier = Modifier
                .padding(4.dp)
        )
        Text(
            text = "RM${item?.price}",
            modifier = Modifier
                .padding(4.dp)
        )
    }
    HorizontalDivider(thickness = 1.dp, color = Color(230, 239, 252, 255))
}
