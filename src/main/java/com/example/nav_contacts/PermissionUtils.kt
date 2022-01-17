package com.example.nav_contacts


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionUtils() {
    companion object{
        const val CALL_PERMISSION_CODE=1
        const val GALLERY_PERMISSION_CODE=2
        fun requestPermissions(activity: Activity,permissions:Array<String>,permissionCode:Int){
            activity.requestPermissions(permissions,permissionCode)
        }
        fun shouldShowRational(activity: Activity,permission:String):Boolean{
            return activity.shouldShowRequestPermissionRationale(permission)
        }
        fun hasPermission(activity: Activity,permission: String):Boolean{
            return activity.checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED
        }
//        arrayOf(Manifest.permission.CALL_PHONE)
    }
}