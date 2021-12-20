package com.example.nav_contacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

class ContactDetails : AppCompatActivity(){
    private lateinit var contact: Contact
    private lateinit var recyler: RecyclerView
    private lateinit var phoneNumber:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        contact = intent.getSerializableExtra("abc") as Contact
        val star:ImageView=findViewById(R.id.star)
        if(contact.favorite){
            star.setImageResource(R.drawable.fav_star)
        }
        recyler=findViewById(R.id.recycler_contacts)
        recyler.layoutManager=LinearLayoutManager(this)
        recyler.adapter=MyAdapter(false,contact.number, context = this)
        setToolbar()
        backArrowClick()
        callMessageButtonListener()
        makeFavorite(star)
        editContactDetails()
        fillDetailsInLayout()
    }
    private fun setToolbar(){
        val toolbar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    @SuppressLint("SetTextI18n")
    private fun fillDetailsInLayout(){
        findViewById<Button>(R.id.contact_icon).text="${contact.firstName[0]}"
        findViewById<TextView>(R.id.contact_name).text="${contact.firstName} ${contact.lastName}"
    }
    private fun callMessageButtonListener(){
        findViewById<ImageView>(R.id.call).setOnClickListener {
            phoneNumber=contact.number[0]
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                makeCall()
            else
                requestCallPermission();
        }
        findViewById<ImageView>(R.id.message_main).setOnClickListener {
            Toast.makeText(this, "This feature is 'On progress'", Toast.LENGTH_SHORT).show()
        }
    }
    fun requestCallPermission(phoneNumber:String?=null){
        if(phoneNumber!=null)this.phoneNumber=phoneNumber
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.CALL_PHONE)){

            AlertDialog.Builder(this).setTitle("Call permissions needed to make call").setMessage("'ok' to allow from settings")
                .setPositiveButton("ok") { _, _ ->
                    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
            }.setNegativeButton("cancel"){dialogInterface, i ->
                    dialogInterface.dismiss()
                }.create().show()
        }
        else {
            CallPermission.requestPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==CallPermission.CALL_PERMISSION_CODE)
            if(grantResults.isNotEmpty() &&(grantResults[0]==PackageManager.PERMISSION_GRANTED))
                makeCall()
    }
     private fun makeCall(){
        val phone= "tel:$phoneNumber"
        val intent= Intent(Intent.ACTION_CALL)
        intent.data=Uri.parse(phone)
        startActivity(intent)
    }
    private fun backArrowClick(){
        val backArrow=findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish()
        }
    }
    private fun makeFavorite(star:ImageView){
        star.setOnClickListener {
            if(contact.favorite){
                star.setImageResource(R.drawable.star_border)
                Database.favList.remove(contact)
                contact.favorite=false
                changeFavoriteOption(contact.favorite,contact.userId)
            }else {
                star.setImageResource(R.drawable.fav_star)
                contact.favorite=true
                changeFavoriteOption(contact.favorite,contact.userId)
                Database.favList.add(contact)
            }
        }
    }
    private fun changeFavoriteOption(option:Boolean,userId:Int){
        for(i in Database.list){
            if(i.userId==userId){
                i.favorite=option
            }
        }
    }
    private fun editContactDetails(){
        findViewById<View>(R.id.edit_icon).setOnClickListener {
            val intent=Intent(this,ContactCreation::class.java)
            intent.putExtra("option",ContactCreation.EDIT)
            intent.putExtra("contact",contact)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contact_details_menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.delete){
            for (i in 0 until Database.list.size) {
                if (Database.list[i].userId == contact.userId) {
                    Toast.makeText(this, "Deleted ${contact.firstName}", Toast.LENGTH_SHORT).show()
                    Database.list.removeAt(i)
                    Database.refreshFavList()
                    break
                }
            }
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        for(i in Database.list){
            if (i.userId==contact.userId){
                contact=i
                break
            }
        }
        val adapter=(recyler.adapter as MyAdapter)
        for(i in 0 until contact.number.size){
            adapter?.number?.set(i,contact.number[i])
            recyler.adapter?.notifyItemChanged(i)
        }
    }
}