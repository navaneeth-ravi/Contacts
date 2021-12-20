package com.example.nav_contacts

import android.util.Log

class Database {
    companion object {
        var list: ArrayList<Contact> = ArrayList()
        var favList:ArrayList<Contact> = ArrayList()

        init {
            val number: ArrayList<String> = ArrayList()
            number.add("8838900839")
            number.add("7539916246")
            list.add(Contact("Navaneethan", "Ravi", number, "abc@gmail.com",true))
            list.add(Contact("vishnu", "N", number, "darls@nav.com",true))
            list.add(Contact("Naani", "N", number, "mail"))
            list.add(Contact("Adhi", "Ravi", number, "mail"))
            list.add(Contact("Ravi", "J", number, "mail"))
            list.add(Contact("Rajakumari", "R", number, "mail"))
            list.add(Contact("Rajini", "Super", number, "mail"))

            for(i in list) {
                Log.i("abc", "getFavorites:for ")
                if (i.favorite) {
                    Log.i("abc", "getFavorites: ")
                    favList.add(i)
                }
            }
        }
        fun refreshFavList(){
            favList=ArrayList()
            for(i in list) {
                Log.i("abc", "getFavorites:for ")
                if (i.favorite) {
                    Log.i("abc", "getFavorites: ")
                    favList.add(i)
                }
            }
        }
    }
}