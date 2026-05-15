package com.example.canteen.ui.screen.buyer

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.canteen.data.CartItem
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.component.BottomNavBar
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CartScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var cartCount by remember { mutableStateOf(0) }
    var isCheckingOut by remember { mutableStateOf(false) }

    // Load cart
    LaunchedEffect(Unit) {
        scope.launch {
            val userResult = repository.getCurrentUser()
            userResult.onSuccess { user ->
                repository.getCartItems(user.id).onSuccess {
                    cartItems = it
                    cartCount = it.sumOf { item -> item.quantity }
                }
            }
            isLoading = false
        }
    }

    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentRoute = "cart",
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
            Text(
                text = "Keranjang",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (isLoading || isCheckingOut) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YellowPrimary)
                }
            } else if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Keranjang kosong 😔", color = GrayText, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        CartItemCard(
                            item = item,
                            onIncrease = {
                                scope.launch {
                                    val userId = repository.getCurrentUserId() ?: return@launch
                                    val newQty = item.quantity + 1
                                    repository.updateCartQuantity(userId, item.id, newQty)
                                    cartItems = cartItems.map {
                                        if (it.id == item.id) it.copy(quantity = newQty) else it
                                    }
                                    cartCount = cartItems.sumOf { it.quantity }
                                }
                            },
                            onDecrease = {
                                scope.launch {
                                    val userId = repository.getCurrentUserId() ?: return@launch
                                    if (item.quantity > 1) {
                                        val newQty = item.quantity - 1
                                        repository.updateCartQuantity(userId, item.id, newQty)
                                        cartItems = cartItems.map {
                                            if (it.id == item.id) it.copy(quantity = newQty) else it
                                        }
                                    } else {
                                        repository.removeFromCart(userId, item.id)
                                        cartItems = cartItems.filter { it.id != item.id }
                                    }
                                    cartCount = cartItems.sumOf { it.quantity }
                                }
                            },
                            onRemove = {
                                scope.launch {
                                    val userId = repository.getCurrentUserId() ?: return@launch
                                    repository.removeFromCart(userId, item.id)
                                    cartItems = cartItems.filter { it.id != item.id }
                                    cartCount = cartItems.sumOf { it.quantity }
                                    Toast.makeText(context, "Dihapus", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                // Total + Checkout
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                "Rp $totalPrice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = YellowPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isCheckingOut = true
                                    val userResult = repository.getCurrentUser()
                                    userResult.onSuccess { user ->
                                        repository.createOrder(user.id, user.name, cartItems, totalPrice)
                                            .onSuccess {
                                                Toast.makeText(context, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                                cartItems = emptyList()
                                                cartCount = 0
                                                navController.navigate("orders") {
                                                    popUpTo("cart") { inclusive = true }
                                                }
                                            }
                                            .onFailure {
                                                Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    isCheckingOut = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YellowPrimary,
                                contentColor = Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Checkout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(item.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .background(GrayInput, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(GrayInput, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No img", fontSize = 10.sp, color = GrayText)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Rp ${item.price}", color = YellowPrimary, fontSize = 14.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = RedError)
            }
        }
    }
}