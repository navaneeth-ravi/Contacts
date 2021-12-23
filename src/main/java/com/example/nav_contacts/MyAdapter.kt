package com.example.nav_contacts

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.arch.core.executor.DefaultTaskExecutor
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class MyAdapter(private val gridForRecycler:Boolean,var number: ArrayList<String>?=null,var context:Context?=null):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
     var values:ArrayList<Contact>
//    private var contactList:ArrayList<Contact> = Database.list
//    var favList:ArrayList<Contact> =Database.favList
    init {
        Database(context).getAlldata()
        values=assignAdapterData()
    }
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val button:Button=itemView.findViewById(R.id.contact_icon)
        val name:TextView=itemView.findViewById(R.id.contact_name)
        val card:RelativeLayout=itemView.findViewById(R.id.card)
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
        return if(viewType== DisplayContactsFragment.CONTACTS) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_view, parent, false)
            ViewHolder(view)
        }else if (viewType== DisplayContactsFragment.FAVORITES){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.favorites_item, parent, false)
            ViewHolder1(view)
        }else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item_contact_details, parent, false)
            ViewHolder2(view)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        if (getItemViewType(position)==DisplayContactsFragment.CONTACTS) {
            holder as ViewHolder
            holder.button.text = values[position % values.size].firstName[0].toString()
            //setRandomBackgroundColor(holder.button)
            holder.name.text ="${values[position % values.size].firstName} ${values[position % values.size].lastName}"
            holder.card.setOnClickListener {
                showContactDetails(it, position)
            }
            holder.button.setOnClickListener {
                showContactDetails(it, position)
            }
        }else if(getItemViewType(position)==DisplayContactsFragment.FAVORITES){
            holder as ViewHolder1
            holder.letter.text= values[position % values.size].firstName[0].toString()
            holder.contactName.text =values[position % values.size].firstName + " " + values[position % values.size].lastName
//            setRandomBackgroundColor(holder.card)
            holder.edit.setOnClickListener { showContactDetails(it,position) }
            holder.card.setOnClickListener{
                val phone="tel:"+values[position].number[0]
                val intent= Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse(phone)
                it.context.startActivity(intent)
            }
        }else{
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
        return if(gridForRecycler && number==null) DisplayContactsFragment.FAVORITES
        else if(!gridForRecycler && number==null) DisplayContactsFragment.CONTACTS
        else 2
    }
    private fun showContactDetails(view: View, position: Int){
        val intent=Intent(view.context,ContactDetails::class.java)
        intent.putExtra("abc",values[position%values.size])
        view.context.startActivity(intent)
    }
    private fun assignAdapterData():ArrayList<Contact>{
        return if(gridForRecycler)
            Database.favList
        else
            Database.list
    }
    fun setAdapterData(){
        values=Database.list
    }
}