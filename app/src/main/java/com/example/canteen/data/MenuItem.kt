package com.example.canteen.data

data class MenuItem(
    val id: String = "",
    val canteenId: String = "",
    val canteenName: String = "",
    val name: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    val category: String = ""
)