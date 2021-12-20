package com.example.nav_contacts


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat

class CallPermission() {
    companion object{
        const val CALL_PERMISSION_CODE=1
        fun requestPermissions(context: Context){
            ActivityCompat.requestPermissions(context as ContactDetails,
                arrayOf(Manifest.permission.CALL_PHONE),CALL_PERMISSION_CODE
            );
        }
    }
}