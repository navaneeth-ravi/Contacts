package com.example.nav_contacts

import android.content.ContentResolver
import android.content.ContentValues
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DatabaseFunctionalities {
    fun update(values: ContentValues,contact:ContactDataClass,contentResolver: ContentResolver){
        GlobalScope.launch{
            values.put(MyContentProvider.FAVORITE,contact.favorite)
            contentResolver.update(MyContentProvider.CONTENT_URI,values,MyContentProvider.ID+"=?", arrayOf(contact.dbID.toString()))
        }
    }
    fun delete(dbID:String,contentResolver: ContentResolver){
        GlobalScope.launch {
            contentResolver.delete(
                MyContentProvider.CONTENT_URI, MyContentProvider.ID + "=?",
                arrayOf(dbID)
            )
            MainActivity.makeFavoriteContactList()
        }
    }
    fun insert(values: ContentValues,contentResolver: ContentResolver){
        GlobalScope.launch {
            contentResolver.insert(MyContentProvider.CONTENT_URI, values)
            getAllContactDataFromDatabase(contentResolver)
        }
    }
    fun getAllContactDataFromDatabase(contentResolver: ContentResolver,activity: MainActivity?=null) {
            val cursor =
                contentResolver.query(MyContentProvider.CONTENT_URI, null, null, null, null)
            MainActivity.contactList = ArrayList()
            if (cursor!!.moveToNext()) {
                val firstNameId = cursor.getColumnIndex(MyContentProvider.FIRST_NAME)
                val lastNameId = cursor.getColumnIndex(MyContentProvider.LAST_NAME)
                val dbId = cursor.getColumnIndex(MyContentProvider.ID)
                val number1Id = cursor.getColumnIndex(MyContentProvider.NUMBER1)
                val number2Id = cursor.getColumnIndex(MyContentProvider.NUMBER2)
                val emailColumnId = cursor.getColumnIndex(MyContentProvider.EMAIL)
                val favoriteId = cursor.getColumnIndex(MyContentProvider.FAVORITE)
                val profileImageId = cursor.getColumnIndex(MyContentProvider.PROFILE_IMAGE)
                while (!cursor.isAfterLast) {
                    val firstName = cursor.getString(firstNameId)
                    val id = cursor.getInt(dbId)
                    val lastName = cursor.getString(lastNameId)
                    val number = ArrayList<String>()
                    val number1 = cursor.getString(number1Id)
                    if (!number1.equals("empty") && (number1 != null))
                        number.add(number1)
                    val number2 = cursor.getString(number2Id)
                    if (!number2.equals("empty") && (number2 != null))
                        number.add(number2)
                    val favorites = cursor.getString(favoriteId).toInt() == 1
                    val email = cursor.getString(emailColumnId)
                    val profileImageFileName = cursor.getString(profileImageId)
                    MainActivity.contactList.add(
                        ContactDataClass(
                            firstName,
                            lastName,
                            number,
                            email = email,
                            favorite = favorites,
                            dbID = id,
                            profileImage = profileImageFileName
                        )
                    )
                    cursor.moveToNext()
                }
                cursor.close()
                MainActivity.contactList.sortBy { it.firstName }
                MainActivity.makeFavoriteContactList()
            }
            if(activity?.displayContactsFragment?.isRecyclerInitialized() == true) {
                activity.refreshRecycler()
            }
    }
}