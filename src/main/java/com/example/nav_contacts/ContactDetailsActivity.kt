package com.example.nav_contacts

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_contact_details.*
import java.io.File
import java.io.IOException

class ContactDetailsActivity : AppCompatActivity(){
    companion object {
        const val CONTACT_KEY = "contactDetail"
        const val POSITION_IN_ADAPTER = "position"
    }
    private lateinit var contact: ContactDataClass
    private lateinit var recyler: RecyclerView
    private lateinit var phoneNumber:String
    private var positionOfContactInAdapter:Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        contact = intent.getSerializableExtra(CONTACT_KEY) as ContactDataClass
        positionOfContactInAdapter=intent.getIntExtra(POSITION_IN_ADAPTER,-1)
        if (contact.favorite) {
            val starIcon:ImageView = findViewById(R.id.star)
            starIcon.setImageResource(R.drawable.fav_star)
        }
        recyler = findViewById(R.id.recycler_contacts)
        recyler.layoutManager = LinearLayoutManager(this)
        recyler.adapter = ContactAndFavoriteAdapter(false,contact.number, context = this)
        setToolbarAsActionBar()
        fillContactDetailsInLayout()
        setProfilePictureIfAvailable()
        onClickBackArrow()
        onClickCallIcon()
        onClickStarIcon(star)
        onClickEditIcon()
        onClickMailAndMessageIcon()
    }

    private fun setProfilePictureIfAvailable(){
        val imageView = findViewById<ImageView>(R.id.user_ic)
        val cardView = findViewById<CardView>(R.id.custom_profile)
        try {
            val directory = applicationContext.filesDir
            val imageDirectory = File(directory, resources.getString(R.string.image_directory_name))
            val imgFile = File(imageDirectory, "${contact.firstName + contact.lastName}${resources.getString(R.string.image_format)}")
            if (imgFile.exists()) {
                imageView.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
                cardView.visibility = View.VISIBLE
            }
        } catch (e: IOException){
            cardView.visibility = View.GONE
        }
    }

    private fun setToolbarAsActionBar() {
        val toolbar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun fillContactDetailsInLayout() {
        findViewById<Button>(R.id.contact_icon).text="${contact.firstName[0]}"
        val contactName="${contact.firstName} ${contact.lastName}"
        findViewById<TextView>(R.id.contact_name).text=contactName
        val mailField=findViewById<TextView>(R.id.mail)
        if (contact.email!="") {
            mailField.visibility=View.VISIBLE
            mailField.text = contact.email
        } else {
            mailField.visibility=View.GONE
        }
    }
    private fun onClickMailAndMessageIcon() {
        findViewById<LinearLayout>(R.id.mail_layout).setOnClickListener {
            Toast.makeText(this, resources.getString(R.string.on_progress_feature), Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageView>(R.id.message_main).setOnClickListener {
            Toast.makeText(this, resources.getString(R.string.on_progress_feature), Toast.LENGTH_SHORT).show()
        }
    }
    private fun onClickCallIcon() {
        findViewById<ImageView>(R.id.call).setOnClickListener {
            if (PermissionUtils.hasPermission(this,Manifest.permission.CALL_PHONE)) {
                if (contact.number.isNotEmpty()) {
                    phoneNumber = contact.number[0]
                    makePhoneCall()
                } else {
                    Toast.makeText(this, resources.getString(R.string.number_not_available), Toast.LENGTH_SHORT).show()
                }
            }
            else {
                requestCallPermission()
            }
        }
    }
    private fun onClickBackArrow(){
        val backArrow=findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            // activity closed
            finish()
        }
    }
    private fun onClickEditIcon(){
        findViewById<View>(R.id.edit_icon).setOnClickListener {
            val intent=Intent(this,CreateAndEditContactActivity::class.java)
            intent.putExtra(CreateAndEditContactActivity.OPTION,CreateAndEditContactActivity.EDIT)
            intent.putExtra(CreateAndEditContactActivity.EDIT_CONTACT_KEY,contact)
            intent.putExtra(POSITION_IN_ADAPTER,positionOfContactInAdapter)
            startCreateAndEditContactActivityForEditContact.launch(intent)
        }
    }
    private fun onClickStarIcon(star:ImageView) {
        star.setOnClickListener {
            if (contact.favorite) {
                removeContactFromFavorite(star)
            } else {
                addContactToFavorite(star)
            }
            val values=ContentValues()
            values.put(MyContentProvider.FAVORITE,contact.favorite)
            DatabaseFunctionalities().update(values,contact,contentResolver)
        }
    }
    fun requestCallPermission(phoneNumber:String?=null) {
        if (phoneNumber!=null){
            this.phoneNumber = phoneNumber
        }
        if (PermissionUtils.shouldShowRational(this,Manifest.permission.CALL_PHONE)) {
            AlertDialog.Builder(this).setTitle(resources.getString(R.string.call_permission)).setMessage(resources.getString(R.string.allow_permission))
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts(resources.getString(R.string.uri_package),packageName,null)
                    intent.data=uri
                    startActivity(intent)
            }.setNegativeButton(resources.getString(R.string.cancel)){ dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.create().show()
        }
        else {
            PermissionUtils.requestPermissions(this,arrayOf(Manifest.permission.CALL_PHONE),PermissionUtils.CALL_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ( requestCode==PermissionUtils.CALL_PERMISSION_CODE && grantResults.isNotEmpty()
            && (grantResults[0]==PackageManager.PERMISSION_GRANTED) ) {
            makePhoneCall()
        }
    }
     private fun makePhoneCall() {
        val phone= "tel:$phoneNumber"
        val intent= Intent(Intent.ACTION_CALL)
        intent.data=Uri.parse(phone)
        startActivity(intent)
    }

    private fun removeContactFromFavorite(star:ImageView) {
        star.setImageResource(R.drawable.star_border)
        MainActivity.favoriteContactList.remove(contact)
        contact.favorite=false
        contact.dbID?.let { it1 -> changeFavoriteOption(contact.favorite, it1) }
    }
    private fun addContactToFavorite(star:ImageView) {
        star.setImageResource(R.drawable.fav_star)
        contact.favorite=true
        contact.dbID?.let { it1 -> changeFavoriteOption(contact.favorite, it1) }
        MainActivity.favoriteContactList.add(contact)
    }

    private fun changeFavoriteOption(option:Boolean,userId:Int) {
        for (i in MainActivity.contactList) {
            if (i.dbID==userId) {
                i.favorite=option
            }
        }
    }

    private val startCreateAndEditContactActivityForEditContact =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK) {
            contact = it.data?.getSerializableExtra(CreateAndEditContactActivity.EDIT_CONTACT_KEY) as ContactDataClass
            val adapter = recyler.adapter as ContactAndFavoriteAdapter
            try {
                for (i in 0 until contact.number.size) {
                    if (contact.number[i] == "" || contact.number[i] == resources.getString(R.string.empty)) {
                        contact.number.removeAt(i)
                    }
                }
            } catch (e:Exception) {
                Log.w("", "${e.printStackTrace()}", )
            } finally {
                adapter.number = contact.number
                adapter.notifyDataSetChanged()
                fillContactDetailsInLayout()
            }
        }
    }
    private fun deleteContact() {
        Toast.makeText(this, "${resources.getString(R.string.deleted)} ${contact.firstName}", Toast.LENGTH_SHORT).show()
        MainActivity.contactList.remove(contact)
        DatabaseFunctionalities().delete(contact.dbID.toString(),contentResolver)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contact_details_menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AlertDialog.Builder(this).setTitle(resources.getString(R.string.sure_delete)).setMessage(resources.getString(R.string.permanent_delete))
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                if (item.itemId==R.id.delete) {
                    deleteContact()
                    finish()
                }
            }.setNegativeButton(resources.getString(R.string.cancel)){ dialogInterface, _ ->
                dialogInterface.dismiss()
            }.create().show()

        return super.onOptionsItemSelected(item)
    }
}