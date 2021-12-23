package com.example.nav_contacts

//class DBHelper {
//}

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.lang.Exception

class DBHelper(val context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY, " +
                FIRST_NAME + " TEXT," +
                LAST_NAME + " TEXT," +
                NUMBER1 + " TEXT," +
                NUMBER2 + " TEXT," +
                FAVORITE + " TEXT," +
                EMAIL + " TEXT" + ")")
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        Toast.makeText(context, "update", Toast.LENGTH_SHORT).show()
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME)
        onCreate(db)
    }
    fun deleteValues(contact: Contact){
        val db=this.writableDatabase
        val value=(contact.dbID).toString()
        db.delete(TABLE_NAME, ID+"=?", arrayOf(value))
    }
    fun addToDatabase(firstName : String,lastName : String,number1:String,number2:String,email:String,favorite:Boolean){
        val values = ContentValues()
        val db = this.writableDatabase
        values.put(FIRST_NAME, firstName)
        values.put(LAST_NAME, lastName)
        values.put(NUMBER1,number1)
        values.put(NUMBER2,number2)
        values.put(EMAIL,email)
        values.put(FAVORITE,favorite)
        db.insert(TABLE_NAME, null, values)
//        Toast.makeText(context, "created $firstName success", Toast.LENGTH_SHORT).show()
    }
    fun update(contact: Contact){
        val db=this.writableDatabase
        val cv=ContentValues()
        cv.put(FIRST_NAME,contact.firstName)
        cv.put(LAST_NAME,contact.lastName)
        if(contact.number.isNotEmpty()) {
            cv.put(NUMBER1, contact.number[0])
            if(contact.number.size==2)
            cv.put(NUMBER2, contact.number[1])
        }
        cv.put(EMAIL,contact.email)
        cv.put(FAVORITE,contact.favorite)
        try {
            db.update(TABLE_NAME, cv, "$ID=?", arrayOf((contact.dbID.toString())))
        }catch (e:Exception){Toast.makeText(context, "exception", Toast.LENGTH_SHORT).show()}
    }
    fun getName(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }
    fun getAllData():ArrayList<Contact>{
        val db=this.readableDatabase
        val contactList=ArrayList<Contact>()
        val cursor=db.rawQuery("SELECT * FROM "+ TABLE_NAME,null)
//        cursor!!.moveToFirst()
        while (cursor.moveToNext()){
            val firstName=cursor.getString(1)
            val id=cursor.getInt(0)
            val lastName=cursor.getString(2)
            val number=ArrayList<String>()
            val number1=cursor.getString(3)
            if(!number1.equals("empty"))
                number.add(number1)
            val number2=cursor.getString(4)
            if(!number2.equals("empty"))
                number.add(number2)
            val favorites=cursor.getString(5).toInt()==1
//            Toast.makeText(context, "$favorites", Toast.LENGTH_SHORT).show()
            val email=cursor.getString(6)
            contactList.add(Contact(firstName,lastName,number,email,favorites, dbID =id ))
        }
        return contactList

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
    }
}
