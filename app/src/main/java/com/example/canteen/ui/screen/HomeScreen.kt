package com.example.canteen.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.example.canteen.ui.theme.GrayBg

// 🔥 model data (kosong dulu, nanti dari database)
data class MenuItem(
    val name: String,
    val place: String,
    val price: String
)

@Composable
fun HomeScreen() {

    // 🔥 kosong (nanti diisi dari database)
    val menuList = remember { mutableStateListOf<MenuItem>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .padding(16.dp)
    ) {

        // 🔥 HEADER
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NA")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text("User", fontWeight = FontWeight.Medium)
            }

            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 CEK DATA
        if (menuList.isEmpty()) {

            // ❗ KOSONG STATE
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada menu 😢",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

        } else {

            // 🔥 LIST MENU (kalau nanti ada data)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuList) { item ->
                    MenuCard(item)
                }
            }
        }
    }
}

@Composable
fun MenuCard(item: MenuItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(item.name, fontWeight = FontWeight.Bold)
            Text(item.place, fontSize = 12.sp, color = Color.Gray)
            Text(item.price, fontWeight = FontWeight.SemiBold)
        }
    }
}