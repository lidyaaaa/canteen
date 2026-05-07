package com.example.canteen.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

import com.example.canteen.data.DataHelper
import com.example.canteen.ui.component.MenuCard
import com.example.canteen.data.MenuItem // 🔥 WAJIB
import com.example.canteen.ui.theme.GrayBg

@Composable
fun MenuScreen(navController: NavController) {

    val context = LocalContext.current
    val db = DataHelper(context)

    val menuList = remember { mutableStateListOf<MenuItem>() }

    // 🔥 LOAD DATA
    LaunchedEffect(Unit) {
        menuList.clear()
        menuList.addAll(db.getAllMenu())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Menu Kantin",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (menuList.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada menu 😢", color = Color.Gray)
                }

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(menuList) { item ->

                        MenuCard(
                            item = item,

                            // ✏️ EDIT
                            onEdit = {
                                navController.navigate("edit_menu/${item.id}")
                            },

                            // 🗑️ DELETE
                            onDelete = {
                                db.deleteMenu(item.id)

                                // 🔄 REFRESH
                                menuList.clear()
                                menuList.addAll(db.getAllMenu())
                            }
                        )
                    }
                }
            }
        }

        // ➕ ADD MENU
        FloatingActionButton(
            onClick = {
                navController.navigate("add_menu")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("+")
        }
    }
}