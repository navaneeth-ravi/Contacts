package com.example.nav_contacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import java.io.IOException
import java.lang.Exception
import java.util.regex.Matcher
import java.util.regex.Pattern


class CreateAndEditContactActivity : AppCompatActivity() {
    private var imageUri : Uri?=null
    companion object{
        const val EDIT=0
        const val CREATE=1
        const val PERMISSION_CODE=100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_and_edit_contact)
        val option=intent.getIntExtra("option",1)
        if(savedInstanceState!=null){
            imageUri=savedInstanceState.getString("image")?.toUri()
            setUserProfileImage()
        }
        if(option== EDIT) {
            fillExistingDetailsToEdit()
        }
        onClickSave(option)
        onClickCloseButton()
        onClickAddImageIcon()

    }

    private fun onClickAddImageIcon(){
        findViewById<Button>(R.id.add_photo).setOnClickListener {
            if(!PermissionUtils.hasPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                if(PermissionUtils.shouldShowRational(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    AlertDialog.Builder(this).setTitle("Call permissions needed to make call").setMessage("'ok' to allow from settings")
                        .setPositiveButton("ok") { _, _ ->
                            val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri= Uri.fromParts("package",packageName,null)
                            intent.data=uri
                            startActivity(intent)
                        }.setNegativeButton("cancel"){ dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }.create().show()
                }else {
                    PermissionUtils.requestPermissions(
                        this,
                        permissions,
                        PermissionUtils.GALLERY_PERMISSION_CODE
                    )
                }
            } else{
                chooseImageFromGallery();
            }
        }
    }

    private val activityForGettingImageFromGallery=registerForActivityResult(
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

    private fun saveProfilePicture(bitmap:Bitmap?,contact: ContactDataClass){
        val directory=applicationContext.filesDir
        val imageDirectory=File(directory,"profileImages")
        if(!imageDirectory.exists()){
            imageDirectory.mkdir()
        }
        try {
            if (imageUri != null) {
                val imgFile = File(imageDirectory, "${contact.firstName + contact.lastName}.png")
                val stream = FileOutputStream(imgFile)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 10, stream)
                stream.flush()
                stream.close()
            }
        }catch (e: IOException){}
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(imageUri!=null){
            outState.putString("image",imageUri.toString())
        }
    }
    private fun chooseImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activityForGettingImageFromGallery.launch(intent)
    }
    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PermissionUtils.GALLERY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    chooseImageFromGallery()
                }else{
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun validateMobileNumber(phone:String):Boolean{
        val pattern:Pattern= Pattern.compile("[6-9][0-9]{9}")
        val matcher:Matcher=pattern.matcher(phone)
        return matcher.matches()
//        String regexStr = "^[0-9]$";
//
//        String number=entered_number.getText().toString();
//
//        if(entered_number.getText().toString().length()<10 || number.length()>13 || number.matches(regexStr)==false  ) {
//            Toast.makeText(MyDialog.this,"Please enter "+"\n"+" valid phone number",Toast.LENGTH_SHORT).show();
//            // am_checked=0;
//        }`
//        return true
    }
    private fun validEmail(email: String):Boolean{
        val pattern:Pattern= Pattern.compile("^\\S+@\\S+\\.\\S+\$")
        val matcher:Matcher=pattern.matcher(email)
        return matcher.matches()
    }
    private fun getContactFromInputFieldsToSave():ContactDataClass?{
        val firstName:String=findViewById<EditText>(R.id.first_name).text.toString().trim()
        val lasName:String=findViewById<EditText>(R.id.last_name).text.toString().trim()
        val number=ArrayList<String>()
        val number1:String=findViewById<EditText>(R.id.number1).text.toString().trim()
        if(number1.length>0)
            if(!validateMobileNumber(number1)) {
                Toast.makeText(this, "Invalid Number1", Toast.LENGTH_SHORT).show()
                return null
            }
        val number2=findViewById<EditText>(R.id.number2).text.toString().trim()
        if(number2.length>0)
            if(!validateMobileNumber(number2)) {
                Toast.makeText(this, "Invalid Number2", Toast.LENGTH_SHORT).show()
                return null
            }
        val email:String=findViewById<EditText>(R.id.email).text.toString().trim()
        if(!validEmail(email)){
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
            return null
        }
        val bitmap=(findViewById<ImageView>(R.id.user_ic).drawable as BitmapDrawable).bitmap
        val stream=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream)
        if(number1=="")
            number.add("empty")else number.add(number1)
        if(number2=="")
            number.add("empty")else number.add(number2)
        return ContactDataClass(firstName = firstName, lastName = lasName,number=number,email=email, profileImage = firstName+lasName+".png")
    }

    private fun onSaveNewContact(){
        val detailContact=getContactFromInputFieldsToSave()
        if(detailContact!=null) {
            if (detailContact.firstName != "" || detailContact.lastName != "") {
                if (detailContact.firstName == "") {
                    detailContact.firstName = detailContact.lastName
                    detailContact.lastName = ""
                }
                GlobalScope.launch {
                    addNewContactToDatabase(detailContact)
                    saveProfilePicture(convertUriToBitmap(), detailContact)
                }
                Database.list.add(detailContact)
                finish()
            } else {
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onSaveEditContact(){
        try {
            val detailContact = getContactFromInputFieldsToSave()
            if(detailContact!=null) {
                var contact = intent.getSerializableExtra("contact") as ContactDataClass
                detailContact.dbID = contact.dbID
                detailContact.favorite = contact.favorite
                GlobalScope.launch {
                    Database.update(detailContact)
                }
                saveProfilePicture(convertUriToBitmap(), detailContact)
                Database.getAlldata()
            }
        }catch (e:Exception){
            Log.i("", "onSaveEditContact: ${e.printStackTrace()}")
        }
        finish()
    }

    private fun onClickSave(option:Int){
        findViewById<Button>( R.id.save ).setOnClickListener {
            if (option == CREATE)
                onSaveNewContact()
            else
                onSaveEditContact()
        }
    }

    private fun onClickCloseButton(){
        findViewById<ImageView>(R.id.close).setOnClickListener {
            finish()
        }
    }

    private fun fillExistingDetailsToEdit(){
        val profile:ImageView=findViewById(R.id.user_ic)
        val firstName=findViewById<EditText>(R.id.first_name)
        val lastName=findViewById<EditText>(R.id.last_name)
        val number1 = findViewById<EditText>(R.id.number1)
        val number2 = findViewById<EditText>(R.id.number2)
        val email=findViewById<EditText>(R.id.email)
        val contact=intent.getSerializableExtra("contact") as ContactDataClass
        val title="Edit contact"
        findViewById<TextView>(R.id.header_title).text=title
        firstName.setText(contact.firstName)
        lastName.setText(contact.lastName)

        if(contact.number.isNotEmpty()) {
            if(contact.number[0]!="empty")
                number1.setText(contact.number[0])
            if(contact.number.size==2)
                number2.setText(contact.number[1])
        }
        email.setText(contact.email)
        try {
            val directory = applicationContext.filesDir
            val imageDirectory = File(directory, "profileImages")
            val imgFile =
                File(imageDirectory, "${firstName.text.toString() + lastName.text.toString()}.png")
            if (imgFile.exists()) {
                profile.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
                findViewById<CardView>(R.id.custom_profile).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.default_user_ic).visibility = View.GONE
            }
        }catch (e:IOException){}
    }

    private suspend fun addNewContactToDatabase(detailContact: ContactDataClass){
        Database.addContactToDatabaseTable(detailContact.firstName, detailContact.lastName, detailContact.number[0],
            detailContact.number[1], detailContact.email, false)
        Database.getAlldata()
    }
    suspend fun addd(detailContact: ContactDataClass){

    }
    fun generateThumb(bitmap: Bitmap,THUMB_SIZE:Int):Bitmap{
        val ratioSquare: Double
        val bitmapHeight: Int
        val bitmapWidth: Int
        bitmapHeight = bitmap.height
        bitmapWidth = bitmap.width
        ratioSquare = (bitmapHeight * bitmapWidth / THUMB_SIZE).toDouble()
        if (ratioSquare <= 1) return bitmap
        val ratio = Math.sqrt(ratioSquare)
        Log.d("mylog", "Ratio: $ratio")
        val requiredHeight = Math.round(bitmapHeight / ratio).toInt()
        val requiredWidth = Math.round(bitmapWidth / ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, requiredWidth, requiredHeight, true)
    }
}