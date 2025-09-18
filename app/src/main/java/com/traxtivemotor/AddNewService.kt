@file:JvmName("AddNewServiceKt")

package com.traxtivemotor

import android.app.Activity
import android.content.Context
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.traxtivemotor.ui.theme.TraxtiveTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class AddNewService : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val motorFromIntent = intent.getParcelableExtra<Motorcycle>("motor")
        val serviceIdFromIntent = intent.getStringExtra("serviceId") // ID of service to edit, or null if new
        val serviceDataFromIntent = intent.getParcelableExtra<Service2>("service")

        Log.d("AddNewService", "Received motorId: ${motorFromIntent?.motorId}, serviceId: $serviceIdFromIntent")
        Log.d("AddNewService", "Received serviceDataFromIntent: $serviceDataFromIntent")

        enableEdgeToEdge()

        setContent {
            TraxtiveTheme(dynamicColor = false) {
                var showBottomSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState()
                var imageUri by remember { mutableStateOf<Uri?>(null) }

                ServiceForm(
                    motor = motorFromIntent,
                    initialServiceData = serviceDataFromIntent,
                    imageUri = imageUri,
                    onBackPressed = { finish() },
                    onShowBottomSheetChange = { newValue ->
                        showBottomSheet = newValue
                    },
                    onSubmit = { submittedServiceData -> // submittedServiceData is of type Service3
                        val database = Firebase.database
                        val currentAuthUserId = Firebase.auth.currentUser?.uid
                        val currentMotorActualId = motorFromIntent?.motorId

                        if (currentAuthUserId == null) {
                            Log.e("AddNewService", "User not authenticated.")
                            setResult(Activity.RESULT_CANCELED, Intent().putExtra("error", "User not authenticated"))
                            finish()
                            return@ServiceForm
                        }
                        if (currentMotorActualId == null) {
                            Log.e("AddNewService", "Motor ID is missing.")
                            setResult(Activity.RESULT_CANCELED, Intent().putExtra("error", "Motor ID missing"))
                            finish()
                            return@ServiceForm
                        }

                        val serviceDetailsMap = mapOf(
                            "date" to submittedServiceData.date,
                            "mileage" to submittedServiceData.mileage,
                            "total" to submittedServiceData.total,
                            "remark" to submittedServiceData.remark,
                            "workshop" to submittedServiceData.workshop
                        )
                        val serviceItemsListForDb = submittedServiceData.items?.values?.toList() ?: emptyList<Item2>()

                        val resultIntent = Intent()
                        // Map List<Item2> to List<Item> for Service2
                        val finalServiceItemsForDisplay = serviceItemsListForDb.map { item2 ->
                            Item(
                                name = item2.name,
                                price = item2.price
                            )
                        }
                        val finalServiceForDisplay = Service2(
                            date = submittedServiceData.date,
                            workshop = submittedServiceData.workshop,
                            mileage = submittedServiceData.mileage,
                            total = submittedServiceData.total,
                            remark = submittedServiceData.remark,
                            items = finalServiceItemsForDisplay
                        )
                        resultIntent.putExtra("updatedService", finalServiceForDisplay)

                        if (serviceIdFromIntent != null && serviceIdFromIntent.isNotBlank()) {
                            // EDIT MODE
                            Log.d("AddNewService", "Updating service: $serviceIdFromIntent for motor: $currentMotorActualId")
                            val serviceNodeRef = database.getReference("services")
                                .child(currentAuthUserId)
                                .child(currentMotorActualId)
                                .child(serviceIdFromIntent)

                            serviceNodeRef.updateChildren(serviceDetailsMap).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    val itemsNodeRef = serviceNodeRef.child("items")
                                    itemsNodeRef.removeValue().addOnCompleteListener { removeItemTask ->
                                        if (removeItemTask.isSuccessful) {
                                            serviceItemsListForDb.forEach { itemData -> // Use serviceItemsListForDb (List<Item2>) for Firebase
                                                itemsNodeRef.push().setValue(itemData)
                                            }
                                            Log.d("AddNewService", "Service '$serviceIdFromIntent' and items updated.")
                                            resultIntent.putExtra("updatedServiceId", serviceIdFromIntent)
                                            setResult(Activity.RESULT_OK, resultIntent)
                                        } else {
                                            Log.e("AddNewService", "Failed to clear items for '$serviceIdFromIntent'.", removeItemTask.exception)
                                            resultIntent.putExtra("updatedServiceId", serviceIdFromIntent)
                                            resultIntent.putExtra("warning", "Failed to update service items fully")
                                            setResult(Activity.RESULT_OK, resultIntent) 
                                        }
                                        finish()
                                    }
                                } else {
                                    Log.e("AddNewService", "Failed to update service details for '$serviceIdFromIntent'.", updateTask.exception)
                                    setResult(Activity.RESULT_CANCELED, Intent().putExtra("error", "Failed to update service details"))
                                    finish()
                                }
                            }
                        } else {
                            // ADD NEW MODE
                            Log.d("AddNewService", "Adding new service for motor: $currentMotorActualId")
                            val motorServicesRef = database.getReference("services")
                                .child(currentAuthUserId)
                                .child(currentMotorActualId)

                            val newServiceNodeRef = motorServicesRef.push()
                            val newServiceKey = newServiceNodeRef.key

                            if (newServiceKey == null) {
                                Log.e("AddNewService", "Failed to generate new service key from Firebase.")
                                setResult(Activity.RESULT_CANCELED, Intent().putExtra("error", "Failed to generate service key"))
                                finish()
                                return@ServiceForm
                            }

                            newServiceNodeRef.setValue(serviceDetailsMap).addOnCompleteListener { addTask ->
                                if (addTask.isSuccessful) {
                                    serviceItemsListForDb.forEach { itemData -> // Use serviceItemsListForDb (List<Item2>) for Firebase
                                        newServiceNodeRef.child("items").push().setValue(itemData)
                                    }
                                    Log.d("AddNewService", "New service added with key: $newServiceKey")
                                    resultIntent.putExtra("updatedServiceId", newServiceKey)
                                    setResult(Activity.RESULT_OK, resultIntent)
                                } else {
                                    Log.e("AddNewService", "Failed to add new service.", addTask.exception)
                                    setResult(Activity.RESULT_CANCELED, Intent().putExtra("error", "Failed to add new service"))
                                }
                                finish()
                            }
                        }
                    }
                )
                if (showBottomSheet) {
                    ModalBottomSheet(
                        containerColor = Color(230, 239, 252, 255),
                        sheetState = sheetState,
                        onDismissRequest = { showBottomSheet = false }
                    ) {
                        CameraBottomSheet(baseContext,
                            onDismiss = { showBottomSheet = false },
                            onAction = {
                                imageUri = it.toUri()
                                println("camera bottom sheet dismissed")
                                println(imageUri.toString())
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceForm(
    motor: Motorcycle?,
    initialServiceData: Service2?, // Added new parameter for existing service data
    imageUri: Uri?,
    onBackPressed: () -> Unit,
    onShowBottomSheetChange: (Boolean) -> Unit,
    onSubmit: (Service3) -> Unit
) {
    val mContext = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val todayDate = LocalDate.now().format(dateFormatter)

    var serviceDate by remember { mutableStateOf(initialServiceData?.date?.takeIf { it.isNotBlank() } ?: todayDate) }
    var workshopName by remember { mutableStateOf(initialServiceData?.workshop ?: "") }
    var mileage by remember { mutableStateOf(initialServiceData?.mileage ?: "") }
    var remarks by remember { mutableStateOf(initialServiceData?.remark ?: "") }

    val initialItems = initialServiceData?.items
        ?.filterNotNull()
        ?.mapNotNull { item ->
            // item is of type Item here from Service2
            val name = item.name
            val price = item.price?.toString() // Convert Double? price from Item to String? for Item2
            if (name != null) Item2(name, price) else null
        }
        ?.toMutableList()

    val serviceDescriptions = remember {
        mutableStateListOf<Item2>().apply {
            if (initialItems != null && initialItems.isNotEmpty()) {
                addAll(initialItems)
                // Add an empty item at the end for editing/adding new items easily
                add(Item2(null, null))
            } else {
                add(Item2(null, null))
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val totalPrice by remember(serviceDescriptions) {
        derivedStateOf {
            serviceDescriptions.sumOf { it.price?.toDoubleOrNull() ?: 0.0 }.toString()
        }
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
                            "Today, ${localDate.format(dateFormatter)}"
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
                title = { Text(if (initialServiceData != null) "Edit Service" else "Add New Service") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.Black, navigationIconContentColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            MotorHeader(motor)

            Text(
                text = "Service date",
                modifier = Modifier
                    .padding(top = 8.dp)
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
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, keyboardType = KeyboardType.Text, imeAction = ImeAction.Default),
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Default
                ),
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

            Box(modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.LightGray
                )
                .background(Color.White),
                contentAlignment = if (serviceDescriptions.isNotEmpty()) Alignment.TopStart else Alignment.Center
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    serviceDescriptions.forEachIndexed { index, item ->
                        ServiceItemForm(index, item,
                            onValueChange = { newText ->
                                serviceDescriptions[index] = serviceDescriptions[index].copy(name = newText)
                                // Auto-add a new row if typing in the last item's name field
                                if (index == serviceDescriptions.lastIndex && newText.isNotEmpty()) {
                                    serviceDescriptions.add(Item2())
                                }
                                // Auto-remove the last row if the second to last item's name is cleared (and price is also empty)
                                if (index == serviceDescriptions.lastIndex - 1 && newText.isEmpty() && serviceDescriptions.getOrNull(index)?.price.isNullOrEmpty() && serviceDescriptions.size > 1) {
                                    serviceDescriptions.removeLastOrNull()
                                }
                            },
                            onPriceChange = { newPrice ->
                                serviceDescriptions[index] = serviceDescriptions[index].copy(price = newPrice)
                                // Auto-remove the last row if the second to last item's price is cleared (and name is also empty)
                                if (index == serviceDescriptions.lastIndex - 1 && newPrice.isEmpty() && serviceDescriptions.getOrNull(index)?.name.isNullOrEmpty() && serviceDescriptions.size > 1) {
                                    serviceDescriptions.removeLastOrNull()
                                }
                            }
                        )
                    }

                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "Total",
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(end = 12.dp),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "RM$totalPrice",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(67.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
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
                    val finalItemsForService3 = serviceDescriptions
                        .filter { !(it.name.isNullOrBlank() && it.price.isNullOrBlank()) }
                        .mapIndexedNotNull { index, item ->
                             // Ensure item.name is not null; provide a default or handle error if necessary
                            item.name?.let { itemName ->
                                "item_$index" to item
                            }
                        }.toMap()

                    onSubmit(
                        Service3(
                            date = serviceDate.replace("Today, ", ""),
                            items = finalItemsForService3, 
                            mileage = mileage,
                            total = totalPrice,
                            remark = remarks,
                            workshop = workshopName,
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AD5FF))
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
fun PlusIconBox(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(70.dp, 90.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Icon",
                tint = Color.LightGray,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun ServiceItemForm(index: Int, item: Item2, onValueChange: (String) -> Unit, onPriceChange: (String) -> Unit) {
    var currentItem by remember { mutableStateOf(item) }
    val dividerColor = Color(0xFFE3EDFB)

    LaunchedEffect(item) {
        currentItem = item
    }

    Row(modifier = Modifier
        .padding(vertical = 8.dp)
        .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ItemTextField(Modifier
            .padding(start = 4.dp)
            .height(44.dp)
            .weight(1f)
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
            "Service Item", currentItem.name, "${index + 1}. ", KeyboardOptions(keyboardType = KeyboardType.Text)) { newText ->
            currentItem = currentItem.copy(name = newText)
            onValueChange(newText)
        }

        ItemTextField(Modifier
                        .height(44.dp)
                        .width(67.dp)
                        .drawBehind {
                            drawLine(
                                color = dividerColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        },
            "0", currentItem.price, "RM", KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Default)) { newText ->
            currentItem = currentItem.copy(price = newText)
            onPriceChange(newText)
        }
    }
}

@Composable
fun ItemTextField(modifier: Modifier, placeholder: String, value: String?, prefix: String, keyboardOptions: KeyboardOptions, onValueChange: (String) -> Unit) {
    BasicTextField(
        modifier = modifier,
        value = value?: "",
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = Color.Black,
            fontSize = 16.sp,
            textAlign = TextAlign.Start
        ),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                if ((value ?: "").isEmpty()) {
                    Row {
                        Text(
                            text = prefix,
                            color = Color.Black,
                            fontSize = 16.sp
                        )

                        Text(
                            text = placeholder,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                     Row { // Show prefix even when there is value
                        Text(
                            text = prefix,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
                innerTextField()
            }
        },
        keyboardOptions = keyboardOptions,
        visualTransformation = if ((value ?: "").isNotEmpty()) PrefixTransformation(prefix) else VisualTransformation.None
    )
}

class PrefixTransformation(val prefix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return PrefixFilter(text, prefix)
    }
}

fun PrefixFilter(number: AnnotatedString, prefix: String): TransformedText {
    val out = prefix + number.text
    val prefixOffset = prefix.length

    val numberOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return offset + prefixOffset
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset < prefixOffset) return 0
            return offset - prefixOffset
        }
    }
    return TransformedText(AnnotatedString(out), numberOffsetTranslator)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TraxtiveTheme {
        ServiceForm(
            motor = Motorcycle(),
            initialServiceData = null,
            imageUri = null,
            onBackPressed = { /* Handle back navigation */ },
            onShowBottomSheetChange = { /* Handle bottom sheet visibility change */ },
            onSubmit = { service ->
                // Handle form submission
            }
        )
    }
}

@Composable
fun EnhancedCapsuleTextField(value: String,
                             keyboardOptions: KeyboardOptions,
                             onTextChange: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val isError by remember { mutableStateOf(false) }

    val textColor by animateColorAsState(
        targetValue = if (isError) Color.Red else Color.Black
    )

    BasicTextField(
        value = value,
        onValueChange =  onTextChange,
        keyboardOptions = keyboardOptions,
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
                innerTextField() 
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EnhancedCapsuleTextFieldPreview() {
    TraxtiveTheme {
        EnhancedCapsuleTextField("", keyboardOptions = KeyboardOptions.Default, onTextChange = {})
    }
}

@Composable
fun CameraBottomSheet(
    context: Context,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    var photoFile by remember { mutableStateOf<File?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var hasCameraPermission by remember { mutableStateOf(true) }
    var hasGalleryPermission by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        hasGalleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun createImageFile(): File? {
        return try {
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                onAction(it.toString())
            }
        } else {
            photoFile?.delete() 
        }
        onDismiss() 
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            onAction(it.toString())
        }
        onDismiss() 
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            photoFile = createImageFile()
            photoFile?.let { file ->
                try {
                    imageUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider", 
                        file
                    )
                    imageUri?.let { uri ->
                        cameraLauncher.launch(uri)
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("CameraBottomSheet", "FileProvider URI error: ${e.message}")
                    onDismiss()
                }
            } ?: onDismiss() 
        } else {
            onDismiss() 
        }
    }

    val galleryPermissionRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasGalleryPermission = isGranted
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            onDismiss()
        }
    }


    Column(modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Button(
            onClick = {
                if (hasCameraPermission) {
                    photoFile = createImageFile()
                    photoFile?.let { file ->
                        try {
                            imageUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            imageUri?.let { uri ->
                                cameraLauncher.launch(uri)
                            }
                        } catch (e: IllegalArgumentException) {
                             Log.e("CameraBottomSheet", "FileProvider URI error on direct launch: ${e.message}")
                             onDismiss()
                        }
                    } ?: onDismiss()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255))
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Camera",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "Camera")
        }

        Button(
            onClick = {
                val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                if (hasGalleryPermission) {
                    galleryLauncher.launch("image/*")
                } else {
                    galleryPermissionRequestLauncher.launch(permissionToRequest)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255))
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Photo Library",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "Photo Library")
        }
    }
}
