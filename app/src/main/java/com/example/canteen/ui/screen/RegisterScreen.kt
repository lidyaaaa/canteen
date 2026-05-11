package com.example.canteen.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canteen.R
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🍴 LOGO
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 🍴 LOGO (gambar doang, lebih gede)
                    Image(
                        painter = painterResource(id = R.drawable.logo_canteen),
                        contentDescription = "Logo Canteen",
                        modifier = Modifier.size(150.dp)  // lebih gede
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 👤 NAME
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        unfocusedBorderColor = GrayInput,
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 📧 EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        unfocusedBorderColor = GrayInput,
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔒 PASSWORD
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        unfocusedBorderColor = GrayInput,
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔒 CONFIRM PASSWORD
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        unfocusedBorderColor = GrayInput,
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔥 SIGN UP BUTTON
                Button(
                    onClick = {
                        if (
                            name.isEmpty() ||
                            email.isEmpty() ||
                            password.isEmpty() ||
                            confirm.isEmpty()
                        ) {
                            Toast.makeText(
                                context,
                                "Isi semua field",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (password != confirm) {
                            Toast.makeText(
                                context,
                                "Password tidak sama",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (password.length < 6) {
                            Toast.makeText(
                                context,
                                "Password minimal 6 karakter",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        isLoading = true

                        scope.launch {
                            val result = repository.register(
                                email = email,
                                password = password,
                                name = name,
                                role = "buyer"
                            )

                            result.onSuccess {
                                Toast.makeText(
                                    context,
                                    "Register berhasil! Silakan login 🎉",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.navigate("login") {
                                    popUpTo("register") {
                                        inclusive = true
                                    }
                                }
                            }

                            result.onFailure { error ->
                                Toast.makeText(
                                    context,
                                    "Register gagal: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YellowPrimary,
                        contentColor = Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Black
                        )
                    } else {
                        Text(
                            text = "sign up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 📝 SIGN IN
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = GrayText,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Sign in",
                        color = YellowPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate("login")
                        }
                    )
                }
            }
        }
    }
}