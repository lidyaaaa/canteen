    package com.example.canteen.data

    data class User(
        val id: String = "",
        val email: String = "",
        val name: String = "",
        val role: String = "buyer" // admin | seller | buyer
    )