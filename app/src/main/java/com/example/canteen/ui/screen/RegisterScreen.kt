package com.example.canteen.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.canteen.R
import com.example.canteen.ui.component.InputField
import com.example.canteen.ui.theme.GrayBg
import com.example.canteen.ui.theme.YellowPrimary

// 🔥 DB
import com.example.canteen.data.DataHelper
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun RegisterScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val context = LocalContext.current
    val db = DataHelper(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text("Canteen", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(40.dp))

        InputField(email, { email = it }, "Email")
        InputField(password, { password = it }, "Password", true)
        InputField(confirm, { confirm = it }, "Confirm Password", true)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(context, "Isi semua field", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password != confirm) {
                    Toast.makeText(context, "Password tidak sama", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val success = db.register(email, password)

                if (success) {
                    Toast.makeText(context, "Register berhasil 🎉", Toast.LENGTH_SHORT).show()

                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }

                } else {
                    Toast.makeText(context, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Text("Already have an account? ")
            Text(
                "Login",
                color = YellowPrimary,
                modifier = Modifier.clickable {
                    navController.navigate("login")
                }
            )
        }
    }
}