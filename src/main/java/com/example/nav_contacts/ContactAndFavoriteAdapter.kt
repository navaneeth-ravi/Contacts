package com.example.nav_contacts

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import kotlin.random.Random


class ContactAndFavoriteAdapter(private val gridForRecycler:Boolean, var number: ArrayList<String>?=null, var context:Context?=null):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var values:ArrayList<ContactDataClass>

    init {
//        Database.getAlldata()
        values=assignAdapterData()
    }
    inner class ViewHolderForContact(itemView: View):RecyclerView.ViewHolder(itemView){
        val button:Button=itemView.findViewById(R.id.contact_icon)
        val name:TextView=itemView.findViewById(R.id.contact_name)
        val card:RelativeLayout=itemView.findViewById(R.id.card)
        val defaultUserIcon:Button=itemView.findViewById(R.id.contact_icon)
        val profileImage:ImageView=itemView.findViewById(R.id.profile_image)
        val profileCard:CardView=itemView.findViewById(R.id.contact_icon1)
    }
    inner class ViewHolderForFavorite(itemView: View): RecyclerView.ViewHolder(itemView){
        val letter:TextView=itemView.findViewById(R.id.letter)
        val contactName:TextView=itemView.findViewById(R.id.contact)
        val card:CardView=itemView.findViewById(R.id.fav_card)
        val edit:ImageView=itemView.findViewById(R.id.edit_fav)
        val userIconLayout:CardView=itemView.findViewById(R.id.user_image)
        val userIcon:ImageView=itemView.findViewById(R.id.user_ic)
    }
    inner class ViewHolderForPhoneNumberInContactDetails(itemView: View):RecyclerView.ViewHolder(itemView){
        val number:TextView=itemView.findViewById(R.id.number)
        val messageIcon:ImageView=itemView.findViewById(R.id.message)
        val layout:RelativeLayout=itemView.findViewById(R.id.lay)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType== ContactsDisplayFragment.CONTACTS) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
            ViewHolderForContact(view)
        }else if (viewType== ContactsDisplayFragment.FAVORITES){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.favorites_item, parent, false)
            ViewHolderForFavorite(view)
        }else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_number_contact_details, parent, false)
            ViewHolderForPhoneNumberInContactDetails(view)
        }
    }

    private fun onBindViewHolderContact(holder: ViewHolderForContact, position: Int){
        holder.button.text = values[position % values.size].firstName[0].toString()
        val sortFirstContactName=values[position % values.size].lastName + " " + values[position % values.size].firstName+values[position].dbID
        val sortLastContactName=values[position % values.size].firstName + " " + values[position % values.size].lastName
        holder.name.text = sortFirstContactName
        if (!ContactsDisplayFragment.SORT_BY_FIRST_NAME) {
            holder.name.text = sortLastContactName
        }

        //setRandomBackgroundColor(holder.button)

        holder.card.setOnClickListener {
            if(values[position].dbID==null){
                for(i in Database.list){
                    if(values[position].copy(dbID = i.dbID)==i){
                        showContactDetails(it, position)
                    }else{
                        values[position].dbID=null
                    }
                }
            }else {
                showContactDetails(it, position)
            }
        }
        holder.button.setOnClickListener { showContactDetails(it, position) }
        val fileName=values[position % values.size].lastName+ values[position % values.size].firstName
        val directory = context?.filesDir
        val imageDirectory = File(directory, "profileImages")
        val imgFile = File(imageDirectory, "$fileName.png")
        if(imgFile.exists()) {
            holder.profileImage.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
        }else{
            holder.defaultUserIcon.visibility=View.VISIBLE
            holder.profileCard.visibility=View.GONE
        }
    }

    private fun onBindViewHolderFavorite(holder: ViewHolderForFavorite, position: Int){
        val firstName = values[position % values.size].firstName
        val lastName = values[position % values.size].lastName

        val directory = context?.filesDir
        val imageDirectory = File(directory, "profileImages")
        val imgFile = File(imageDirectory, "${firstName + lastName}.png")

        if(imgFile.exists()){
            holder.letter.visibility=View.GONE
            holder.userIcon.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
            holder.userIconLayout.visibility=View.VISIBLE
        }else
            holder.letter.text= values[position % values.size].firstName[0].toString().uppercase()

        val sortFirstContactName=values[position % values.size].lastName + " " + values[position % values.size].firstName
        val sortLastContactName=values[position % values.size].firstName + " " + values[position % values.size].lastName
        if(!ContactsDisplayFragment.SORT_BY_FIRST_NAME) {
            holder.contactName.text = sortFirstContactName
        }
        else {
            holder.contactName.text = sortLastContactName
        }
//            setRandomBackgroundColor(holder.card)
        holder.edit.setOnClickListener { showContactDetails(it,position) }
        holder.card.setOnClickListener{
            val phone:String
            if(values[position].number.isNotEmpty()) {
                phone = "tel:" + values[position].number[0]
                if (PermissionUtils.hasPermission(context as MainActivity,Manifest.permission.CALL_PHONE)) {
                    makeCall(phone)
                } else {
                    if (PermissionUtils.shouldShowRational(context as MainActivity,Manifest.permission.CALL_PHONE)){
                        alertDialogForGettingCallPermission()
                    } else {
                        PermissionUtils.requestPermissions(context as MainActivity, arrayOf(Manifest.permission.CALL_PHONE),
                            PermissionUtils.CALL_PERMISSION_CODE)
                    }
                }
            }
            else
                Toast.makeText(it.context, "Number not available for Call", Toast.LENGTH_SHORT).show()
        }
    }
    private fun makeCall(phone:String){
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse(phone)
        (context as MainActivity).startActivity(intent)
    }
    private fun alertDialogForGettingCallPermission(){
        AlertDialog.Builder(context)
            .setTitle("Call permissions needed to make call")
            .setMessage("'ok' to allow from settings")
            .setPositiveButton("ok") { _, _ ->
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts(
                    "package",
                    (context as MainActivity).packageName, null
                )
                intent.data = uri
                (context as MainActivity).startActivity(intent)
            }.setNegativeButton("cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.create().show()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        if (getItemViewType(position)==ContactsDisplayFragment.CONTACTS) {
            holder as ViewHolderForContact
            onBindViewHolderContact(holder,position)
        }
        else if(getItemViewType(position)==ContactsDisplayFragment.FAVORITES){
            holder as ViewHolderForFavorite
            onBindViewHolderFavorite(holder,position)
        }
        else{
            holder as ViewHolderForPhoneNumberInContactDetails
            val number=number?.get(position)
            holder.number.text="$number"
            holder.layout.setOnClickListener {
                (context as ContactDetailsActivity).requestCallPermission(number)
            }
            holder.messageIcon.setOnClickListener {
                Toast.makeText(it.context, " This feature is 'On progress'", Toast.LENGTH_SHORT).show()
            }
        }
    private fun setRandomBackgroundColor(cardView: CardView){
        val random= Random(256)
        val color=Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256))
        cardView.setBackgroundColor(color)
        Log.i("abc", "setRandomBackgroundColor: $color")
    }
    override fun getItemCount(): Int {
        return if(gridForRecycler && number==null)values.size
        else if(!gridForRecycler && number==null) values.size
        else number?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return if(gridForRecycler && number==null) ContactsDisplayFragment.FAVORITES
        else if(!gridForRecycler && number==null) ContactsDisplayFragment.CONTACTS
        else 2
    }
    private fun showContactDetails(view: View, position: Int){
        val intent=Intent( view.context , ContactDetailsActivity::class.java )
        intent.putExtra("abc",values[position%values.size])
        intent.putExtra("positionInAdapter",position)
        view.context.startActivity(intent)
    }
    private fun assignAdapterData():ArrayList<ContactDataClass>{
        return if(gridForRecycler)
            Database.favList
        else
            Database.list
    }
    fun setAdapterContactData(list:ArrayList<ContactDataClass>){
        values=list
    }
    fun setAdapterFavoritesData(list:ArrayList<ContactDataClass>){
        values=list
    }
}