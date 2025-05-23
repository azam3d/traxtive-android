package com.traxtivemotor

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.MutableLiveData
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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

//        parseJson(baseContext,"traxtive-motor.json")

        val userId = Firebase.auth.currentUser!!.uid
//        val userId = "w5uBnQl7GOdCDSYABBtqPOhjJRr1"
//        val userId = "wNbbFw1vR2hghglOxAT7w3zET4x1"
//        val userId = "000hoj3BEpgrvUIDwg7xhAr1vUu1"

        enableEdgeToEdge()
        setContent {
            TraxtiveTheme {
                val name = remember { mutableStateOf("email") }
                val motorcyclesLiveData = remember { MutableLiveData<List<Motorcycle>>() }
                var showBottomSheet by remember { mutableStateOf(false) }

                fetchFirebaseData(userId, motorcyclesLiveData)

                PagerAnimateToItem(userId, motorcycles = motorcyclesLiveData,
                    onShowBottomSheetChange = { newValue ->
                        showBottomSheet = newValue
                    }
                )

                BottomSheet(
                    showBottomSheet,
                    onShowBottomSheetChange = { newValue ->
                        showBottomSheet = newValue
                    }
                )

                ProfileMenu()
            }
        }
    }
}

fun fetchFirebaseData(userId: String, motorcycleLiveData: MutableLiveData<List<Motorcycle>>) {
    val database = Firebase.database
    val motorcycleRef = database.getReference("motorcycles")

    val userRef = motorcycleRef.child(userId)
    Log.d("Firebase", "userId: $userRef")

    userRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val motorcycles2 = mutableListOf<Motorcycle>()

            for (motorcycleSnapshot in snapshot.children) {
                val motorId = motorcycleSnapshot.key
                val motorcycles = motorcycleSnapshot.getValue(Motorcycle::class.java)
                val motor = Motorcycle(
                    motorId = motorId,
                    brand = motorcycles?.brand,
                    imageUrl = motorcycles?.imageUrl,
                    model = motorcycles?.model,
                    plateNumber = motorcycles?.plateNumber,
                    userId = motorcycles?.userId
                )
                motorcycles2.add(motor)

//                motorcycles?.let {
//                    Log.d("Firebase", "Motorcycle: $it")
////                    name.value = it.brand ?: "Unknown"
//                    motorcycles2.add(it)
//                }
            }
            motorcycleLiveData.value = motorcycles2
            Log.d("Firebase", "Motorcycles: $motorcycles2")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Firebase", "Failed to read value.", error.toException())
        }
    })

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
fun PagerAnimateToItem(userId: String, motorcycles: MutableLiveData<List<Motorcycle>>, onShowBottomSheetChange: (Boolean) -> Unit) {
    val mContext = LocalContext.current
    val motorcyclesList by motorcycles.observeAsState()
    val motor = motorcyclesList ?: return
    Log.d("Firebase", "Motor1: $motor")
    Log.d("Firebase", "Motor2: $motorcyclesList")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp)
            .background(Color.Transparent)
    ) {
        val pagerState = rememberPagerState(pageCount = { motor.size })
        val colorStops = arrayOf(
            0.0f to Color(0, 200, 255, 255),
            0.5f to Color(104, 54, 255, 255),
            1f to Color(253, 106, 255, 255),
        )

        Column {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = colorStops.map { it.second },
                            start = Offset(0f, Float.POSITIVE_INFINITY),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    )
                    .weight(0.5f)
            ) {
                Column {
                    Text(
                        text = "My Motorcycles",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 80.dp)
                    )

                    if (motor.isEmpty()) {
                        Text(
                            text = "No motorcycles available",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 160.dp),
                            color = Color.White
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 3,
                        contentPadding = PaddingValues(horizontal = 64.dp),
                        modifier = Modifier
                            .fillMaxSize()
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .zIndex(page * 2f)
                                .graphicsLayer {
                                    val startOffset = pagerState.startOffsetForPage(page)
                                    translationX = size.width * (startOffset * .85f)

                                    val blur = (startOffset * 20).coerceAtLeast(.1f)
                                    renderEffect = RenderEffect
                                        .createBlurEffect(
                                            blur, blur, Shader.TileMode.DECAL
                                        )
                                        .asComposeRenderEffect()

                                    val scale = 1f - (startOffset * .1f)
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(RoundedCornerShape(16.dp)) // must be the last modifier
                                .fillMaxWidth()
//                        .border(
//                            width = 2.dp,
//                            color = Color.Red,
//                        )
                                .clickable {
                                    Log.d("Pager", "Clicked on page $page")

                                    val intent = Intent(mContext, MotorDetails::class.java)
                                    intent.putExtra("userId", userId)
                                    intent.putExtra("motorId", motor[page].motorId)
                                    intent.putExtra("motor", motor[page])
                                    mContext.startActivity(intent)
                                },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                if (motor[page].imageUrl != null) {
                                    BikeImage(imageUrl = motor[page].imageUrl!!, modifier = Modifier.size(200.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Motorcycle,
                                        contentDescription = "Motorbike",
                                        modifier = Modifier.size(140.dp),
                                        tint = Color.Blue
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .weight(0.3f)
                    .padding(16.dp)
                    .height(86.dp),
            ) {
                val verticalState = rememberPagerState(pageCount = { motor.size })
                VerticalPager(
                    state = verticalState,
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = false, horizontalAlignment = Alignment.Start
                ) { page ->
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(mContext, MotorDetails::class.java)
                            intent.putExtra("userId", userId)
                            intent.putExtra("motorId", motor[page].motorId)
                            intent.putExtra("motor", motor[page])
                            mContext.startActivity(intent)
                        },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        motor[page].let { brand ->
                            Text(
                                text = brand.brand!!,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = brand.model!!,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 24.sp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = brand.plateNumber!!,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 24.sp)
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

        Button(onClick = {
            onShowBottomSheetChange(true)
//            mContext.startActivity(Intent(mContext, ServiceDetails::class.java))
        }, modifier = Modifier
            .align(Alignment.BottomCenter)
            .size(
                width = 240.dp, height = 48.dp
            ),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255))) {
            Text("+ Add Motorcycle")
        }
    }
}

@Composable
fun BikeImage(imageUrl: String, modifier: Modifier) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Bike Image",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction

fun PagerState.startOffsetForPage(page: Int) = offsetForPage(page).coerceAtLeast(0f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(showBottomSheet: Boolean, onShowBottomSheetChange: (Boolean) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val brands = listOf(brandAprilia, brandBenelli, brandCfmotor, brandBmw, brandDucati, brandHarley, brandHonda, brandKawasaki, brandKtm, brandModenas, brandSuzuki, brandSym, brandTriumph, brandYamaha)
    var hasSubmitted by remember { mutableStateOf(false) }

    var selectedBrand by remember { mutableStateOf(Brand()) }
    var model by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }

    var brandError by remember { mutableStateOf<String?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }
    var plateNumberError by remember { mutableStateOf<String?>(null) }

    if (showBottomSheet) {
        ModalBottomSheet(
            containerColor = Color(230, 239, 252, 255),
            onDismissRequest = {
                onShowBottomSheetChange(false)
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add Motorcycle",
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )

                EnhancedAutoCompleteTextField(
                    suggestions = brands,
                    selectedBrand = {
                        selectedBrand = it
                    },
                    isError = hasSubmitted && selectedBrand.brand.isNullOrEmpty()
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = {
                        model = it
                        modelError = validateModel(it)
                    },
                    label = { Text("Model") },
                    isError = modelError != null,
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, errorContainerColor = MaterialTheme.colorScheme.background),
                    trailingIcon = {
                        if (model.isNotEmpty()) {
                            IconButton(onClick = { model = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear text"
                                )
                            }
                        }
                    }
                )

                OutlinedTextField(
                    value = plateNumber,
                    onValueChange = { input ->
                        val filtered = input.uppercase()
                        plateNumber = filtered
                        plateNumberError = validatePlateNumber(filtered)
                    },
                    label = { Text("Plate Number") },
                    isError = plateNumberError != null,
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, errorContainerColor = MaterialTheme.colorScheme.background),
                    trailingIcon = {
                        if (plateNumber.isNotEmpty()) {
                            IconButton(onClick = { plateNumber = "" }) {
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

                Spacer(modifier = Modifier.height(8.dp))

                brandError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 12.dp)) }
                modelError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 12.dp)) }
                plateNumberError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 12.dp)) }

                Button(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(48.dp)
                        .align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255)),
                    onClick = {
                        brandError = validateBrand(selectedBrand.brand)
                        modelError = validateModel(model.trim())
                        plateNumberError = validatePlateNumber(plateNumber.trim())

                        hasSubmitted = true

                        if (!isFormValid(selectedBrand.brand, model, plateNumber)) {
                            return@Button
                        }

//                        scope.launch { sheetState.hide() }.invokeOnCompletion {
//                            if (!sheetState.isVisible) {
//                                onShowBottomSheetChange(false)
//                            }
//                        }
                        val database = Firebase.database
                        val userId = Firebase.auth.currentUser?.uid

                        println("Submit button clicked")
                        println(selectedBrand.brand)
                        println(model)
                        println(plateNumber)
                        println(userId)

                        val motorcycle = Motorcycle2(selectedBrand.brand, model, plateNumber)
                        if (userId != null) {
                            database.reference.child("motorcycles").child(userId).push().setValue(motorcycle)
                        }
                        onShowBottomSheetChange(false)
                }) {
                    Text("Submit")
                }
            }
        }
    }
}

private fun validateBrand(brand: String?): String? {
    return when {
        brand.isNullOrEmpty() -> "Motorcycle brand is required"
        else -> null
    }
}

private fun validateModel(model: String): String? {
    return when {
        model.isEmpty() -> "Motorcycle model is required"
        else -> null
    }
}

private fun validatePlateNumber(plateNumber: String): String? {
    return when {
        plateNumber.isEmpty() -> "Plate number is required"
        else -> null
    }
}

private fun isFormValid(brand: String?, model: String, plateNumber: String): Boolean {
    return validateBrand(brand) == null &&
            validateModel(model) == null &&
            validatePlateNumber(plateNumber) == null
}

@Composable
fun ProfileMenu() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val user = FirebaseAuth.getInstance()
    val email = user.currentUser?.email

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, end = 16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Person, contentDescription = "More options", modifier = Modifier.size(32.dp), tint = Color.White)

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(email!!) },
                    onClick = {}
                )

                DropdownMenuItem(
                    text = { Text("Logout") },
                    leadingIcon = { LogoutImage() },
                    onClick = {
                        expanded = false
                        val firebaseAuth = FirebaseAuth.getInstance()
                        firebaseAuth.signOut()
                        context.startActivity(Intent(context, SignIn::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun LogoutImage() {
    Image(
        painter = painterResource(id = R.drawable.logout),
        contentDescription = "Logout",
        modifier = Modifier.size(24.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun MotorImage() {
    Image(
        painter = painterResource(id = R.drawable.yamaha),
        contentDescription = "Yamaha",
        modifier = Modifier.size(24.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun EnhancedAutoCompleteTextField(suggestions: List<Brand>, selectedBrand: (Brand) -> Unit, isError: Boolean) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var filteredSuggestions by remember { mutableStateOf(suggestions) }
    var showSuggestions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (newValue.text != textFieldValue.text) {
                    textFieldValue = newValue
                    // Show suggestions only when typing and input is not empty
                    showSuggestions = newValue.text.isNotEmpty()
                    // Update filtered suggestions based on current input
                    filteredSuggestions = if (newValue.text.isEmpty()) {
                        suggestions
                    } else {
                        suggestions.filter { it.brand!!.contains(newValue.text, ignoreCase = true) }
                    }

                    val matchedBrand = filteredSuggestions.firstOrNull {
                        it.brand?.contains(newValue.text, ignoreCase = true) == true
                    }

                    // Call selectedBrand with the matched brand or null
                    selectedBrand(matchedBrand ?: Brand(brand = newValue.text))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isError) Color(255, 251, 255) else Color.White,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.LightGray,
                    shape = CircleShape
                )
                .padding(16.dp)
                .focusRequester(focusRequester),
            textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            singleLine = true,
            cursorBrush = SolidColor(Color.Gray),
            decorationBox = { innerTextField ->
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        text = "Brand",
                        style = TextStyle(color = if (isError) MaterialTheme.colorScheme.error else Color.LightGray,
                                          fontSize = 18.sp)
                    )
                }
                innerTextField()
            }
        )

        // Automatically request focus when the UI is composed
//        LaunchedEffect(Unit) {
//            focusRequester.requestFocus()
//        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(25.dp)
                )
                .shadow(8.dp, RoundedCornerShape(25.dp))
                .background(Color.White, shape = RoundedCornerShape(25.dp))
                .animateContentSize()
        ) {
            if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    items(filteredSuggestions) { suggestion ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Update text with selected suggestion and move cursor to the end
                                    textFieldValue = TextFieldValue(
                                        text = suggestion.brand!!,
                                        selection = TextRange(suggestion.brand.length)
                                    )
                                    // Hide suggestions after selection
                                    showSuggestions = false
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row {
                                Image(
                                    painter = painterResource(id = R.drawable.yamaha),
                                    contentDescription = "Yamaha",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color.LightGray,
                                            shape = CircleShape
                                        ),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = suggestion.brand!!,
                                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterVertically),
                                )
                            }
                        }
                    }
                }
            } else if (showSuggestions && filteredSuggestions.isEmpty()) {
                Text(
                    text = "No suggestions available",
                    style = TextStyle(color = Color.Gray, fontSize = 16.sp),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}