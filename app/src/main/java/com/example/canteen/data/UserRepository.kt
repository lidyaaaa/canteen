package com.example.canteen.data

data class User(
    val email: String,
    val password: String
)

object UserRepository {
    private val users = mutableListOf<User>()

    fun register(email: String, password: String): Boolean {
        val exist = users.find { it.email == email }
        return if (exist == null) {
            users.add(User(email, password))
            true
        } else {
            false
        }
    }

    fun login(email: String, password: String): Boolean {
        return users.any { it.email == email && it.password == password }
    }
}