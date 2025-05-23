@file:JvmName("AddNewServiceKt")

package com.traxtivemotor

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

class AddNewService : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val motor = intent.getParcelableExtra("motor") as Motorcycle?
        Log.d("motor", motor.toString())

        enableEdgeToEdge()

        setContent {
            TraxtiveTheme(dynamicColor = false) {
                var showBottomSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState()
                var imageUri by remember { mutableStateOf<Uri?>(null) }

                ServiceForm(
                    motor,
                    imageUri,
                    onBackPressed = { finish() },
                    onShowBottomSheetChange = { newValue ->
                        showBottomSheet = newValue
                    },
                    onSubmit = { service ->
                        println(service.date)
                        println(service.items)
                        println(service.mileage)
                        println(service.total)
                        println(service.remark)
                        println(service.workshop)

                        val database = Firebase.database
                        val userId = Firebase.auth.currentUser?.uid

                        Log.d("userId", userId.toString())

//                        val service2 = Service2(serviceFormData.serviceDate, serviceFormData.workshopName, serviceFormData.mileage, serviceFormData.serviceDescriptions, serviceFormData.remarks)
                        val serviceId = database.reference.child("services").child(userId!!).child(motor?.motorId!!).push()
                        Log.d("serviceId", serviceId.toString())
                        Log.d("serviceId", serviceId.key.toString())

                        serviceId.setValue(service)

                        finish()

                        // use serviceId to setValue of date, workshop, mileage, remarks, item[], price[], totalPrice

//                        if (userId != null) {
//                            database.reference.child("services").child(userId).child(motor?.motorId!!).child(serviceId).setValue(service2)
//                        }
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
    imageUri: Uri?,
    onBackPressed: () -> Unit,
    onShowBottomSheetChange: (Boolean) -> Unit,
    onSubmit: (Service2) -> Unit
) {
    val mContext = LocalContext.current
    var serviceDate by remember { mutableStateOf("") }
    var workshopName by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    val serviceDescriptions = remember { mutableStateListOf<Item>() }
    var remarks by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val todayDate = LocalDate.now().format(dateFormatter)

    if (serviceDate.isEmpty()) {
        serviceDate = "$todayDate (Today)"
    }
    serviceDescriptions.add(Item(null, null))

    val totalPrice by remember(serviceDescriptions) {
        derivedStateOf {
            serviceDescriptions.sumOf { it.price?.toIntOrNull() ?: 0 }.toString()
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

                                if (index == serviceDescriptions.lastIndex && newText.isNotEmpty()) {
                                    serviceDescriptions.add(Item())
                                }
                                if (index == serviceDescriptions.lastIndex - 1 && newText.isEmpty()) {
                                    serviceDescriptions.removeLastOrNull()
                                }
                            },
                            onPriceChange = { newPrice ->
                                serviceDescriptions[index] = serviceDescriptions[index].copy(price = newPrice)
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

//            Spacer(modifier = Modifier.height(24.dp))
//
//            Text(
//                text = "Picture (optional)",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            PlusIconBox(imageUri = imageUri, onClick = {
//                if (imageUri == null) {
//                    onShowBottomSheetChange(true)
//                } else {
//                    val intent = Intent(mContext, PhotoViewer::class.java)
//                    intent.putExtra("imageUri", imageUri)
//                    mContext.startActivity(intent)
//                }
//            })

//            Button(enabled = if (imageUri != null) true else false,
//                modifier = Modifier
//                    .size(70.dp, 36.dp)
//                    .padding(top = 4.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
//                shape = RoundedCornerShape(6.dp),
//                contentPadding = PaddingValues(0.dp),
//                onClick = {
////                    imageUri = null
//                }
//            ) {
//                Icon(
//                    modifier = Modifier.size(16.dp),
//                    imageVector = Icons.Default.Delete,
//                    contentDescription = "Delete",
//                    tint = Color.LightGray
//                )
//                Text(text = "Delete", color = Color.LightGray)
//            }

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
                    val serviceDescriptions2 = serviceDescriptions.toMutableList()

                    serviceDescriptions2.removeAll {
                        it.name.isNullOrBlank() || it.price.isNullOrBlank()
                    }
                    println("serviceDescriptions2 $serviceDescriptions2")
//                    onSubmit(
//                        Service2(
//                            date = serviceDate,
//                            items = null,
//                            mileage = mileage,
//                            total = totalPrice,
//                            remark = remarks,
//                            workshop = workshopName,
//                        )
//                    )
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
fun ServiceItemForm(index: Int, item: Item, onValueChange: (String) -> Unit, onPriceChange: (String) -> Unit) {
    var item by remember { mutableStateOf(item) }
    val dividerColor = Color(0xFFE3EDFB)

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
            "Service Item", item.name, "${index + 1}. ", KeyboardOptions(keyboardType = KeyboardType.Text)) { newText ->
            item = item.copy(name = newText)
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
            "0", item.price, "RM", KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Default)) { newText ->
            item = item.copy(price = newText)
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
                if (value == null) {
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
                }
                innerTextField() // The actual text field
            }
        },
        keyboardOptions = keyboardOptions,
        visualTransformation = PrefixTransformation(prefix)
    )
}

class PrefixTransformation(val prefix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return PrefixFilter(text, prefix)
    }
}

fun PrefixFilter(number: AnnotatedString, prefix: String): TransformedText {
    var out = prefix + number.text
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

//@Preview
//@Composable
//fun PreviewServiceItemForm() {
//    ServiceItemForm(index = 0, item = Item("Example Service", "10.50"),
//        onValueChange = { })
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TraxtiveTheme {
        ServiceForm(
            Motorcycle(), null,
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
    val isError by remember { mutableStateOf(false) }  // Set this based on validation logic

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
                innerTextField() // The actual text field
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
            onDismiss()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted

        if (isGranted) {
            // Retry camera launch after permission granted
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
                    e.printStackTrace()
                }
            }
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasGalleryPermission = isGranted

        if (isGranted) {
            galleryLauncher.launch("image/*")
        }
    }

    Column(modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Button(
            onClick = {
                        println("open camera")

                        if (hasCameraPermission) {
                            println("hasCameraPermission")
//                            imageUri = createImageFile(context)
//                            cameraLauncher.launch(imageUri!!)

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
                                    e.printStackTrace()
                                }
                            }
                        } else {
                            println("doesn't hasCameraPermission")
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
                        println("open photo library")

                        if (hasGalleryPermission) {
                            println("hasGalleryPermission")
                            galleryLauncher.launch("image/*")
                        } else {
                            println("doesn't hasGalleryPermission")
                            galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                      },
            modifier = Modifier
                .fillMaxWidth(),
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

//private fun createImageFile(context: Context): Uri {
//    val storageDir: File? = context.getExternalFilesDir(null)
//    val imageFile = File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
//    return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
//}
