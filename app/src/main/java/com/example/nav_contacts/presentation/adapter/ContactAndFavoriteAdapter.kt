package com.example.nav_contacts.presentation.adapter

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.nav_contacts.*
import com.example.nav_contacts.domain.entity.ContactDataClass
import com.example.nav_contacts.presentation.activity.ContactDetailsActivity
import com.example.nav_contacts.presentation.activity.MainActivity
import com.example.nav_contacts.presentation.fragment.ContactsDisplayFragment
import java.io.File
import java.io.IOException
import kotlin.random.Random


class ContactAndFavoriteAdapter(private val gridForRecycler:Boolean, var context:Context?=null):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var values= ArrayList<ContactDataClass>()
    private val resources:Resources= ContactMain.resources
    companion object {
        private val IMAGE_DIRECTORY_NAME = ContactMain.resources.getString(R.string.image_directory_name)
        private val IMAGE_FORMAT = ContactMain.resources.getString(R.string.image_format)
    }
    inner class ViewHolderForContact(itemView: View):RecyclerView.ViewHolder(itemView) {
        val contactIconButton:Button = itemView.findViewById(R.id.contact_icon)
        val name:TextView = itemView.findViewById(R.id.contact_name)
        val card:RelativeLayout = itemView.findViewById(R.id.card)
        val defaultUserIcon:Button = itemView.findViewById(R.id.contact_icon)
        val profileImage:ImageView = itemView.findViewById(R.id.profile_image)
        val profilePictureCard:CardView = itemView.findViewById(R.id.contact_icon1)
    }
    inner class ViewHolderForFavorite(itemView: View): RecyclerView.ViewHolder(itemView) {
        val letter:TextView = itemView.findViewById(R.id.letter)
        val contactName:TextView = itemView.findViewById(R.id.contact)
        val card:CardView = itemView.findViewById(R.id.fav_card)
        val editIcon:ImageView = itemView.findViewById(R.id.edit_fav)
        val profilePictureLayout:CardView = itemView.findViewById(R.id.user_image)
        val profilePicture:ImageView = itemView.findViewById(R.id.user_ic)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType== ContactsDisplayFragment.CONTACTS){
                val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
                ViewHolderForContact(view)
            }
            else{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.favorites_item, parent, false)
                ViewHolderForFavorite(view)
            }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        if(getItemViewType(position)== ContactsDisplayFragment.CONTACTS){
            holder as ViewHolderForContact
            onBindViewHolderContact(holder,position)
        }
        else{
            holder as ViewHolderForFavorite
            onBindViewHolderFavorite(holder,position)
        }
    private fun onBindViewHolderContact(holder: ViewHolderForContact, position: Int){
        holder.contactIconButton.text = values[position].firstName[0].toString()
        val sortLastContactName = values[position].lastName + " " + values[position].firstName
        val sortFirstContactName = values[position].firstName + " " + values[position].lastName
        holder.name.text = sortFirstContactName

        if (!ContactsDisplayFragment.SORT_BY_FIRST_NAME) {
            holder.name.text = sortLastContactName
            if(values[position].lastName.isNotEmpty()) {
                holder.contactIconButton.text = values[position].lastName[0].toString()
            }
        }
        holder.card.setOnClickListener {
            viewContactDetails(it, position)
        }
        holder.contactIconButton.setOnClickListener { viewContactDetails(it, position) }
        val fileName = values[position % values.size].firstName + values[position % values.size].lastName
        try {
            val directory = context?.filesDir
            val imageDirectory = File(directory, IMAGE_DIRECTORY_NAME)
            val imgFile = File(imageDirectory, fileName+ IMAGE_FORMAT)
            if (imgFile.exists()) {
                holder.profileImage.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
            } else {
                holder.defaultUserIcon.visibility = View.VISIBLE
                holder.profilePictureCard.visibility = View.GONE
            }
        } catch (e:IOException){
            holder.defaultUserIcon.visibility = View.VISIBLE
            holder.profilePictureCard.visibility = View.GONE
        }
    }
    private fun onBindViewHolderFavorite(holder: ViewHolderForFavorite, position: Int){
        val firstName = values[position ].firstName
        val lastName = values[position ].lastName
        try {
            val directory = context?.filesDir
            val imageDirectory = File(directory, IMAGE_DIRECTORY_NAME)
            val imgFile = File(imageDirectory, firstName + lastName+ IMAGE_FORMAT)
            if (imgFile.exists()) {
                holder.letter.visibility = View.GONE
                holder.profilePicture.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
                holder.profilePictureLayout.visibility = View.VISIBLE
            } else {
                holder.letter.text =
                    values[position].firstName[0].toString().uppercase()
            }
        } catch (e:IOException){
            holder.letter.visibility = View.VISIBLE
            holder.profilePictureLayout.visibility = View.GONE
        }
        val sortFirstContactName = values[position].lastName + " " + values[position].firstName
        val sortLastContactName = values[position].firstName + " " + values[position].lastName
        if (!ContactsDisplayFragment.SORT_BY_FIRST_NAME) {
            holder.contactName.text = sortFirstContactName
        }
        else {
            holder.contactName.text = sortLastContactName
        }
        holder.editIcon.setOnClickListener { viewContactDetails(it,position) }
        holder.card.setOnClickListener{
            initiatePhoneCall(position,it.context)
        }
    }
    private fun initiatePhoneCall(position: Int,context: Context?){
        val phone:String
        if (values[position].number.isNotEmpty()) {
            phone = "tel:" + values[position].number[0]
            if (PermissionUtils.hasPermission(
                    context as MainActivity,
                    Manifest.permission.CALL_PHONE
                )
            ) {
                makePhoneCall(phone)
            } else {
                if (PermissionUtils.shouldShowRational(context, Manifest.permission.CALL_PHONE)){
                    alertDialogForGettingCallPermission()
                } else {
                    PermissionUtils.requestPermissions(
                        context, arrayOf(Manifest.permission.CALL_PHONE),
                        PermissionUtils.CALL_PERMISSION_CODE
                    )
                }
            }
        }
        else {
            Toast.makeText(context,resources.getString(R.string.number_not_available) , Toast.LENGTH_SHORT)
                .show()
        }
    }
    private fun makePhoneCall(phone:String){
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse(phone)
        (context as MainActivity).startActivity(intent)
    }
    private fun alertDialogForGettingCallPermission(){
        AlertDialog.Builder(context)
            .setTitle(resources.getString(R.string.call_permission))
            .setMessage(resources.getString(R.string.allow_permission))
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts(
                    resources.getString(R.string.uri_package),
                    (context as MainActivity).packageName, null
                )
                intent.data = uri
                (context as MainActivity).startActivity(intent)
            }.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.create().show()
    }

    private fun setRandomBackgroundColor(userIcon:Button){
        val random= Random(256)
        val color=Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256))
        userIcon.setBackgroundColor(color)
    }
    override fun getItemCount(): Int {
        return if (gridForRecycler) {
            values.size
        }
        else {
            values.size
        }
    }
    override fun getItemViewType(position: Int): Int {
        return if (gridForRecycler) {
            ContactsDisplayFragment.FAVORITES
        }
        else{
            ContactsDisplayFragment.CONTACTS
        }
    }

    private fun viewContactDetails(view: View, position: Int){
        val intent=Intent( view.context , ContactDetailsActivity::class.java )
        intent.putExtra(ContactDetailsActivity.CONTACT_KEY, values[position].dbID )
        intent.putExtra(ContactDetailsActivity.POSITION_IN_ADAPTER, position )
        (context as MainActivity).details.launch(intent)
    }
    fun setAdapterContactData(list:ArrayList<ContactDataClass>){
        values=list
    }
}