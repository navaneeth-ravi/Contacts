package com.example.nav_contacts


import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager

object PermissionUtils{
    const val CALL_PERMISSION_CODE=1
    const val GALLERY_PERMISSION_CODE=2
    const val CONTACTS_PERMISSION_CODE=3
    fun requestPermissions(activity: Activity,permissions:Array<String>,permissionCode:Int){
        activity.requestPermissions(permissions,permissionCode)
    }
    fun shouldShowRational(activity: Activity,permission:String):Boolean{
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    fun hasPermission(activity: Activity,permission: String):Boolean{
        return activity.checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED
    }
}