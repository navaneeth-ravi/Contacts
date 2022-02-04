package com.example.nav_contacts

import android.database.Cursor
import android.provider.ContactsContract
import com.example.nav_contacts.ContactMain.contentResolver
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.lang.Exception

object SystemContact {
    suspend fun getAllContacts(){
        val cursor = getAllContactsQuery()
        if (cursor!!.moveToNext()) {
            while (!cursor.isAfterLast) {
                val contact = getContact(cursor)
                if(contact != null){
                    val values = ContactDataClass.getContentValuesForContact(contact)
                    DatabaseFunctionalities.insert(values)
                }
                cursor.moveToNext()
            }
        }
    }
    private suspend fun getAllContactsQuery(): Cursor?{
        val cursor: Deferred<Cursor?> = GlobalScope.async {
            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, null,
                null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC "
            )
        }
        return cursor.await()
    }
    suspend fun getContact(cursor: Cursor?):ContactDataClass?{
        if(cursor!=null){
            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val contactId = cursor.getString( idIndex )
            val displayNameId = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val displayName = cursor.getString(displayNameId).trim()
            var firstName: String
            var lastName: String
            val starredId=cursor.getColumnIndex(ContactsContract.Contacts.STARRED)
            val favorite:Boolean = cursor.getString(starredId) == "1"
            try {
                lastName =
                    displayName.substring(displayName.indexOf(" ") + 1, displayName.length)
                firstName = displayName.substring(0, displayName.indexOf(" "))
            }catch (e: Exception){
                firstName = displayName
                lastName = ""
            }
            val number:ArrayList<String> = getPhoneNumbers(contactId)
            val email = getEmail(contactId)
            if(number.size==0){
                number.add(ContactMain.resources.getString(R.string.empty))
                number.add(ContactMain.resources.getString(R.string.empty))
            }else if(number.size==1){
                number.add(ContactMain.resources.getString(R.string.empty))
            }
            return ContactDataClass(
                firstName,
                lastName,
                number,
                email = email,
                favorite = favorite,
                profileImage = firstName+""+ContactMain.resources.getString(R.string.image_format)
            )
        }
        else{
            return null
        }
    }
    private suspend fun getPhoneNumbers(contactId:String):ArrayList<String>{
        val phone=GlobalScope.async {
            val phones = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                arrayOf(contactId),
                null
            )
            var count=0
            val number:ArrayList<String> = ArrayList()
            while (phones!!.moveToNext() && count<2) {
                val phoneId= phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val phoneNumber=phones.getString(phoneId).replace("\\s".toRegex(), "")
                if(!number.contains(phoneNumber)) {
                    number.add(phoneNumber)
                    count++
                }
            }
            phones.close()
            number
        }
        return phone.await()
    }
    private suspend fun getEmail(contactId:String):String{
        val email=GlobalScope.async {
            val emails = contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                arrayOf(contactId),
                null
            )
            var email = ""
            if (emails!!.moveToNext()) {
                val emailId = emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                email = emails.getString(emailId)
            }
            emails.close()
            email
        }
        return email.await()
    }
}