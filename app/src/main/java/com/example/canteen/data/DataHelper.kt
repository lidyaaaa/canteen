package com.example.canteen.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataHelper(context: Context) :
    SQLiteOpenHelper(context, "canteen.db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT UNIQUE," +
                    "password TEXT," +
                    "name TEXT)"
        )

        db.execSQL(
            "CREATE TABLE menu (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "place TEXT," +
                    "price TEXT," +
                    "imageUri TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE menu ADD COLUMN imageUri TEXT")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // =========================
    // 🔐 USER
    // =========================

    fun register(email: String, password: String, name: String): Boolean {
        val db = writableDatabase

        val check = db.rawQuery(
            "SELECT * FROM users WHERE email=?",
            arrayOf(email)
        )

        if (check.count > 0) {
            check.close()
            return false
        }

        val values = ContentValues().apply {
            put("email", email)
            put("password", password)
            put("name", name)
        }

        db.insert("users", null, values)
        return true
    }

    fun login(email: String, password: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email=? AND password=?",
            arrayOf(email, password)
        )

        val success = cursor.count > 0
        cursor.close()
        return success
    }

    fun getUserName(email: String): String {
        var name = "Guest"

        try {
            val db = readableDatabase

            val cursor = db.rawQuery(
                "SELECT name FROM users WHERE email=?",
                arrayOf(email)
            )

            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex("name")
                if (index != -1 && !cursor.isNull(index)) {
                    name = cursor.getString(index)
                }
            }

            cursor?.close()

        } catch (e: Exception) {
            e.printStackTrace()
            return "Guest"
        }

        return name
    }

    // =========================
    // 🍔 MENU
    // =========================

    fun insertMenu(name: String, place: String, price: String, imageUri: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("place", place)
            put("price", price)
            put("imageUri", imageUri)
        }
        db.insert("menu", null, values)
    }

    fun getAllMenu(): MutableList<MenuItem> {
        val list = mutableListOf<MenuItem>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM menu", null)

        if (cursor != null && cursor.moveToFirst()) {
            do {

                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))

                val nameIndex = cursor.getColumnIndex("name")
                val name = if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                    cursor.getString(nameIndex)
                } else ""

                val placeIndex = cursor.getColumnIndex("place")
                val place = if (placeIndex != -1 && !cursor.isNull(placeIndex)) {
                    cursor.getString(placeIndex)
                } else ""

                val priceIndex = cursor.getColumnIndex("price")
                val price = if (priceIndex != -1 && !cursor.isNull(priceIndex)) {
                    cursor.getString(priceIndex)
                } else ""

                val imageIndex = cursor.getColumnIndex("imageUri")
                val imageUri = if (imageIndex != -1 && !cursor.isNull(imageIndex)) {
                    cursor.getString(imageIndex)
                } else ""

                list.add(
                    MenuItem(
                        id = id,
                        name = name,
                        place = place,
                        price = price,
                        imageUri = imageUri
                    )
                )

            } while (cursor.moveToNext())
        }

        cursor?.close()
        return list
    }

    fun getMenuById(id: Int): MenuItem? {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM menu WHERE id=?",
            arrayOf(id.toString())
        )

        var menu: MenuItem? = null

        if (cursor != null && cursor.moveToFirst()) {

            val name = cursor.getString(cursor.getColumnIndexOrThrow("name")) ?: ""
            val place = cursor.getString(cursor.getColumnIndexOrThrow("place")) ?: ""
            val price = cursor.getString(cursor.getColumnIndexOrThrow("price")) ?: ""

            val imageIndex = cursor.getColumnIndex("imageUri")
            val imageUri = if (imageIndex != -1 && !cursor.isNull(imageIndex)) {
                cursor.getString(imageIndex)
            } else ""

            menu = MenuItem(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                name = name,
                place = place,
                price = price,
                imageUri = imageUri
            )
        }

        cursor?.close()
        return menu
    }

    fun updateMenuById(id: Int, name: String, place: String, price: String, imageUri: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("place", place)
            put("price", price)
            put("imageUri", imageUri)
        }
        db.update("menu", values, "id=?", arrayOf(id.toString()))
    }

    fun deleteMenu(id: Int) {
        val db = writableDatabase
        db.delete("menu", "id=?", arrayOf(id.toString()))
    }
}