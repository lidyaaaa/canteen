package com.example.canteen.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.component.BottomNavBar
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {

    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("User") }
    var searchQuery by remember { mutableStateOf("") }
    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var cartCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            // Get user name
            val userResult = repository.getCurrentUser()
            userResult.onSuccess { user ->
                userName = user.name

                // Get cart count
                repository.getCartCount(user.id).onSuccess {
                    cartCount = it
                }
            }

            // Get menu list
            val menuResult = repository.getAllMenu()
            menuResult.onSuccess { menus ->
                menuList = menus
            }

            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentRoute = "home",
                cartCount = cartCount
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrayBg)
                .padding(paddingValues)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(YellowPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.toString()?.uppercase() ?: "?",
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

                Row {
                    IconButton(onClick = { navController.navigate("cart") }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart"
                        )
                    }
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedBorderColor = GrayInput,
                    unfocusedBorderColor = GrayInput
                )
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = YellowPrimary)
                }
            } else if (menuList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada menu 😢",
                        color = GrayText,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(menuList.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }) { item ->
                        MenuGridCard(
                            item = item,
                            onClick = {
                                navController.navigate("menu_detail/${item.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuGridCard(
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (item.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl),
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GrayInput),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = GrayText, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Rp ${item.price}",
                color = YellowPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowPrimary,
                    contentColor = Black
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}