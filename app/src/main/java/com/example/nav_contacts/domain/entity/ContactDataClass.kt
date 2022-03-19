package com.example.nav_contacts.domain.entity

import android.content.ContentValues
import android.database.Cursor
import com.example.nav_contacts.ContactMain
import com.example.nav_contacts.R
import com.example.nav_contacts.data.db.local_db.MyContentProvider
import com.example.nav_contacts.presentation.fragment.ContactsDisplayFragment


data class ContactDataClass(var firstName: String,var lastName: String,var number:ArrayList<String>,
                            var favorite:Boolean=false,var email: String, var dbID:Int?=null, var profileImage: String){
    companion object{
        fun getContentValuesForContact(data: ContactDataClass):ContentValues{
            val values=ContentValues()
            values.put(MyContentProvider.ID,data.dbID)
            values.put(MyContentProvider.FIRST_NAME,data.firstName)
            values.put(MyContentProvider.LAST_NAME,data.lastName)
            values.put(MyContentProvider.EMAIL,data.email)
            values.put(MyContentProvider.FAVORITE,data.favorite)
            values.put(
                MyContentProvider.PROFILE_IMAGE, data.firstName+data.lastName+ ContactMain.resources.getString(
                R.string.image_format
            ))
            when( data.number.size) {
                2 -> {
                   values.put(MyContentProvider.NUMBER1, data.number[0])
                   values.put(MyContentProvider.NUMBER2, data.number[1])
                }
                1-> {
                    values.put(MyContentProvider.NUMBER1, data.number[0])
                    values.put(MyContentProvider.NUMBER2, ContactsDisplayFragment.EMPTY_STRING)
                }
                0 -> {
                    values.put(MyContentProvider.NUMBER2, ContactsDisplayFragment.EMPTY_STRING)
                    values.put(MyContentProvider.NUMBER1, ContactsDisplayFragment.EMPTY_STRING)
                }
            }
            return values
        }
        fun getContact(cursor:Cursor?): ContactDataClass?{
            if (cursor!=null) {
                try {
                    val firstNameId = cursor.getColumnIndex(MyContentProvider.FIRST_NAME)
                    val lastNameId = cursor.getColumnIndex(MyContentProvider.LAST_NAME)
                    val dbId = cursor.getColumnIndex(MyContentProvider.ID)
                    val number1Id = cursor.getColumnIndex(MyContentProvider.NUMBER1)
                    val number2Id = cursor.getColumnIndex(MyContentProvider.NUMBER2)
                    val emailColumnId = cursor.getColumnIndex(MyContentProvider.EMAIL)
                    val favoriteId = cursor.getColumnIndex(MyContentProvider.FAVORITE)
                    val profileImageId = cursor.getColumnIndex(MyContentProvider.PROFILE_IMAGE)
                    val firstName = cursor.getString(firstNameId)
                    val id = cursor.getInt(dbId)
                    val lastName = cursor.getString(lastNameId)
                    val number = ArrayList<String>()
                    val number1 = cursor.getString(number1Id)
                    if (!number1.equals(ContactMain.resources.getString(R.string.empty)) && (number1 != null)) {
                        number.add(number1)
                    }
                    val number2 = cursor.getString(number2Id)
                    if (!number2.equals(ContactMain.resources.getString(R.string.empty)) && (number2 != null)) {
                        number.add(number2)
                    }
                    val favorites = cursor.getString(favoriteId).toInt() == 1
                    val email = cursor.getString(emailColumnId)
                    val profileImageFileName = cursor.getString(profileImageId)
                    return ContactDataClass(
                        firstName,
                        lastName,
                        number,
                        email = email,
                        favorite = favorites,
                        dbID = id,
                        profileImage = profileImageFileName
                    )
                }catch (e: java.lang.IndexOutOfBoundsException){
                    return null
                }
            }
            else {
                return null
            }
        }
    }
}