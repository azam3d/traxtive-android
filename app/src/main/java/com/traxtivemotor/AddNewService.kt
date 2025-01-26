@file:JvmName("AddNewServiceKt")

package com.traxtivemotor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.traxtivemotor.ui.theme.TraxtiveTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class AddNewService : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraxtiveTheme(dynamicColor = false) {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ServiceForm(
                        onBackPressed = { finish() },
                        onSubmit = { serviceFormData ->
                            println(serviceFormData.serviceDate)
                            println(serviceFormData.workshopName)
                            println(serviceFormData.mileage)
                            println(serviceFormData.remarks)
                        }
                    )
//                }
            }
        }
    }
}

data class ServiceFormData(
    val serviceDate: String,
    val workshopName: String,
    val mileage: String,
    val serviceDescriptions: List<String>,
    val remarks: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceForm(
    onBackPressed: () -> Unit,
    onSubmit: (ServiceFormData) -> Unit
) {
    var serviceDate by remember { mutableStateOf("") }
    var workshopName by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var serviceDescriptions by remember { mutableStateOf(List(5) { "" }) }
    var remarks by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val todayDate = LocalDate.now().format(dateFormatter)

    if (serviceDate.isEmpty()) {
        serviceDate = "$todayDate (Today)"
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        serviceDate = if (localDate.format(dateFormatter) == todayDate) {
                            "${localDate.format(dateFormatter)} (Today)"
                        } else {
                            localDate.format(dateFormatter)
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = Color(230, 239, 252, 255),
        topBar = {
            TopAppBar(
                title = { Text("Add New Service") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.Black, navigationIconContentColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Replace with actual Yamaha logo
                    contentDescription = "Yamaha Logo",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Red
                )
                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "Yamaha Lagenda 115Z",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AGU 4907",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = "Service date",
                modifier = Modifier
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = serviceDate,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Select date")
                    }
                },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )

            Text(
                text = "Workshop name",
                modifier = Modifier
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )

            EnhancedCapsuleTextField(
                value = workshopName,
                onTextChange = { workshopName = it }
            )

            Text(
                text = "Mileage (km)",
                modifier = Modifier
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )

            EnhancedCapsuleTextField(
                value = mileage,
                onTextChange = { mileage = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Service Descriptions",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            serviceDescriptions.forEachIndexed { index, description ->
                EnhancedCapsuleTextField(
                    value = description,
                    onTextChange = { newValue ->
                        serviceDescriptions = serviceDescriptions.toMutableList().apply {
                            set(index, newValue)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Picture (optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlusIconBox()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Remarks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSubmit(
                        ServiceFormData(
                            serviceDate = serviceDate,
                            workshopName = workshopName,
                            mileage = mileage,
                            serviceDescriptions = serviceDescriptions,
                            remarks = remarks
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1AD5FF)
                )
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
fun PlusIconBox() {
    Box(
        modifier = Modifier
            .size(70.dp, 90.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .clickable {
                println("Plus icon clicked")
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Icon",
            tint = Color.LightGray,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TraxtiveTheme {
        ServiceForm(
            onBackPressed = { /* Handle back navigation */ },
            onSubmit = { serviceFormData ->
                // Handle form submission
            }
        )
    }
}

@Composable
fun EnhancedCapsuleTextField(value: String,
                             onTextChange: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val isError by remember { mutableStateOf(false) }  // Set this based on validation logic

    val textColor by animateColorAsState(
        targetValue = if (isError) Color.Red else Color.Black
    )

    BasicTextField(
        value = value,
        onValueChange =  onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                color = Color.White,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = CircleShape
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        singleLine = true,
        cursorBrush = SolidColor(Color.Gray),
        textStyle = TextStyle(
            color = textColor,
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            shadow = Shadow(color = Color.Gray, blurRadius = 1f)
        ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxSize()
            ) {
                innerTextField() // The actual text field
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EnhancedCapsuleTextFieldPreview() {
    TraxtiveTheme {
        EnhancedCapsuleTextField("", onTextChange = {})
    }
}