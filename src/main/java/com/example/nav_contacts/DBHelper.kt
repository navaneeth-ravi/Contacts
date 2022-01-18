package com.example.nav_contacts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import androidx.core.util.rangeTo

class DBHelper(val context: Context, factory: CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY, " +
                FIRST_NAME + " TEXT," +
                LAST_NAME + " TEXT," +
                NUMBER1 + " TEXT," +
                NUMBER2 + " TEXT," +
                FAVORITE + " TEXT," +
                EMAIL + " TEXT," +
                PROFILE_IMAGE + " TEXT" +")")
        db.execSQL(query)
    }
    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        Toast.makeText(context, "update", Toast.LENGTH_SHORT).show()
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object{
        private const val DATABASE_NAME = "CONTACTSDB"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "contacts"
        const val ID = "id"
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val NUMBER1="number1"
        const val NUMBER2="number2"
        const val EMAIL="email"
        const val FAVORITE="favorite"
        const val PROFILE_IMAGE="image"
    }
}
