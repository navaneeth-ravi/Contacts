package com.example.nav_contacts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class Database {
    companion object {
        var list: ArrayList<ContactDataClass> = ArrayList()
        var favList: ArrayList<ContactDataClass> = ArrayList()
        lateinit var database: DBHelper
        lateinit var writeableDatabase: SQLiteDatabase
        lateinit var readableDatabase: SQLiteDatabase

        private val firstName= arrayOf("Police","Goverment","SIM","IRCTC","Fire","Women's ","Corona TN","Traffic"," Navaneethan")
        private val lastName= arrayOf("","Ambulance","Complaint","HelpLine","Service","HelpLine","HelpLine","HelpLine","")
        private val numberdum= arrayOf("100","108","199","1800111139","101","1091","+91-11-23978046","1073","8838900839")
        private var isInsertedToDatabase= false
        fun dummy(){
            if(!isInsertedToDatabase) {
                for (i in firstName.indices) {
                    addContactToDatabaseTable(
                        firstName[i],
                        lastName[i],
                        numberdum[i],
                        number2 = "empty",
                        favorite = true
                    )
                }
                isInsertedToDatabase = true
                getAlldata()
            }
        }
        fun makeFavResult() {
            favList = ArrayList()
            for (i in list)
                if (i.favorite)
                    favList.add(i)
        }
        fun openDatabase(context: Context) {
            database = DBHelper(context, null)
            writeableDatabase = database.writableDatabase
            readableDatabase = database.readableDatabase
        }
        fun insert(contact: ContactDataClass) {
            var number = ArrayList<String>()
            if (contact.number.isNotEmpty()) {
                number.add(contact.number[0])
                if (contact.number.size == 2) {
                    number = contact.number
                } else {
                    number.add("empty")
                }
            } else {
                number.add("empty")
                number.add("empty")
            }
            retrieve(
                contact.firstName,
                contact.lastName,
                number[0],
                number[1],
                contact.email,
                contact.favorite,
                contact.dbID
            )
        }
        private fun retrieve(
            firstName: String,
            lastName: String,
            number1: String = "",
            number2: String = "",
            email: String = "",
            favorite: Boolean,
            id: Int?
        ) {
            val values = ContentValues()
            values.put(DBHelper.ID, id)
            values.put(DBHelper.FIRST_NAME, firstName)
            values.put(DBHelper.LAST_NAME, lastName)
            values.put(DBHelper.NUMBER1, number1)
            values.put(DBHelper.NUMBER2, number2)
            values.put(DBHelper.EMAIL, email)
            values.put(DBHelper.FAVORITE, favorite)
            values.put(DBHelper.PROFILE_IMAGE, "$firstName$lastName.png")
            writeableDatabase.insert(DBHelper.TABLE_NAME, null, values)
        }

        fun update(contact: ContactDataClass) {
            val cv = ContentValues()
            cv.put(DBHelper.FIRST_NAME, contact.firstName)
            cv.put(DBHelper.LAST_NAME, contact.lastName)
            if (contact.number.isNotEmpty()) {
                cv.put(DBHelper.NUMBER1, contact.number[0])
                if (contact.number.size == 2) {
                    cv.put(DBHelper.NUMBER2, contact.number[1])
                }
            }
            cv.put(DBHelper.EMAIL, contact.email)
            cv.put(DBHelper.FAVORITE, contact.favorite)
            cv.put(DBHelper.PROFILE_IMAGE, "${contact.firstName + contact.lastName}.png")
            try {
                writeableDatabase.update(
                    DBHelper.TABLE_NAME,
                    cv,
                    "${DBHelper.ID}=?",
                    arrayOf((contact.dbID.toString()))
                )
            } catch (e: Exception) {
//                Toast.makeText(context, "exception", Toast.LENGTH_SHORT).show()
            }

        }

        fun getAlldata() {
            val contactList = ArrayList<ContactDataClass>()
            val cursor = readableDatabase.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME, null)
            while (cursor.moveToNext()) {
                val firstName = cursor.getString(1)
                val id = cursor.getInt(0)
                val lastName = cursor.getString(2)
                val number = ArrayList<String>()
                val number1 = cursor.getString(3)
                if (!number1.equals("empty"))
                    number.add(number1)
                val number2 = cursor.getString(4)
                if (!number2.equals("empty"))
                    number.add(number2)
                val favorites = cursor.getString(5).toInt() == 1
//            Toast.makeText(context, "$favorites", Toast.LENGTH_SHORT).show()
                val email = cursor.getString(6)
                val profileImageId = cursor.getString(7)
                contactList.add(
                    ContactDataClass(
                        firstName,
                        lastName,
                        number,
                        email=email,
                        favorite = favorites,
                        dbID = id,
                        profileImage = profileImageId
                    )
                )
            }
            contactList.sortBy { it.firstName }
            list = contactList
            makeFavResult()
        }

        fun delete(contact: ContactDataClass) {
            val value = (contact.dbID).toString()
            writeableDatabase.delete(DBHelper.TABLE_NAME, DBHelper.ID + "=?", arrayOf(value))
        }

        fun addContactToDatabaseTable(
            firstName: String,
            lastName: String,
            number1: String = "",
            number2: String = "",
            email: String = "",
            favorite: Boolean
        ) {
            val values = ContentValues()
            values.put(DBHelper.FIRST_NAME, firstName)
            values.put(DBHelper.LAST_NAME, lastName)
            values.put(DBHelper.NUMBER1, number1)
            values.put(DBHelper.NUMBER2, number2)
            values.put(DBHelper.EMAIL, email)
            values.put(DBHelper.FAVORITE, favorite)
            values.put(DBHelper.PROFILE_IMAGE, "$firstName$lastName.png")
            writeableDatabase.insert(DBHelper.TABLE_NAME, null, values)
//        Toast.makeText(context, "created $firstName success", Toast.LENGTH_SHORT).show()
        }
    }
}



