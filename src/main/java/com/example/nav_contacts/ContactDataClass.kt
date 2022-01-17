package com.example.nav_contacts


import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
//@Parcelize
data class ContactDataClass(var firstName: String,var lastName: String,var number:ArrayList<String>,var favorite:Boolean=false,var email: String
 , var dbID:Int?=null, var profileImage: String):Serializable{
//    Serializable {
//    constructor(parcel: Parcel) : this(
//        parcel.readString() as String,
//        parcel.readString() as String,
//        arrayListOf<String>().apply { parcel.readArrayList(String::class.java.classLoader)},
//        parcel.readByte() != 0.toByte(),
//        parcel.readString() as String,
//        parcel.readValue(Int::class.java.classLoader) as? Int,
//        parcel.readString() as String
//    ) {
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(firstName)
//        parcel.writeString(lastName)
//        parcel.writeByte(if (favorite) 1 else 0)
//        parcel.writeArray(arrayOf(number))
//        parcel.writeString(email)
//        parcel.writeValue(dbID)
//        parcel.writeString(profileImage)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<ContactDataClass> {
//        override fun createFromParcel(parcel: Parcel): ContactDataClass {
//            return ContactDataClass(parcel)
//        }
//
//        override fun newArray(size: Int): Array<ContactDataClass?> {
//            return arrayOfNulls(size)
//        }
//    }

}