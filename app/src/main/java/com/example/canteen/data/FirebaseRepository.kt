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

    suspend fun getMenuByCanteen(canteenId: String): Result<List<MenuItem>> {
        return try {
            val snapshot = firestore.collection("menu")
                .whereEqualTo("canteenId", canteenId)
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
    // 📋 MENU REQUESTS (Seller → Admin approval)
    // =============================

    /**
     * Seller submit request tambah menu baru (status: pending, butuh approve admin)
     */
    suspend fun submitMenuRequest(
        sellerId: String,
        sellerName: String,
        canteenId: String,
        canteenName: String,
        name: String,
        price: Int,
        imageUrl: String,
        category: String
    ): Result<String> {
        return try {
            val requestData = hashMapOf(
                "sellerId" to sellerId,
                "sellerName" to sellerName,
                "canteenId" to canteenId,
                "canteenName" to canteenName,
                "name" to name,
                "price" to price,
                "imageUrl" to imageUrl,
                "category" to category,
                "status" to "pending",   // pending | approved | rejected
                "type" to "add",         // add | edit | delete
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val docRef = firestore.collection("menu_requests")
                .add(requestData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Seller submit request edit menu (status: pending, butuh approve admin)
     */
    suspend fun submitMenuEditRequest(
        sellerId: String,
        sellerName: String,
        canteenId: String,
        canteenName: String,
        menuId: String,
        name: String,
        price: Int,
        imageUrl: String,
        category: String
    ): Result<String> {
        return try {
            val requestData = hashMapOf(
                "sellerId" to sellerId,
                "sellerName" to sellerName,
                "canteenId" to canteenId,
                "canteenName" to canteenName,
                "menuId" to menuId,
                "name" to name,
                "price" to price,
                "imageUrl" to imageUrl,
                "category" to category,
                "status" to "pending",
                "type" to "edit",
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val docRef = firestore.collection("menu_requests")
                .add(requestData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Seller submit request hapus menu (status: pending, butuh approve admin)
     */
    suspend fun submitMenuDeleteRequest(
        sellerId: String,
        sellerName: String,
        canteenId: String,
        canteenName: String,
        menuId: String,
        menuName: String
    ): Result<String> {
        return try {
            val requestData = hashMapOf(
                "sellerId" to sellerId,
                "sellerName" to sellerName,
                "canteenId" to canteenId,
                "canteenName" to canteenName,
                "menuId" to menuId,
                "name" to menuName,
                "status" to "pending",
                "type" to "delete",
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            val docRef = firestore.collection("menu_requests")
                .add(requestData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin: ambil semua menu request yang masih pending
     */
    suspend fun getPendingMenuRequests(): Result<List<MenuRequest>> {
        return try {
            val snapshot = firestore.collection("menu_requests")
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val requests = snapshot.documents.map { doc ->
                MenuRequest(
                    id = doc.id,
                    sellerId = doc.getString("sellerId") ?: "",
                    sellerName = doc.getString("sellerName") ?: "",
                    canteenId = doc.getString("canteenId") ?: "",
                    canteenName = doc.getString("canteenName") ?: "",
                    menuId = doc.getString("menuId") ?: "",
                    name = doc.getString("name") ?: "",
                    price = doc.getLong("price")?.toInt() ?: 0,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    category = doc.getString("category") ?: "",
                    status = doc.getString("status") ?: "pending",
                    type = doc.getString("type") ?: "add",
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                )
            }
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Seller: ambil semua menu request milik seller tertentu (buat lihat status)
     */
    suspend fun getMenuRequestsBySeller(sellerId: String): Result<List<MenuRequest>> {
        return try {
            val snapshot = firestore.collection("menu_requests")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val requests = snapshot.documents.map { doc ->
                MenuRequest(
                    id = doc.id,
                    sellerId = doc.getString("sellerId") ?: "",
                    sellerName = doc.getString("sellerName") ?: "",
                    canteenId = doc.getString("canteenId") ?: "",
                    canteenName = doc.getString("canteenName") ?: "",
                    menuId = doc.getString("menuId") ?: "",
                    name = doc.getString("name") ?: "",
                    price = doc.getLong("price")?.toInt() ?: 0,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    category = doc.getString("category") ?: "",
                    status = doc.getString("status") ?: "pending",
                    type = doc.getString("type") ?: "add",
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                )
            }
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin: approve/reject menu request.
     * Kalau approve → eksekusi aksi (add/edit/delete menu) sekaligus update status request.
     */
    suspend fun resolveMenuRequest(request: MenuRequest, approved: Boolean): Result<Unit> {
        return try {
            val newStatus = if (approved) "approved" else "rejected"

            // Update status di menu_requests
            firestore.collection("menu_requests")
                .document(request.id)
                .update("status", newStatus)
                .await()

            // Kalau di-approve, eksekusi aksi nyata ke collection "menu"
            if (approved) {
                when (request.type) {
                    "add" -> {
                        val menuData = hashMapOf(
                            "canteenId" to request.canteenId,
                            "canteenName" to request.canteenName,
                            "name" to request.name,
                            "price" to request.price,
                            "imageUrl" to request.imageUrl,
                            "category" to request.category,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        firestore.collection("menu").add(menuData).await()
                    }
                    "edit" -> {
                        if (request.menuId.isNotEmpty()) {
                            val updates = hashMapOf<String, Any>(
                                "name" to request.name,
                                "price" to request.price,
                                "imageUrl" to request.imageUrl,
                                "category" to request.category
                            )
                            firestore.collection("menu")
                                .document(request.menuId)
                                .update(updates)
                                .await()
                        }
                    }
                    "delete" -> {
                        if (request.menuId.isNotEmpty()) {
                            firestore.collection("menu")
                                .document(request.menuId)
                                .delete()
                                .await()
                        }
                    }
                }
            }

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

            val orders = snapshot.documents.map { doc -> doc.toOrder() }
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

            val orders = snapshot.documents.map { doc -> doc.toOrder() }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Seller: ambil pesanan yang sudah selesai (status = "done" / "completed")
     */
    suspend fun getCompletedOrdersForSeller(canteenId: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("canteenId", canteenId)
                .whereIn("status", listOf("done", "completed", "selesai"))
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.map { doc -> doc.toOrder() }
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

    // =============================
    // 🔧 PRIVATE HELPERS
    // =============================

    private fun com.google.firebase.firestore.DocumentSnapshot.toOrder(): Order {
        val itemsList = (get("items") as? List<Map<String, Any>>)?.map {
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

        return Order(
            id = id,
            userId = getString("userId") ?: "",
            userName = getString("userName") ?: "",
            items = itemsList,
            totalPrice = getLong("totalPrice")?.toInt() ?: 0,
            status = getString("status") ?: "pending",
            canteenId = getString("canteenId") ?: "",
            canteenName = getString("canteenName") ?: "",
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
        )
    }
}

// =============================
// 📦 DATA MODELS
// =============================

data class MenuRequest(
    val id: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val canteenId: String = "",
    val canteenName: String = "",
    val menuId: String = "",       // hanya untuk type "edit" dan "delete"
    val name: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    val category: String = "",
    val status: String = "pending", // pending | approved | rejected
    val type: String = "add",       // add | edit | delete
    val createdAt: Long = System.currentTimeMillis()
)