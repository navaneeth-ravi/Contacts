package com.example.nav_contacts

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast

class Database(val context: Context?=null) {

    companion object {
        var list: ArrayList<Contact> = ArrayList()
        var favList: ArrayList<Contact> = ArrayList()
        fun makeFavResult(){
            favList=ArrayList()
            for(i in list)
                if(i.favorite)
                    favList.add(i)
        }

//        init {
//            val number: ArrayList<String> = ArrayList()
//            number.add("8838900839")
//            number.add("7539916246")
//            list.add(Contact("Navaneethan", "Ravi", number, "abc@gmail.com", true))
//            list.add(Contact("vishnu", "N", number, "darls@nav.com", true))
//            list.add(Contact("Naani", "N", number, "mail"))
//            list.add(Contact("Adhi", "Ravi", number, "mail"))
//            list.add(Contact("Ravi", "J", number, "mail"))
//            list.add(Contact("Rajakumari", "R", number, "mail"))
//            list.add(Contact("Rajini", "Super", number, "mail"))
//
//            for (i in list) {
//                Log.i("abc", "getFavorites:for ")
//                if (i.favorite) {
//                    Log.i("abc", "getFavorites: ")
//                    favList.add(i)
//                }
//            }
//        }

    }
    fun insert(contact: Contact){
        val db= context?.let { DBHelper(it,null) }
        var number=ArrayList<String>()
        if(contact.number.isNotEmpty()){
            number.add(contact.number[0])
            if(contact.number.size==2){
               number= contact.number
            }else{
                number.add("empty")
            }
        }else{
            number.add("empty")
            number.add("empty")
        }
        db?.addToDatabase(contact.firstName,contact.lastName,number[0]
            ,number[1],contact.email,contact.favorite)
        db?.close()
    }
    fun update(contact: Contact){
        val db= context?.let { DBHelper(it,null) }
        db?.update(contact)
    }
    fun getAlldata(){
        val db= context?.let { DBHelper(it,null) }
        list=db!!.getAllData()
        makeFavResult()
    }
    fun delete(contact: Contact){
        val db= context?.let { DBHelper(it,null) }
        db?.deleteValues( contact )
    }
}