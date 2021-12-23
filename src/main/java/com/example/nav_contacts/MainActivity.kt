package com.example.nav_contacts


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlin.properties.Delegates
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {
    var gridForRecycler:Boolean=false
    var portrait by Delegates.notNull<Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Database(this).getAlldata()

        setContentView(R.layout.activity_main)
        if(savedInstanceState!=null){
            gridForRecycler=savedInstanceState.getBoolean("grid")
        }

        supportFragmentManager.popBackStack()
        portrait = findViewById<LinearLayout>(R.id.activity_main_portrait) != null

        if(savedInstanceState==null)
            supportFragmentManager.beginTransaction()
                .add(R.id.container12, DisplayContactsFragment()).commit()
        onclickButtons()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        findViewById<Button>(R.id.fav).setTextColor(savedInstanceState.getInt("fav"))
        findViewById<Button>(R.id.contacts).setTextColor(savedInstanceState.getInt("con"))
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("grid",gridForRecycler)
        val favColor=findViewById<Button>(R.id.fav).textColors
        val contactColor=findViewById<Button>(R.id.contacts).textColors
        outState.putInt("fav",favColor.defaultColor)
        outState.putInt("con",contactColor.defaultColor)
    }

    private fun onclickButtons(){
        val favorites:Button=findViewById(R.id.fav)
        val contacts:Button=findViewById(R.id.contacts)
        favorites.setOnClickListener {
            gridForRecycler=true
            favorites.setTextColor(getColor(R.color.blue))
            contacts.setTextColor(getColor(R.color.black))
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction().replace(R.id.container12,DisplayContactsFragment()/*FavoritesFragment()*/).commit()
        }
        contacts.setOnClickListener {
            gridForRecycler=false
            DBHelper(context=this,null ).writableDatabase
            favorites.setTextColor(getColor(R.color.black))
            contacts.setTextColor(getColor(R.color.blue))
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction().replace(R.id.container12,DisplayContactsFragment()).commit()
        }
    }

}