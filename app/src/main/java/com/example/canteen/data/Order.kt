package com.example.canteen.data

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Int = 0,
    val status: String = "pending", // pending, diproses, selesai
    val canteenId: String = "",
    val canteenName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)