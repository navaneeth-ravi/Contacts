package com.example.nav_contacts



import java.io.Serializable
data class ContactDataClass(var firstName: String,var lastName: String,var number:ArrayList<String>,var favorite:Boolean=false,var email: String
 , var dbID:Int?=null, var profileImage: String):Serializable