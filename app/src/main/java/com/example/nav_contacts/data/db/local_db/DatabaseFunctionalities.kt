package com.example.nav_contacts.data.db.local_db

import android.content.ContentValues
import android.database.Cursor
import com.example.nav_contacts.ContactMain
import kotlinx.coroutines.*

object DatabaseFunctionalities {

    fun update(values: ContentValues, dbID: Int?){
        GlobalScope.launch(Dispatchers.IO){
            ContactMain.contentResolver.update(
                MyContentProvider.CONTENT_URI,values,
                MyContentProvider.ID +"=?", arrayOf(dbID.toString()))
        }
    }
    fun delete(dbID: String){
        GlobalScope.launch(Dispatchers.IO) {
            ContactMain.contentResolver.delete(
                MyContentProvider.CONTENT_URI, MyContentProvider.ID + "=?",
                arrayOf(dbID)
            )
        }
    }
    fun insert(values: ContentValues){
        GlobalScope.launch(Dispatchers.IO){
            val a= ContactMain.contentResolver.insert(MyContentProvider.CONTENT_URI, values)
//            Uri.parse(MyContentProvider.CONTENT_URI+"/"+)
        }
    }
    suspend fun getContact(dbID: String):Cursor?{
        val job:Deferred<Cursor?> =GlobalScope.async(Dispatchers.IO) {
            ContactMain.contentResolver.query(MyContentProvider.CONTENT_URI, null, MyContentProvider.ID +"=?", arrayOf(dbID), null)
        }
        return job.await()
    }
    suspend fun getAllContactDataFromDatabase():Cursor?{
        val job=GlobalScope.async(Dispatchers.IO) {
            ContactMain.contentResolver.query(MyContentProvider.CONTENT_URI, null, null, null, null)
        }
        return job.await()
    }
}
