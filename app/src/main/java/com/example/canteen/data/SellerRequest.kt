package com.example.canteen.data

data class SellerRequest(
    val id: Int = 0,
    val userId: Int,
    val canteenName: String,
    val description: String,
    val status: String
)