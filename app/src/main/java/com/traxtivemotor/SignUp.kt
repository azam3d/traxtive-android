package com.traxtivemotor
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase

class SignUp : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        setContent {
            SignUpScreen(onSignupSuccess = {
                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SignIn::class.java))
            }, onSignupFailure = { error ->
                Toast.makeText(this, "Signup failed: $error", Toast.LENGTH_LONG).show()
            })
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        TODO("Not yet implemented")
    }
}

@Composable
fun SignUpScreen(
    onSignupSuccess: () -> Unit,
    onSignupFailure: (String) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val auth = Firebase.auth

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    val colorStops = arrayOf(
        0.0f to Color(0, 200, 255, 255),
        0.5f to Color(104, 54, 255, 255),
        1f to Color(253, 106, 255, 255),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = colorStops.map { it.second },
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                ))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = {
                email = it
                emailError = validateEmail(it)
            },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = {
                emailError?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validatePassword(it)
                if (confirmPassword.isNotEmpty()) {
                    confirmPasswordError = validatePasswordMatch(it, confirmPassword)
                }
            },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = {
                passwordError?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = validatePasswordMatch(password, it)
            },
            label = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = {
                confirmPasswordError?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                Log.d("SignUpScreen", "Button clicked")

                emailError = validateEmail(email.trim())
                passwordError = validatePassword(password.trim())
                confirmPasswordError = validatePasswordMatch(password.trim(), confirmPassword.trim())

                if (!isFormValid(email, password, confirmPassword)) {
                    Log.d("SignUpScreen", "Form is not valid")
                    return@Button
                }
                isLoading = true

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        keyboardController?.hide()

                        if (task.isSuccessful) {
                            Log.d(TAG, "createUserWithEmail:success")
                            onSignupSuccess()
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            onSignupFailure(task.exception?.message ?: "Unknown error")
                        }
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255)),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Already signed up? Signup instead", color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { context.startActivity(Intent(context, SignIn::class.java)) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 213, 255, 255))
        ) {
            Text(text = "Sign in")
        }
    }
}

private fun validateEmail(email: String): String? {
    return when {
        email.isEmpty() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
        else -> null
    }
}

private fun validatePassword(password: String): String? {
    return when {
        password.isEmpty() -> "Password is required"
        password.length < 8 -> "Password must be at least 8 characters"
        !password.any { it.isDigit() } -> "Password must contain at least one number"
        !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
        else -> null
    }
}

private fun validatePasswordMatch(password: String, confirmPassword: String): String? {
    return when {
        confirmPassword.isEmpty() -> "Please confirm your password"
        password != confirmPassword -> "Passwords do not match"
        else -> null
    }
}

private fun isFormValid(email: String, password: String, confirmPassword: String): Boolean {
    return validateEmail(email) == null &&
            validatePassword(password) == null &&
            validatePasswordMatch(password, confirmPassword) == null
}