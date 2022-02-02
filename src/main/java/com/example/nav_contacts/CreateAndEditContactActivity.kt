package com.example.nav_contacts

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern


class CreateAndEditContactActivity : AppCompatActivity() {
    private var imageUri : Uri?=null
    private lateinit var contact:ContactDataClass
    companion object{
        const val EDIT=0
        const val CREATE=1
        const val OPTION="option"
        const val EDIT_CONTACT_KEY="editContact"
        const val IMAGE_KEY="image"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_and_edit_contact)
        if(savedInstanceState!=null){
            imageUri=savedInstanceState.getString(IMAGE_KEY)?.toUri()
            setUserProfileImage()
        }
        if(intent.getIntExtra(OPTION,1)== EDIT) {
            getContactData()
        }
        onClickListeners()
        val number1EditField=findViewById<EditText>(R.id.number1)
        val number2EditField=findViewById<EditText>(R.id.number2)
        val emailEditField=findViewById<EditText>(R.id.email)
        inputsValidationForMobileNumberAndEmail(number1EditField)
        inputsValidationForMobileNumberAndEmail(number2EditField)
        inputsValidationForMobileNumberAndEmail(emailEditField,true)
    }


    private val activityForGettingImageFromGallery=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode== RESULT_OK) {
            it.data?.data?.let { it1 ->
                imageUri=it1
                setUserProfileImage()
            }
        }
    }
    private fun setUserProfileImage(){
        val image: ImageView =findViewById(R.id.user_ic)
        val bitmap=convertUriToBitmap()
        if (bitmap!=null) {
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
        val imageDirectory=File(directory, resources.getString(R.string.image_directory_name))
        if (!imageDirectory.exists()) {
            imageDirectory.mkdir()
        }
        try {
            if (imageUri != null) {
                val imgFile = File(imageDirectory, contact.firstName + contact.lastName + resources.getString(R.string.image_format))
                val stream = FileOutputStream(imgFile)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 10, stream)
                stream.flush()
                stream.close()
            }
        } catch (e: IOException){
            Log.e("", "${e.printStackTrace()} " )
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (imageUri!=null) {
            outState.putString(IMAGE_KEY,imageUri.toString())
        }
    }
    private fun getContactData(){
        val dbID=intent.getIntExtra(EDIT_CONTACT_KEY,-1)
        GlobalScope.launch(Dispatchers.IO) {
            if(dbID!=-1) {
                val cursor:Cursor?=DatabaseFunctionalities.getContact(dbID.toString())
                withContext(Dispatchers.Main){
                    cursor?.moveToFirst()
                    val data=ContactDataClass.getContact(cursor)
                    if(data!=null) {
                        contact=data
                        fillExistingValuesToEdit()
                    }
                }
            }
        }
    }
    private fun fillExistingValuesToEdit(){
        val profile:ImageView=findViewById(R.id.user_ic)
        val firstName=findViewById<EditText>(R.id.first_name)
        val lastName=findViewById<EditText>(R.id.last_name)
        val number1 = findViewById<EditText>(R.id.number1)
        val number2 = findViewById<EditText>(R.id.number2)
        val email=findViewById<EditText>(R.id.email)
        findViewById<TextView>(R.id.header_title).text=resources.getString(R.string.title_edit_contact_activity)
        firstName.setText(contact.firstName)
        lastName.setText(contact.lastName)

        if (contact.number.isNotEmpty()) {
            if (contact.number[0]!=resources.getString(R.string.empty)) {
                number1.setText(contact.number[0])
            }
            if (contact.number.size==2) {
                number2.setText(contact.number[1])
            }
        }
        email.setText(contact.email)
        try {
            val directory = applicationContext.filesDir
            val imageDirectory = File(directory, resources.getString(R.string.image_directory_name))
            val imgFile =
                File(imageDirectory, firstName.text.toString() + lastName.text.toString()+resources.getString(R.string.image_format))
            if (imgFile.exists()) {
                profile.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
                findViewById<CardView>(R.id.custom_profile).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.default_user_ic).visibility = View.GONE
            }
        } catch (e:IOException){
            Log.e("", "${e.printStackTrace()} ")
            Toast.makeText(this, resources.getString(R.string.file_missing), Toast.LENGTH_SHORT).show()
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
        when (requestCode) {
            PermissionUtils.GALLERY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    chooseImageFromGallery()
                } else {
                    Toast.makeText(this,resources.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun inputsValidationForMobileNumberAndEmail(editTextMobileNumber: EditText,emailValidate:Boolean=false){
        editTextMobileNumber.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (emailValidate){
                    if (p0.toString().isNotEmpty() &&!validateEmail(p0.toString())) {
                        editTextMobileNumber.error = resources.getString(R.string.invalid_email)
                    }
                } else {
                    if (p0.toString().isNotEmpty() &&!validateMobileNumber(p0.toString())) {
                        editTextMobileNumber.error = resources.getString(R.string.invalid_number)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) { }
        })
    }
    private fun validateMobileNumber(phone:String):Boolean{
        val pattern:Pattern= Pattern.compile("[6-9][0-9]{9}")
        val matcher:Matcher=pattern.matcher(phone)
        return matcher.matches()
    }
    private fun validateEmail(email: String):Boolean{
        val pattern:Pattern= Pattern.compile("^\\S+@\\S+\\.\\S+\$")
        val matcher:Matcher=pattern.matcher(email)
        return matcher.matches()
    }
    private fun getContactFromInputFieldsToSave():ContactDataClass?{
        val firstName:String=findViewById<EditText>(R.id.first_name).text.toString().trim()
        val lasName:String=findViewById<EditText>(R.id.last_name).text.toString().trim()
        val number=ArrayList<String>()
        val number1:String=findViewById<EditText>(R.id.number1).text.toString().trim()
        if (number1.isNotEmpty()) {
            if (!validateMobileNumber(number1)) {
                Toast.makeText(this, resources.getString(R.string.invalid_number1), Toast.LENGTH_SHORT).show()
                return null
            }
        }
        val number2=findViewById<EditText>(R.id.number2).text.toString().trim()
        if (number2.isNotEmpty()) {
            if (!validateMobileNumber(number2)) {
                Toast.makeText(this,  resources.getString(R.string.invalid_number2), Toast.LENGTH_SHORT).show()
                return null
            }
        }
        val email:String=findViewById<EditText>(R.id.email).text.toString().trim()
        if (email.isNotEmpty()) {
            if (!validateEmail(email)) {
                Toast.makeText(this,  resources.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
                return null
            }
        }
        val bitmap=(findViewById<ImageView>(R.id.user_ic).drawable as BitmapDrawable).bitmap
        val stream=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream)
        if(number1=="") {
            number.add(resources.getString(R.string.empty))
        } else {
            number.add(number1)
        }
        if(number2=="") {
            number.add(resources.getString(R.string.empty))
        } else {
            number.add(number2)
        }
        return ContactDataClass(firstName = firstName, lastName = lasName,number=number,email=email, profileImage = firstName+lasName+resources.getString(R.string.image_format))
    }

    private fun addNewContact(){
        val detailContact=getContactFromInputFieldsToSave()
        if (detailContact!=null) {
            if (detailContact.firstName != "" || detailContact.lastName != "") {
                if (detailContact.firstName == "") {
                    detailContact.firstName = detailContact.lastName
                    detailContact.lastName = ""
                }
                val values=ContactDataClass.getContentValuesForContact(detailContact)
                DatabaseFunctionalities.insert(values)
                saveProfilePicture(convertUriToBitmap(), detailContact)
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, resources.getString(R.string.invalid), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveEditedContact(){
        try {
            val detailContact = getContactFromInputFieldsToSave()
            if(detailContact!=null) {
                if (detailContact.firstName != "" || detailContact.lastName != "") {
                    if (detailContact.firstName == "") {
                        detailContact.firstName = detailContact.lastName
                        detailContact.lastName = ""
                    }
                    detailContact.dbID = contact.dbID
                    detailContact.favorite = contact.favorite
                    val values=ContactDataClass.getContentValuesForContact(detailContact)
                    DatabaseFunctionalities.update(values, detailContact.dbID)
                    saveProfilePicture(convertUriToBitmap(), detailContact)
                    setResult(RESULT_OK,intent)
                    finish()
                } else {
                    Toast.makeText(this, resources.getString(R.string.invalid), Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e:Exception) {
            Log.i("", "${e.printStackTrace()}")
            Toast.makeText(this, resources.getString(R.string.invalid), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onClickSave(){
        val option=intent.getIntExtra(OPTION,1)
        findViewById<Button>( R.id.save ).setOnClickListener {
            if (option == CREATE) {
                addNewContact()
            }
            else {
                saveEditedContact()
            }
        }
    }
    private fun onClickListeners(){
        onClickAddImageIcon()
        onClickCloseButton()
        onClickSave()
    }
    private fun onClickCloseButton(){
        findViewById<ImageView>(R.id.close).setOnClickListener {
            //close activity
            finish()
        }
    }
    private fun getProfilePicture(){
        if (!PermissionUtils.hasPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (PermissionUtils.shouldShowRational(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this).setTitle(resources.getString(R.string.call_permission)).setMessage(resources.getString(R.string.allow_permission))
                    .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                        // open settings
                        val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri= Uri.fromParts(resources.getString(R.string.uri_package),packageName,null)
                        intent.data=uri
                        startActivity(intent)
                    }.setNegativeButton(resources.getString(R.string.cancel)){ dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }.create().show()
            } else {
                PermissionUtils.requestPermissions(
                    this,
                    permissions,
                    PermissionUtils.GALLERY_PERMISSION_CODE
                )
            }
        } else {
            chooseImageFromGallery()
        }
    }
    private fun onClickAddImageIcon(){
        findViewById<Button>(R.id.add_photo).setOnClickListener {
            getProfilePicture()
        }
    }

}