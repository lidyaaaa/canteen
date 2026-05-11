package com.example.canteen.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class DataHelper(context: Context) :
    SQLiteOpenHelper(context, "canteen.db", null, 7) {

    override fun onCreate(db: SQLiteDatabase) {

        // =========================
        // 👤 USERS
        // =========================
        db.execSQL(
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT UNIQUE," +
                    "password TEXT," +
                    "name TEXT," +
                    "role TEXT" +
                    ")"
        )

        // =========================
        // 🏪 CANTEENS
        // =========================
        db.execSQL(
            "CREATE TABLE canteens (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "ownerId INTEGER" +
                    ")"
        )

        // =========================
        // 📝 SELLER REQUESTS
        // =========================
        db.execSQL(
            "CREATE TABLE seller_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "userId INTEGER," +
                    "canteenName TEXT," +
                    "description TEXT," +
                    "status TEXT" +
                    ")"
        )

        // =========================
        // 🍔 MENU
        // =========================
        db.execSQL(
            "CREATE TABLE menu (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "canteenId INTEGER," +
                    "name TEXT," +
                    "price INTEGER," +
                    "imageUri TEXT" +
                    ")"
        )

        // 🔥 ADMIN DEFAULT (dengan password yang di-hash)
        db.execSQL(
            "INSERT INTO users (email, password, name, role) VALUES (" +
                    "'admin@gmail.com'," +
                    "'${hashPassword("admin123")}'," +
                    "'Admin'," +
                    "'admin'" +
                    ")"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS canteens")
        db.execSQL("DROP TABLE IF EXISTS seller_requests")
        db.execSQL("DROP TABLE IF EXISTS menu")

        onCreate(db)
    }

    // =========================
    // 🔐 PASSWORD HASHING
    // =========================

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    // =========================
    // 🔐 USER
    // =========================

    fun register(
        email: String,
        password: String,
        name: String,
        role: String
    ): Boolean {

        val db = writableDatabase

        val check = db.rawQuery(
            "SELECT * FROM users WHERE email=?",
            arrayOf(email)
        )

        if (check.count > 0) {
            check.close()
            return false
        }

        check.close()

        val hashedPassword = hashPassword(password)

        val values = ContentValues().apply {
            put("email", email)
            put("password", hashedPassword)
            put("name", name)
            put("role", role)
        }

        val result = db.insert(
            "users",
            null,
            values
        )

        return result != -1L
    }

    fun login(
        email: String,
        password: String
    ): String? {

        val db = readableDatabase
        val hashedPassword = hashPassword(password)

        val cursor = db.rawQuery(
            "SELECT role FROM users WHERE email=? AND password=?",
            arrayOf(email, hashedPassword)
        )

        var role: String? = null

        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }

        cursor.close()

        return role
    }

    fun getUserName(email: String): String {

        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM users WHERE email=?",
            arrayOf(email)
        )

        var name = "User"

        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }

        cursor.close()

        return name
    }

    fun getUserIdByEmail(email: String): Int? {

        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM users WHERE email=?",
            arrayOf(email)
        )

        var id: Int? = null

        if (cursor.moveToFirst()) {
            id = cursor.getInt(0)
        }

        cursor.close()

        return id
    }

    fun getUserRoleByEmail(email: String): String? {

        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT role FROM users WHERE email=?",
            arrayOf(email)
        )

        var role: String? = null

        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }

        cursor.close()

        return role
    }

    // =========================
    // 🏪 CANTEENS
    // =========================

    fun insertCanteen(
        name: String,
        ownerId: Int
    ) {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("name", name)
            put("ownerId", ownerId)
        }

        db.insert(
            "canteens",
            null,
            values
        )
    }

    fun getCanteenNameById(id: Int): String {

        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM canteens WHERE id=?",
            arrayOf(id.toString())
        )

        var name = "Unknown Canteen"

        if (cursor.moveToFirst()) {

            name = cursor.getString(
                cursor.getColumnIndexOrThrow("name")
            )
        }

        cursor.close()

        return name
    }

    fun getCanteenIdByOwnerId(ownerId: Int): Int? {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM canteens WHERE ownerId=?",
            arrayOf(ownerId.toString())
        )

        var canteenId: Int? = null

        if (cursor.moveToFirst()) {

            canteenId = cursor.getInt(
                cursor.getColumnIndexOrThrow("id")
            )
        }

        cursor.close()

        return canteenId
    }

    fun getAllCanteens(): List<Canteen> {

        val list = mutableListOf<Canteen>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM canteens",
            null
        )

        if (cursor.moveToFirst()) {

            do {

                val canteen = Canteen(
                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                    ).toString(),
                    name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                    ),
                    ownerId = cursor.getInt(
                        cursor.getColumnIndexOrThrow("ownerId")
                    ).toString(),
                    ownerName = "" // SQLite doesn't have this yet
                )

                list.add(canteen)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    // =========================
    // 📝 SELLER REQUESTS
    // =========================

    fun insertSellerRequest(
        userId: Int,
        canteenName: String,
        description: String
    ): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("userId", userId)
            put("canteenName", canteenName)
            put("description", description)
            put("status", "pending")
        }

        val result = db.insert(
            "seller_requests",
            null,
            values
        )

        return result != -1L
    }

    fun getAllPendingRequests(): List<SellerRequest> {

        val list = mutableListOf<SellerRequest>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM seller_requests WHERE status='pending'",
            null
        )

        if (cursor.moveToFirst()) {

            do {

                val sellerRequest = SellerRequest(
                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                    ),
                    userId = cursor.getInt(
                        cursor.getColumnIndexOrThrow("userId")
                    ),
                    canteenName = cursor.getString(
                        cursor.getColumnIndexOrThrow("canteenName")
                    ),
                    description = cursor.getString(
                        cursor.getColumnIndexOrThrow("description")
                    ),
                    status = cursor.getString(
                        cursor.getColumnIndexOrThrow("status")
                    )
                )

                list.add(sellerRequest)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    fun updateRequestStatus(
        id: Int,
        status: String
    ) {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("status", status)
        }

        db.update(
            "seller_requests",
            values,
            "id=?",
            arrayOf(id.toString())
        )
    }

    fun getSellerRequestById(id: Int): SellerRequest? {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM seller_requests WHERE id=?",
            arrayOf(id.toString())
        )

        var request: SellerRequest? = null

        if (cursor.moveToFirst()) {

            request = SellerRequest(
                id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
                ),
                userId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("userId")
                ),
                canteenName = cursor.getString(
                    cursor.getColumnIndexOrThrow("canteenName")
                ),
                description = cursor.getString(
                    cursor.getColumnIndexOrThrow("description")
                ),
                status = cursor.getString(
                    cursor.getColumnIndexOrThrow("status")
                )
            )
        }

        cursor.close()

        return request
    }

    // =========================
    // 🍔 MENU
    // =========================

    fun insertMenu(
        canteenId: Int,
        name: String,
        price: Int,
        imageUri: String
    ) {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("canteenId", canteenId)
            put("name", name)
            put("price", price)
            put("imageUri", imageUri)
        }

        db.insert(
            "menu",
            null,
            values
        )
    }

    fun getAllMenu(): MutableList<MenuItem> {

        val list = mutableListOf<MenuItem>()

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM menu",
            null
        )

        if (cursor.moveToFirst()) {

            do {

                val canteenId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("canteenId")
                )

                val item = MenuItem(

                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                    ).toString(),

                    canteenId = canteenId.toString(),

                    canteenName = getCanteenNameById(
                        canteenId
                    ),

                    name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                    ),

                    price = cursor.getInt(
                        cursor.getColumnIndexOrThrow("price")
                    ),

                    imageUrl = cursor.getString(
                        cursor.getColumnIndexOrThrow("imageUri")
                    ) ?: ""
                )

                list.add(item)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    fun getMenuByCanteenId(canteenId: Int): List<MenuItem> {

        val list = mutableListOf<MenuItem>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM menu WHERE canteenId=?",
            arrayOf(canteenId.toString())
        )

        if (cursor.moveToFirst()) {

            do {

                val item = MenuItem(
                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                    ).toString(),
                    canteenId = canteenId.toString(),
                    canteenName = getCanteenNameById(canteenId),
                    name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                    ),
                    price = cursor.getInt(
                        cursor.getColumnIndexOrThrow("price")
                    ),
                    imageUrl = cursor.getString(
                        cursor.getColumnIndexOrThrow("imageUri")
                    ) ?: ""
                )

                list.add(item)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    fun getMenuById(id: Int): MenuItem? {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM menu WHERE id=?",
            arrayOf(id.toString())
        )

        var menu: MenuItem? = null

        if (cursor.moveToFirst()) {

            val canteenId = cursor.getInt(
                cursor.getColumnIndexOrThrow("canteenId")
            )

            menu = MenuItem(

                id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
                ).toString(),

                canteenId = canteenId.toString(),

                canteenName = getCanteenNameById(
                    canteenId
                ),

                name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
                ),

                price = cursor.getInt(
                    cursor.getColumnIndexOrThrow("price")
                ),

                imageUrl = cursor.getString(
                    cursor.getColumnIndexOrThrow("imageUri")
                ) ?: ""
            )
        }

        cursor.close()

        return menu
    }

    fun updateMenuById(
        id: Int,
        name: String,
        price: Int,
        imageUri: String,
        canteenId: Int
    ) {

        val db = writableDatabase

        val values = ContentValues().apply {

            put("canteenId", canteenId)
            put("name", name)
            put("price", price)
            put("imageUri", imageUri)
        }

        db.update(
            "menu",
            values,
            "id=?",
            arrayOf(id.toString())
        )
    }

    fun deleteMenu(id: Int) {

        val db = writableDatabase

        db.delete(
            "menu",
            "id=?",
            arrayOf(id.toString())
        )
    }
}