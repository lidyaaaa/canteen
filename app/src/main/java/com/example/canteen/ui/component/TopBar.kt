package com.example.canteen.ui.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

import com.example.canteen.data.DataHelper

@Composable
fun TopBar(navController: NavController) {

    val context = LocalContext.current

    // 🔥 FIX: pakai remember biar ga recreate terus
    val db = remember { DataHelper(context) }

    // 🔥 ambil session
    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val email = sharedPref.getString("email", "") ?: ""

    // 🔥 FIX: aman dari crash
    val userName = if (email.isBlank()) {
        "Guest"
    } else {
        try {
            db.getUserName(email)
        } catch (e: Exception) {
            e.printStackTrace()
            "Guest"
        }
    }

    // 🔥 FIX: ga bakal crash walau kosong
    val initial = userName.firstOrNull()?.toString() ?: "?"

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 👤 USER INFO
        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(initial)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(userName, fontWeight = FontWeight.Medium)
        }

        // ⋮ MENU
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        expanded = false

                        // 🔥 clear session
                        sharedPref.edit().clear().apply()

                        // 🔥 balik ke login
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}