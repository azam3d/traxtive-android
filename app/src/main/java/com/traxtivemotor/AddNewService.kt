@file:JvmName("AddNewServiceKt")

package com.traxtivemotor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.traxtivemotor.ui.theme.TraxtiveTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddNewService : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraxtiveTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ServiceForm(
                        onBackPressed = { /* Handle back navigation */ },
                        onSubmit = { serviceFormData ->
                            // Handle form submission
                        }
                    )
//                }
            }
        }
    }
}

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

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        serviceDate = localDate.format(dateFormatter)
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
        topBar = {
            TopAppBar(
                title = { Text("Add New Activity") },
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

            OutlinedTextField(
                value = serviceDate,
                onValueChange = { },
                label = { Text("Service date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Select date")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = workshopName,
                onValueChange = { workshopName = it },
                label = { Text("Workshop Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = mileage,
                onValueChange = { mileage = it },
                label = { Text("Mileage (km)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Service Descriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            serviceDescriptions.forEachIndexed { index, description ->
                OutlinedTextField(
                    value = description,
                    onValueChange = { newValue ->
                        serviceDescriptions = serviceDescriptions.toMutableList().apply {
                            set(index, newValue)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Picture (optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // Image picker implementation would go here
            }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
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
                    containerColor = Color(0xFF00E5FF)
                )
            ) {
                Text("Submit")
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