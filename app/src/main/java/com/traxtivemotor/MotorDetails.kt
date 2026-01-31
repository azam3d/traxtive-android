package com.traxtivemotor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
//import com.google.firebase.ktx.Firebase
import com.traxtivemotor.ui.theme.TraxtiveTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MotorDetails : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra("userId")!!
        val motorId = intent.getStringExtra("motorId")!!
        val motor = intent.getParcelableExtra("motor") as Motorcycle?
        println(userId)
        println(motorId)
        println(motor)

        val servicesLiveData = MutableLiveData<List<Service3>>()
        val serviceId = MutableLiveData<List<String>>()
        val serviceUpdateLiveData = MutableLiveData<ServiceUpdate2>()

        setContent {
            TraxtiveTheme(dynamicColor = false) {
                TopBarNavigation(navigateBack = { finish() })
                AllServices(motor, servicesLiveData, serviceId, serviceUpdateLiveData, userId, motorId)
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

fun fetchServices(userId: String, motorId: String, servicesLiveData: MutableLiveData<List<Service3>>, serviceIdsLiveData: MutableLiveData<List<String>>) {
    println("fetchServices")

    val database = Firebase.database
    val servicesRef = database.getReference("services")
    val userRef = servicesRef.child(userId)

    userRef.child(motorId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var totalCount = 0
            var bigTotal = 0
            val services = mutableListOf<Service3>()
            val serviceIds = mutableListOf<String>()

            println("\n---------------")
            for (serviceSnapshot in snapshot.children) {
                totalCount++

                if (totalCount > bigTotal) {
                    bigTotal = totalCount
                }

                println("\nService ID: ${serviceSnapshot.key}")
                serviceIds.add(serviceSnapshot.key!!)

                val service = serviceSnapshot.getValue(Service3::class.java)
                val service2 = serviceSnapshot.toService3()
                println("- service: $service")
                println("- workshop: ${service?.workshop}")
                println("- service2: $service2")

//                println(serviceSnapshot)

//                services.add(service ?: Service3())
                services.add(service2 ?: Service3())
            }
            totalCount = 0

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val sortedServices = services.sortedByDescending {
                LocalDate.parse(it.date, formatter)
            }
            servicesLiveData.value = sortedServices
//            serviceIdsLiveData.value = serviceIds

            println("---------------\n\n")
            println("bigTotal: $bigTotal")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Firebase", "Failed to read value.", error.toException())
        }
    })
}

fun DataSnapshot.toService3(): Service3? {
    return getValue(Service3::class.java)?.copy(
        id = key
    )
}

fun fetchServiceUpdate(userId: String, motorId: String, serviceUpdateLiveData: MutableLiveData<ServiceUpdate2>) {
    println("fetchServiceUpdate")

    val database = Firebase.database
    val servicesRef = database.getReference("serviceUpdate")
    val userRef = servicesRef.child(userId)

    println("userId: $userId")

    userRef.child(motorId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val serviceUpdate = snapshot.getValue(ServiceUpdate2::class.java)
            println("- serviceUpdate2: $serviceUpdate")

            serviceUpdateLiveData.value = serviceUpdate
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Firebase", "Failed to read value.", error.toException())
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllServices(motor: Motorcycle?,
                servicesLiveData: MutableLiveData<List<Service3>>,
                serviceIds: MutableLiveData<List<String>>,
                serviceUpdateLiveData: MutableLiveData<ServiceUpdate2>,
                userId: String,
                motorId: String) {
    val mContext = LocalContext.current
    val services by servicesLiveData.observeAsState(initial = emptyList())

    LaunchedEffect(userId, motorId) {
        fetchServices(userId, motorId, servicesLiveData, serviceIds)
        fetchServiceUpdate(userId, motorId, serviceUpdateLiveData)
    }
    val serviceUpdate by serviceUpdateLiveData.observeAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchServices(Firebase.auth.currentUser?.uid!!, motor?.motorId!!, servicesLiveData, serviceIds)

            val updatedService = result.data?.getParcelableExtra<Service2>("updatedService")
            if (updatedService != null) {
                println("Updated service: $updatedService")
            }
        }
    }
    var showDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

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
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.LightGray)
                .background(Color.White)
                .clickable {
                    showBottomSheet = true
                }
        ) {
            Row(modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.padding(8.dp)) {
                    Text(
                        text = "NEXT SERVICE",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (serviceUpdate == null) {
                        Text(
                            text = "No next service",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp)
                        )
                    } else {
                        Text(
                            text = "${serviceUpdate?.nextMileage ?: ""}km or before ${serviceUpdate?.date ?: ""}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowCircleRight,
                    contentDescription = "Right Arrow"
                )
            }
        }

//        SmoothBottomSheetExample()

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
                        val items: List<Item>? = it[index].items?.map { (_, item) ->
                            Item(
                                name = item.name,
                                price = item.price
                            )
                        }
                        val service = Service2(it[index].id, it[index].date, items, it[index].mileage, it[index].total, it[index].remark, it[index].workshop, it[index].receipt.toString())
                        ServiceCard(motorId, motor, service, serviceIds.value?.get(index) ?: "", launcher)
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
                launcher.launch(intent)
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
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Red
                )
            ) {
                Text("Remove Motorcycle")
            }

            ConfirmationDialog(
                showDialog = showDialog,
                onDismiss = { showDialog = false },
                onYesClick = {
                    val database = Firebase.database
                    database.reference.child("motorcycles").child(userId).child(motorId).removeValue()

                    (mContext as? Activity)?.finish()
                },
                onNoClick = {
                    println("User clicked No")
                },
                title = "Remove Motorcycle",
                text = "Are you sure you want to remove this motorcycle?",
                confirmText = "Remove Motorcycle"
            )
        }
    }

    if (showBottomSheet) {
        var serviceDue by remember { mutableStateOf(serviceUpdate?.date ?: "") }
        var nextMileage by remember { mutableStateOf(serviceUpdate?.nextMileage ?: "") }
        var showDatePicker by remember { mutableStateOf(false) }
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val todayDate = LocalDate.now().format(dateFormatter)
        var plateNumberError by remember { mutableStateOf<String?>(null) }

        ModalBottomSheet(
            containerColor = Color(230, 239, 252, 255),
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState
        ) {
            // Bottom Sheet Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Next Service",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = serviceDue,
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

                OutlinedTextField(
                    value = nextMileage,
                    onValueChange = { input ->
                        val filtered = input.uppercase()
                        nextMileage = filtered
//                        plateNumberError = validatePlateNumber(filtered)
                    },
                    label = { Text("Next Mileage (km)") },
                    isError = plateNumberError != null,
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, errorContainerColor = MaterialTheme.colorScheme.background),
                    trailingIcon = {
                        if (nextMileage.isNotEmpty()) {
                            IconButton(onClick = { nextMileage = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear text"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Characters
                    )
                )

                Button(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(48.dp)
                        .align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255)),
                    onClick = {
                        val database = Firebase.database
                        val userId = Firebase.auth.currentUser?.uid

                        val serviceUpdate = ServiceUpdate2(serviceDue, nextMileage)
                        if (userId != null) {
                            database.reference.child("serviceUpdate").child(userId).child(motorId).setValue(serviceUpdate)
                                .addOnSuccessListener {
                                    serviceUpdateLiveData.value = serviceUpdate
                                }
                                .addOnFailureListener { exception ->
                                    println("Failed to update service: ${exception.message}")
                                }
                        }

                        showBottomSheet = false
                    }) {
                    Text("Submit")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                                    serviceDue = if (localDate.format(dateFormatter) == todayDate) {
                                        "${localDate.format(dateFormatter)}"
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
fun ServiceCard(motorId: String, motor: Motorcycle?, service: Service2, serviceId: String, launcher: ActivityResultLauncher<Intent>) {
    val mContext = LocalContext.current

    Row(modifier = Modifier
        .padding(vertical = 8.dp)
        .fillMaxWidth()
        .clickable {
            println("serviceId motordetails: ${service.id}")

            val intent = Intent(mContext, ServiceDetails::class.java)
            intent.putExtra("serviceId", service.id)
            intent.putExtra("service", service)
            intent.putExtra("motorId", motorId)
            intent.putExtra("motor", motor)
            launcher.launch(intent)
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

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onYesClick: () -> Unit,
    onNoClick: () -> Unit,
    title: String = "Confirmation",
    text: String = "Are you sure you want to continue?",
    confirmText: String = "Yes",
    dismissText: String = "Cancel"
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = {
                    onYesClick()
                    onDismiss()
                }) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onNoClick()
                    onDismiss()
                }) {
                    Text(dismissText)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmoothBottomSheetExample() {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()

    // Smooth backdrop alpha animation
    val backdropAlpha by animateFloatAsState(
        targetValue = if (bottomSheetState.isVisible) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "backdrop_alpha"
    )

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = bottomSheetState
        ),
        sheetContent = {
            BottomSheetContent(
                onItemClick = { item ->
                    scope.launch {
                        bottomSheetState.hide()
                    }
                }
            )
        },
        sheetPeekHeight = 0.dp,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetTonalElevation = 8.dp,
        sheetShadowElevation = 16.dp,
        sheetDragHandle = {
            // Custom drag handle
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content
            Text("Main Content")
//            MainContent(
//                onOpenBottomSheet = {
//                    scope.launch {
//                        bottomSheetState.expand()
//                    }
//                }
//            )

            // Custom backdrop overlay
            if (bottomSheetState.isVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(backdropAlpha)
                        .background(Color.Black)
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    onOpenBottomSheet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smooth Bottom Sheet Demo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onOpenBottomSheet) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Bottom Sheet"
                    )
                }
            }
        }

        // Sample content
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(20) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Sample Item ${index + 1}",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onOpenBottomSheet,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Bottom Sheet"
                )
            }
        }
    }
}

@Composable
private fun BottomSheetContent(
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Text(
            text = "Bottom Sheet Options",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Divider(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
            thickness = 1.dp
        )

        // Menu items
        val menuItems = listOf(
            "Profile Settings",
            "Notifications",
            "Privacy",
            "Help & Support",
            "About",
            "Sign Out"
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
//            items(menuItems) { item ->
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
//                    ),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                ) {
//                    TextButton(
//                        onClick = { onItemClick(item) },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp)
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.Start
//                        ) {
//                            Text(
//                                text = item,
//                                fontSize = 16.sp,
//                                color = MaterialTheme.colorScheme.onSurface
//                            )
//                        }
//                    }
//                }
//            }
        }

        // Bottom padding for better UX
        Spacer(modifier = Modifier.height(16.dp))
    }
}

