package com.example.canteen.ui.screen.buyer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.component.MenuCard
import com.example.canteen.ui.theme.GrayBg
import com.example.canteen.ui.theme.YellowPrimary
import kotlinx.coroutines.launch

@Composable
fun MenuScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        isLoading = true
        scope.launch {
            val userResult = repository.getCurrentUser()
            userResult.onSuccess { user ->
                val canteenResult = repository.getCanteenByOwnerId(user.id)
                canteenResult.onSuccess { canteen ->
                    if (canteen != null) {
                        val result = repository.getAllMenu()
                        result.onSuccess { menus ->
                            menuList = menus.filter { it.canteenId == canteen.id }
                        }
                    }
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
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
                text = "Menu Kantin Saya",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YellowPrimary)
                }
            } else if (menuList.isEmpty()) {

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
                            onEdit = {
                                navController.navigate("edit_menu/${item.id}")
                            },
                            onDelete = {
                                scope.launch {
                                    val result = repository.deleteMenu(item.id)
                                    result.onSuccess {
                                        Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                                        loadData()
                                    }
                                }
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
                .padding(16.dp),
            containerColor = YellowPrimary
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }
}