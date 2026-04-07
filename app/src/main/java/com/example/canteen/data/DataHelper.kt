package com.example.canteen.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataHelper(context: Context) :
    SQLiteOpenHelper(context, "canteen.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT UNIQUE," +
                    "password TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun register(email: String, password: String): Boolean {
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
}