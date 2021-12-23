package com.example.nav_contacts


import android.widget.Toast
import java.io.Serializable

 data class Contact(var favorite:Boolean, val userId:Int=id, var dbID:Int?): Serializable {
     companion object{
         var id=0
     }
    lateinit var firstName:String
    lateinit var lastName:String
    lateinit var number:ArrayList<String>
    lateinit var email:String
     init {
         id++
     }

    constructor(firstName:String,lastName:String,number: ArrayList<String>
                ,email:String,fav:Boolean=false,dbID:Int?=null): this(favorite=fav, dbID = dbID) {
        this.firstName=firstName
        this.lastName=lastName
        this.number=number
        this.email=email
    }
}