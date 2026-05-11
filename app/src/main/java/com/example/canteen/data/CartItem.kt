package com.example.canteen.data

data class CartItem(
    val id: String = "",          // Firestore document ID di subkoleksi items
    val menuId: String = "",
    val name: String = "",
    val price: Int = 0,
    val quantity: Int = 1,
    val imageUrl: String = "",
    val canteenId: String = "",
    val canteenName: String = ""
)