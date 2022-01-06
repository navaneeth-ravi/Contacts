package com.example.nav_contacts

import android.Manifest
import android.annotation.SuppressLint
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

class MyAdapter(private val gridForRecycler:Boolean,var number: ArrayList<String>?=null,var context:Context?=null):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var values:ArrayList<Contact>

    init {
        Database(context).getAlldata()
        values=assignAdapterData()
    }
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val button:Button=itemView.findViewById(R.id.contact_icon)
        val name:TextView=itemView.findViewById(R.id.contact_name)
        val card:RelativeLayout=itemView.findViewById(R.id.card)
        val defaultUserIcon:Button=itemView.findViewById(R.id.contact_icon)
        val profileImage:ImageView=itemView.findViewById(R.id.profile_image)
        val profileCard:CardView=itemView.findViewById(R.id.contact_icon1)
    }
    inner class ViewHolder1(itemView: View): RecyclerView.ViewHolder(itemView){
        val letter:TextView=itemView.findViewById(R.id.letter)
        val contactName:TextView=itemView.findViewById(R.id.contact)
        val card:CardView=itemView.findViewById(R.id.fav_card)
        val edit:ImageView=itemView.findViewById(R.id.edit_fav)
    }
    inner class ViewHolder2(itemView: View):RecyclerView.ViewHolder(itemView){
        val number:TextView=itemView.findViewById(R.id.number)
        val messageIcon:ImageView=itemView.findViewById(R.id.message)
        val layout:RelativeLayout=itemView.findViewById(R.id.lay)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType== ContactsDisplayFragment.CONTACTS) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_view, parent, false)
            ViewHolder(view)
        }else if (viewType== ContactsDisplayFragment.FAVORITES){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.favorites_item, parent, false)
            ViewHolder1(view)
        }else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item_contact_details, parent, false)
            ViewHolder2(view)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onBindViewHolderContact(holder: ViewHolder, position: Int){
        val firstName = values[position % values.size].firstName
        val lastName = values[position % values.size].lastName
        holder.button.text = firstName[0].toString()
        holder.name.text = "${firstName}  ${lastName}"
        if (!ContactsDisplayFragment.SORT_BY_FIRST_NAME)
            holder.name.text = lastName + " " + firstName
        //setRandomBackgroundColor(holder.button)

//        holder.name.text = firstName+lastName
        holder.card.setOnClickListener { showContactDetails(it, position) }
        holder.button.setOnClickListener { showContactDetails(it, position) }

        val directory = context?.filesDir
        val imageDirectory = File(directory, "profileImages")
        val imgFile = File(imageDirectory, "${firstName + lastName}.png")
        if(imgFile.exists()) {
            holder.profileImage.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
        }else{
            holder.defaultUserIcon.visibility=View.VISIBLE
            holder.profileCard.visibility=View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onBindViewHolderFavorite(holder: ViewHolder1, position: Int){
        holder.letter.text= values[position % values.size].firstName[0].toString()
        if(!ContactsDisplayFragment.SORT_BY_FIRST_NAME)
            holder.contactName.text=values[position % values.size].lastName + " " + values[position % values.size].firstName
        else
            holder.contactName.text =values[position % values.size].firstName + " " + values[position % values.size].lastName
//            setRandomBackgroundColor(holder.card)
        holder.edit.setOnClickListener { showContactDetails(it,position) }
        holder.card.setOnClickListener{
            var phone:String
            if(values[position].number.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(it.context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED )
                {
                    phone = "tel:" + values[position].number[0]
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse(phone)
                    it.context.startActivity(intent)
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            context as MainActivity,
                            Manifest.permission.CALL_PHONE
                        )
                    ) {
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
                                it.context.startActivity(intent)
                            }.setNegativeButton("cancel") { dialogInterface, _ ->
                                dialogInterface.dismiss()
                            }.create().show()
                    } else {
                        ActivityCompat.requestPermissions(context as MainActivity,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            CallPermission.CALL_PERMISSION_CODE
                        );
                    }
                }
            }
            else
                Toast.makeText(it.context, "Number not available for Call", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        if (getItemViewType(position)==ContactsDisplayFragment.CONTACTS) {
            holder as ViewHolder
            onBindViewHolderContact(holder,position)
        }
        else if(getItemViewType(position)==ContactsDisplayFragment.FAVORITES){
            holder as ViewHolder1
            onBindViewHolderFavorite(holder,position)
        }
        else{
            holder as ViewHolder2
            val number=number?.get(position)
            holder.number.text="+91 $number"
            holder.layout.setOnClickListener {
                (context as ContactDetails).requestCallPermission(number)
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
        val intent=Intent( view.context , ContactDetails::class.java )
        intent.putExtra("abc",values[position%values.size])
        view.context.startActivity(intent)
    }
    private fun assignAdapterData():ArrayList<Contact>{
        return if(gridForRecycler)
            Database.favList
        else
            Database.list
    }
    fun setAdapterContactData(list:ArrayList<Contact>){
        values=list
    }
    fun setAdapterFavoritesData(list:ArrayList<Contact>){
        values=list
    }
}