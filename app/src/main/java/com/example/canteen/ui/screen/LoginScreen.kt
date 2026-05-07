package com.example.canteen.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.ui.component.InputField
import com.example.canteen.ui.theme.GrayBg
import com.example.canteen.ui.theme.YellowPrimary
import com.example.canteen.data.DataHelper

@Composable
fun LoginScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    // 🔥 FIX: pakai remember biar gak recreate terus
    val db = remember { DataHelper(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Canteen",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        InputField(email, { email = it }, "Email")
        InputField(password, { password = it }, "Password", true)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Isi semua field", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val success = db.login(email, password)

                if (success) {

                    // 🔥 SESSION
                    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                    sharedPref.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("email", email)
                        .apply()

                    Toast.makeText(context, "Login berhasil 🎉", Toast.LENGTH_SHORT).show()

                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }

                } else {
                    Toast.makeText(context, "Email / Password salah", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Text("Don't have an account? ")
            Text(
                text = "Sign Up",
                color = YellowPrimary,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}