package com.example.nav_contacts

import android.app.Application
import android.content.ContentResolver
import android.content.res.Resources

class ContactMain:Application(){
    companion object{
        lateinit var resources: Resources private set
        lateinit var contentResolver: ContentResolver private set
    }
    override fun onCreate() {
        super.onCreate()
        ContactMain.contentResolver = contentResolver
        ContactMain.resources = resources
    }
}