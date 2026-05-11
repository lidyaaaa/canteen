package com.example.canteen.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // =============================
    // 🔐 AUTHENTICATION
    // =============================

    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: String = "buyer"
    ): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            val userData = hashMapOf(
                "email" to email,
                "name" to name,
                "role" to role,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = User(
                id = userId,
                email = userDoc.getString("email") ?: "",
                name = userDoc.getString("name") ?: "",
                role = userDoc.getString("role") ?: "buyer"
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (userDoc.exists()) {
                val user = User(
                    id = userId,
                    email = userDoc.getString("email") ?: "",
                    name = userDoc.getString("name") ?: "",
                    role = userDoc.getString("role") ?: "buyer"
                )
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
        return getUserById(userId)
    }

    // =============================
    // 🍔 MENU OPERATIONS
    // =============================

    suspend fun getAllMenu(): Result<List<MenuItem>> {
        return try {
            val snapshot = firestore.collection("menu")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val menuList = snapshot.documents.map { doc ->
                MenuItem(
                    id = doc.id,
                    canteenId = doc.getString("canteenId") ?: "",
                    canteenName = doc.getString("canteenName") ?: "",
                    name = doc.getString("name") ?: "",
                    price = doc.getLong("price")?.toInt() ?: 0,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    category = doc.getString("category") ?: ""
                )
            }
            Result.success(menuList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMenuById(menuId: String): Result<MenuItem> {
        return try {
            val doc = firestore.collection("menu")
                .document(menuId)
                .get()
                .await()

            if (doc.exists()) {
                val menu = MenuItem(
                    id = doc.id,
                    canteenId = doc.getString("canteenId") ?: "",
                    canteenName = doc.getString("canteenName") ?: "",
                    name = doc.getString("name") ?: "",
                    price = doc.getLong("price")?.toInt() ?: 0,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    category = doc.getString("category") ?: ""
                )
                Result.success(menu)
            } else {
                Result.failure(Exception("Menu not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMenu(
        canteenId: String,
        canteenName: String,
        name: String,
        price: Int,
        imageUrl: String,
        category: String
    ): Result<String> {
        return try {
            val menuData = hashMapOf(
                "canteenId" to canteenId,
                "canteenName" to canteenName,
                "name" to name,
                "price" to price,
                "imageUrl" to imageUrl,
                "category" to category,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val docRef = firestore.collection("menu").add(menuData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMenu(
        menuId: String,
        name: String,
        price: Int,
        imageUrl: String,
        category: String
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "name" to name,
                "price" to price,
                "imageUrl" to imageUrl,
                "category" to category
            )

            firestore.collection("menu")
                .document(menuId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMenu(menuId: String): Result<Unit> {
        return try {
            firestore.collection("menu")
                .document(menuId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =============================
    // 🏪 CANTEEN OPERATIONS
    // =============================

    suspend fun getCanteenByOwnerId(ownerId: String): Result<Canteen?> {
        return try {
            val snapshot = firestore.collection("canteens")
                .whereEqualTo("ownerId", ownerId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents[0]
                val canteen = Canteen(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    ownerId = doc.getString("ownerId") ?: "",
                    ownerName = doc.getString("ownerName") ?: ""
                )
                Result.success(canteen)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllCanteens(): Result<List<Canteen>> {
        return try {
            val snapshot = firestore.collection("canteens")
                .get()
                .await()

            val canteens = snapshot.documents.map { doc ->
                Canteen(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    ownerId = doc.getString("ownerId") ?: "",
                    ownerName = doc.getString("ownerName") ?: ""
                )
            }
            Result.success(canteens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCanteen(
        name: String,
        ownerId: String,
        ownerName: String
    ): Result<String> {
        return try {
            val canteenData = hashMapOf(
                "name" to name,
                "ownerId" to ownerId,
                "ownerName" to ownerName,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val docRef = firestore.collection("canteens").add(canteenData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =============================
    // 🛒 CART OPERATIONS
    // =============================

    suspend fun addToCart(userId: String, menuItem: MenuItem, quantity: Int = 1): Result<Unit> {
        return try {
            val cartRef = firestore.collection("carts")
                .document(userId)
                .collection("items")

            val existing = cartRef
                .whereEqualTo("menuId", menuItem.id)
                .get()
                .await()

            if (existing.documents.isNotEmpty()) {
                val doc = existing.documents[0]
                val currentQty = doc.getLong("quantity")?.toInt() ?: 0
                cartRef.document(doc.id)
                    .update("quantity", currentQty + quantity)
                    .await()
            } else {
                val cartItem = hashMapOf(
                    "menuId" to menuItem.id,
                    "name" to menuItem.name,
                    "price" to menuItem.price,
                    "quantity" to quantity,
                    "imageUrl" to menuItem.imageUrl,
                    "canteenId" to menuItem.canteenId,
                    "canteenName" to menuItem.canteenName
                )
                cartRef.add(cartItem).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCartItems(userId: String): Result<List<CartItem>> {
        return try {
            val snapshot = firestore.collection("carts")
                .document(userId)
                .collection("items")
                .get()
                .await()

            val items = snapshot.documents.map { doc ->
                CartItem(
                    id = doc.id,
                    menuId = doc.getString("menuId") ?: "",
                    name = doc.getString("name") ?: "",
                    price = doc.getLong("price")?.toInt() ?: 0,
                    quantity = doc.getLong("quantity")?.toInt() ?: 1,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    canteenId = doc.getString("canteenId") ?: "",
                    canteenName = doc.getString("canteenName") ?: ""
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCartQuantity(userId: String, itemId: String, quantity: Int): Result<Unit> {
        return try {
            firestore.collection("carts")
                .document(userId)
                .collection("items")
                .document(itemId)
                .update("quantity", quantity)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(userId: String, itemId: String): Result<Unit> {
        return try {
            firestore.collection("carts")
                .document(userId)
                .collection("items")
                .document(itemId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart(userId: String): Result<Unit> {
        return try {
            val items = firestore.collection("carts")
                .document(userId)
                .collection("items")
                .get()
                .await()
            val batch = firestore.batch()
            items.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCartCount(userId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection("carts")
                .document(userId)
                .collection("items")
                .get()
                .await()
            val count = snapshot.documents.sumOf { doc ->
                doc.getLong("quantity")?.toInt() ?: 0
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =============================
    // 📝 ORDER OPERATIONS
    // =============================

    suspend fun createOrder(
        userId: String,
        userName: String,
        items: List<CartItem>,
        totalPrice: Int
    ): Result<String> {
        return try {
            val orderItems = items.map { item ->
                hashMapOf(
                    "menuId" to item.menuId,
                    "name" to item.name,
                    "price" to item.price,
                    "quantity" to item.quantity,
                    "imageUrl" to item.imageUrl
                )
            }

            val orderData = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "items" to orderItems,
                "totalPrice" to totalPrice,
                "status" to "pending",
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val docRef = firestore.collection("orders").add(orderData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersByUser(userId: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.map { doc ->
                val itemsList = (doc.get("items") as? List<Map<String, Any>>)?.map {
                    CartItem(
                        menuId = it["menuId"] as? String ?: "",
                        name = it["name"] as? String ?: "",
                        price = (it["price"] as? Long)?.toInt() ?: 0,
                        quantity = (it["quantity"] as? Long)?.toInt() ?: 0,
                        imageUrl = it["imageUrl"] as? String ?: "",
                        canteenId = it["canteenId"] as? String ?: "",
                        canteenName = it["canteenName"] as? String ?: ""
                    )
                } ?: emptyList()

                Order(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    userName = doc.getString("userName") ?: "",
                    items = itemsList,
                    totalPrice = doc.getLong("totalPrice")?.toInt() ?: 0,
                    status = doc.getString("status") ?: "pending",
                    canteenId = doc.getString("canteenId") ?: "",
                    canteenName = doc.getString("canteenName") ?: "",
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                )
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersForSeller(canteenId: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("canteenId", canteenId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.map { doc ->
                val itemsList = (doc.get("items") as? List<Map<String, Any>>)?.map {
                    CartItem(
                        menuId = it["menuId"] as? String ?: "",
                        name = it["name"] as? String ?: "",
                        price = (it["price"] as? Long)?.toInt() ?: 0,
                        quantity = (it["quantity"] as? Long)?.toInt() ?: 0,
                        imageUrl = it["imageUrl"] as? String ?: "",
                        canteenId = it["canteenId"] as? String ?: "",
                        canteenName = it["canteenName"] as? String ?: ""
                    )
                } ?: emptyList()

                Order(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    userName = doc.getString("userName") ?: "",
                    items = itemsList,
                    totalPrice = doc.getLong("totalPrice")?.toInt() ?: 0,
                    status = doc.getString("status") ?: "pending",
                    canteenId = doc.getString("canteenId") ?: "",
                    canteenName = doc.getString("canteenName") ?: "",
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                )
            }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("orders")
                .document(orderId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =============================
    // 📝 SELLER REQUESTS
    // =============================

    suspend fun submitSellerRequest(
        userId: String,
        userName: String,
        canteenName: String,
        description: String
    ): Result<String> {
        return try {
            val requestData = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "canteenName" to canteenName,
                "description" to description,
                "status" to "pending",
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val docRef = firestore.collection("seller_requests")
                .add(requestData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingSellerRequests(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("seller_requests")
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val requests = snapshot.documents.map { doc ->
                val data = doc.data ?: mapOf()
                data + ("id" to doc.id)
            }
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSellerRequestStatus(requestId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("seller_requests")
                .document(requestId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(userId: String, role: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("role", role)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}