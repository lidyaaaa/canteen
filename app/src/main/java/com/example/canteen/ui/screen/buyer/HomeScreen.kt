package com.example.canteen.ui.screen.buyer

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
    var userInitial by remember { mutableStateOf("U") }
    var searchQuery by remember { mutableStateOf("") }
    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var cartCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            val userResult = repository.getCurrentUser()
            userResult.onSuccess { user ->
                userName = user.name
                userInitial = user.name.firstOrNull()?.uppercase() ?: "U"

                repository.getCartCount(user.id).onSuccess {
                    cartCount = it
                }
            }

            repository.getAllMenu().onSuccess { menus ->
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
            // ── Top Bar ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar + nama user
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(YellowPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial,
                            color = Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Halo, $userName 👋",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Black
                        )
                        Text(
                            text = "Mau makan apa hari ini?",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                    }
                }

                // Ikon cart + notif
                Row {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) Badge { Text(cartCount.toString()) }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate("cart") }) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Black
                            )
                        }
                    }
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Black
                        )
                    }
                }
            }

            // ── Search Bar ────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari makanan...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = GrayText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedBorderColor = YellowPrimary,
                    unfocusedBorderColor = GrayInput
                )
            )

            // ── Content ───────────────────────────────────────────
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = YellowPrimary)
                    }
                }

                menuList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.NoFood,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = GrayText
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum ada menu 😢",
                                color = GrayText,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                else -> {
                    val filtered = menuList.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tidak ditemukan \"$searchQuery\"",
                                color = GrayText,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtered) { item ->
                                MenuGridCard(
                                    item = item,
                                    onClick = { navController.navigate("menu_detail/${item.id}") }
                                )
                            }
                        }
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
                    Icon(
                        Icons.Default.BrokenImage,
                        null,
                        tint = GrayText,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                color = Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                item.canteenName,
                fontSize = 11.sp,
                color = GrayText,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Rp ${String.format("%,d", item.price).replace(',', '.')}",
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
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pesan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Preview ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Home Screen (Buyer)")
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}

@Preview(showBackground = true, name = "Menu Card - With Image")
@Composable
fun MenuGridCardPreview() {
    Box(modifier = Modifier.padding(12.dp).width(180.dp)) {
        MenuGridCard(
            item = MenuItem(
                id = "1",
                name = "Nasi Goreng Spesial",
                price = 15000,
                imageUrl = "",
                category = "Makanan",
                canteenId = "c1",
                canteenName = "Kantin A"
            ),
            onClick = {}
        )
    }
}