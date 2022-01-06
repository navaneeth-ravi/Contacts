package com.example.nav_contacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class ContactCreation : AppCompatActivity() {
    private var imageUri : Uri?=null
    companion object{
        const val EDIT=0
        const val CREATE=1
        const val PERMISSION_CODE=100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_creation)
        val option=intent.getIntExtra("option",1)
        if(savedInstanceState!=null){
            imageUri=savedInstanceState.getString("image")?.toUri()
            setUserProfileImage()
        }
        if(option== EDIT)
            editContact()
        onClickSave(option)
        onClickCloseButton()
        addPhoto()
    }
    private fun addPhoto(){
        findViewById<FloatingActionButton>(R.id.photo).setOnClickListener {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            } else{
                chooseImageGallery();
            }
        }
    }
    private val getImage=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode== Activity.RESULT_OK){
            it.data?.data?.let { it1 ->
                imageUri=it1
                setUserProfileImage()
            }
        }
    }
    private fun setUserProfileImage(){

        val image: ImageView =findViewById(R.id.user_ic)
        val bitmap=convertUriToBitmap()
        if(bitmap!=null){
            findViewById<CardView>(R.id.custom_profile).visibility= View.VISIBLE
            findViewById<ImageView>(R.id.default_user_ic).visibility=View.GONE
            image.setImageBitmap(bitmap)
        }

    }
    private fun convertUriToBitmap():Bitmap?{
        val source=imageUri?.let { it -> ImageDecoder.createSource(this.contentResolver, it) }
        return source?.let { it -> ImageDecoder.decodeBitmap(it) }
    }
    private fun saveImage(bitmap:Bitmap?,contact: Contact){
        val directory=applicationContext.filesDir
        val imageDirectory=File(directory,"profileImages")
        if(!imageDirectory.exists()){
            imageDirectory.mkdir()
        }
        if(imageUri!=null) {
            val imgFile = File(imageDirectory, "${contact.firstName + contact.lastName}.png")
            val stream = FileOutputStream(imgFile)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 10, stream)
            stream.flush()
            stream.close()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(imageUri!=null){
            outState.putString("image",imageUri.toString())
        }
    }
    private fun chooseImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getImage.launch(intent)
    }
    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    chooseImageGallery()
                }else{
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun getvalues():Contact{
        val firstName:String=findViewById<EditText>(R.id.first_name).text.toString().trim()
        val lasName:String=findViewById<EditText>(R.id.last_name).text.toString().trim()
        val number=ArrayList<String>()
        val number1=findViewById<EditText>(R.id.number1).text.toString().trim()
        val number2=findViewById<EditText>(R.id.number2).text.toString().trim()
        val email:String=findViewById<EditText>(R.id.email).text.toString().trim()
        val bitmap=(findViewById<ImageView>(R.id.user_ic).drawable as BitmapDrawable).bitmap
        val stream=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream)
        val image:ByteArray=stream.toByteArray()
        if(number1=="")
            number.add("empty")else number.add(number1)
        if(number2=="")
            number.add("empty")else number.add(number2)
        return Contact(firstName,lasName,number,email, profile = firstName+lasName+".png")
    }

    private fun onCreateClick(){
        findViewById<Button>(R.id.save).setOnClickListener {
            val detailContact=getvalues()
            if(detailContact.firstName != "" || detailContact.lastName != "") {
                if(detailContact.firstName==""){
                    detailContact.firstName=detailContact.lastName
                    detailContact.lastName=""
                }
                val db = DBHelper(this, null)
                db.addToDatabase(detailContact.firstName, detailContact.lastName, detailContact.number[0],
                    detailContact.number[1], detailContact.email, false)
                saveImage(convertUriToBitmap(),detailContact)
                db.close()
                Database.list.add(detailContact)
                Database(this).getAlldata()
            }else{
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
    private fun onEditClick(){
        findViewById<Button>( R.id.save ).setOnClickListener {
            val detailContact=getvalues()
            val contact=intent.getSerializableExtra("contact") as Contact
            val db=DBHelper(this,null)
            detailContact.dbID=contact.dbID
            detailContact.favorite=contact.favorite
            db.update(detailContact)
            saveImage(convertUriToBitmap(),detailContact)
            Database(this).getAlldata()
            finish()
        }
    }
    private fun onClickSave(option:Int){
        if(option== CREATE)
            onCreateClick()
        else
            onEditClick()
    }
    private fun onClickCloseButton(){
        findViewById<ImageView>(R.id.close).setOnClickListener {
            finish()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun editContact(){
        val profile:ImageView=findViewById(R.id.user_ic)
        val firstName=findViewById<EditText>(R.id.first_name)
        val lastName=findViewById<EditText>(R.id.last_name)
        val number1 = findViewById<EditText>(R.id.number1)
        val number2 = findViewById<EditText>(R.id.number2)
        val email=findViewById<EditText>(R.id.email)
        val contact=intent.getSerializableExtra("contact") as Contact

        findViewById<TextView>(R.id.header_title).text="Edit contact"
        firstName.setText(contact.firstName)
        lastName.setText(contact.lastName)

        if(contact.number.isNotEmpty()) {
            if(contact.number[0]!="empty")
                number1.setText(contact.number[0])
            if(contact.number.size==2)
                number2.setText(contact.number[1])
        }
        email.setText(contact.email)

        val directory = applicationContext.filesDir
        val imageDirectory = File(directory, "profileImages")
        if(imageDirectory.exists()) {
            val imgFile = File(imageDirectory, "${firstName.text.toString() + lastName.text.toString()}.png")
            profile.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
            findViewById<CardView>(R.id.custom_profile).visibility= View.VISIBLE
            findViewById<ImageView>(R.id.default_user_ic).visibility=View.GONE
        }
    }
}


//private fun saveContactWhileClick(option:Int){
//    if(option== ContactCreation.CREATE)
//        findViewById<Button>(R.id.save).setOnClickListener {
//            val detailContact=getvalues()
//            if(detailContact.firstName != "" || detailContact.lastName != "") {
//                if(detailContact.firstName==""){
//                    detailContact.firstName=detailContact.lastName
//                    detailContact.lastName=""
//                }
//                val db = DBHelper(this, null)
//                db.addToDatabase(detailContact.firstName, detailContact.lastName, detailContact.number[0], detailContact.number[1], detailContact.email, false)
//                db.close()
//                var deleteCount=0
//                try {
////                    if (number.isNotEmpty())
////                        for (j in 0 until number.size)
////                            if (number[j] == "empty") {
////                                Toast.makeText(this, "$j", Toast.LENGTH_SHORT).show()
////                                number.removeAt(j - deleteCount)
////                                deleteCount += 1
////                            }
//                }catch (e:Exception){}
//                finally {
//                    Database.list.add(detailContact)
//                    Database(this).getAlldata()
//                }
//
////                Toast.makeText(this, "$firstName $lasName  added", Toast.LENGTH_SHORT).show()
//            }else{
//                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
//            }
//            finish()
//        }
//    else{
//        findViewById<Button>(R.id.save).setOnClickListener {
////            number.add(number1)
//            val detailContact=getvalues()
//            val contact=intent.getSerializableExtra("contact") as Contact
//            val db=DBHelper(this,null)
//            detailContact.dbID=contact.dbID
//            detailContact.favorite=contact.favorite
//            db.update(detailContact)
//            Database(this).getAlldata()
////                for(i in Database.list){
////                    if(i.dbID==contact.dbID){
////                        i.firstName=firstName
////                        i.lastName=lasName
////                        i.number=number
////                        i.email=email
////                        val db=DBHelper(this,null)
////                        db.update(i)
////                        Toast.makeText(this, "edited", Toast.LENGTH_SHORT).show()
////                        db.close()
////                        try {
////                            for (j in 0 until number.size)
////                                if (i.number[j] == "empty") {
////                                    i.number.removeAt(j)
////                                }
////                        }catch (e:Exception){
////
////                        }
////                    }
////                }
//            finish()
//        }
//    }
//}