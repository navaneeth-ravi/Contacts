package com.example.nav_contacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

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
        onBackArrowClick()
        callMessageButtonListener()
        makeFavorite(star)
        editContactDetails()
        fillDetailsInLayout()
        mailLayout()
        setProfile()
    }
    private fun setProfile(){
        val imageView=findViewById<ImageView>(R.id.user_ic)
        val cardView=findViewById<CardView>(R.id.custom_profile)
        val directory = applicationContext.filesDir
        val imageDirectory = File(directory, "profileImages")
        val imgFile = File(imageDirectory, "${contact.firstName + contact.lastName}.png")
        if(imgFile.exists()) {
            imageView.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
            cardView.visibility=View.VISIBLE
        }
    }
    private fun mailLayout(){
        findViewById<LinearLayout>(R.id.mail_layout).setOnClickListener {
            Toast.makeText(this, "This feature is 'On progress'", Toast.LENGTH_SHORT).show()
        }
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
        if(contact.email!="")
            findViewById<TextView>(R.id.mail).text=contact.email
    }
    private fun callMessageButtonListener(){
        findViewById<ImageView>(R.id.call).setOnClickListener {

            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                        if(contact.number.isNotEmpty()) {
                            phoneNumber=contact.number[0]
                            makeCall()
                        }else
                            Toast.makeText(this, "Number not available for Call", Toast.LENGTH_SHORT).show()
            else
                requestCallPermission()
        }
        findViewById<ImageView>(R.id.message_main).setOnClickListener {
            Toast.makeText(this, "This feature is 'On progress'", Toast.LENGTH_SHORT).show()
        }
    }
    fun requestCallPermission(phoneNumber:String?=null){
        if(phoneNumber!=null)this.phoneNumber=phoneNumber
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CALL_PHONE)){
            AlertDialog.Builder(this).setTitle("Call permissions needed to make call").setMessage("'ok' to allow from settings")
                .setPositiveButton("ok") { _, _ ->
                    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
            }.setNegativeButton("cancel"){ dialogInterface, _ ->
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
    private fun onBackArrowClick(){
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
                contact.dbID?.let { it1 -> changeFavoriteOption(contact.favorite, it1) }
                val db=DBHelper(this,null)
                db.update(contact)
                db.close()
            }else {
                star.setImageResource(R.drawable.fav_star)
                contact.favorite=true
                contact.dbID?.let { it1 -> changeFavoriteOption(contact.favorite, it1) }
                Database.favList.add(contact)
                val db=DBHelper(this,null)
                db.update(contact)
                db.close()
            }
        }
    }
    private fun changeFavoriteOption(option:Boolean,userId:Int){
        for(i in Database.list){
            if(i.dbID==userId){
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
        AlertDialog.Builder(this).setTitle("Are you sure to 'delete'?").setMessage("permanently deleted, can't be retrieved")
            .setPositiveButton("Delete") { _, _ ->
                if(item.itemId==R.id.delete){
                    deleteContact()
                    finish()
                }
            }.setNegativeButton("cancel"){ dialogInterface, _ ->
                dialogInterface.dismiss()
            }.create().show()

        return super.onOptionsItemSelected(item)
    }
    private fun deleteContact(){
        for (i in 0 until Database.list.size) {
            if (Database.list[i].userId == contact.userId) {
                Toast.makeText(this, "Deleted ${contact.firstName}", Toast.LENGTH_SHORT).show()
                Database.list.removeAt(i)
                DBHelper(this,null).deleteValues(contact)
                Database.makeFavResult()
                break
            }
        }
    }
    override fun onResume() {
        super.onResume()
        for(i in Database.list)
            if (i.dbID==contact.dbID){
                contact=i
                break
            }
        val adapter=(recyler.adapter as MyAdapter)
        adapter.number=contact.number
        adapter.notifyDataSetChanged()
        fillDetailsInLayout()
    }
}