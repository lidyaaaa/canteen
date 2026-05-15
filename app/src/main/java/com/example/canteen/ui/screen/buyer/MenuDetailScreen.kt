package com.example.canteen.ui.screen.buyer

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MenuDetailScreen(
    navController: NavController,
    menuId: String
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var menuItem by remember { mutableStateOf<MenuItem?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            val result = repository.getMenuById(menuId)
            result.onSuccess {
                menuItem = it
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = YellowPrimary)
        }
    } else {
        menuItem?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White)
            ) {
                Box {
                    if (item.imageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUrl),
                            contentDescription = item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(GrayInput),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image", color = GrayText)
                        }
                    }

                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .background(White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.canteenName,
                        fontSize = 14.sp,
                        color = GrayText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Rp ${item.price}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quantity:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(36.dp)
                                .background(GrayInput, RoundedCornerShape(8.dp))
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        Text("$quantity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(YellowPrimary, RoundedCornerShape(8.dp))
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            scope.launch {
                                val userId = repository.getCurrentUserId()
                                if (userId != null) {
                                    repository.addToCart(userId, item, quantity)
                                        .onSuccess {
                                            Toast.makeText(context, "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                        .onFailure {
                                            Toast.makeText(context, "Gagal menambahkan", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "Silakan login ulang", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowPrimary,
                            contentColor = Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Add to Cart - Rp ${item.price * quantity}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}