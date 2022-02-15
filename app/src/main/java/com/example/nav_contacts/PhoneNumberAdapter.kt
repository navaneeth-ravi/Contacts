package com.example.nav_contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nav_contacts.ContactMain.Companion.resources

class PhoneNumberAdapter(var number: ArrayList<String>?=null, var context: Context?=null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class ViewHolderForPhoneNumberInContactDetails(itemView: View):RecyclerView.ViewHolder(itemView){
        val number: TextView = itemView.findViewById(R.id.number)
        val messageIcon: ImageView = itemView.findViewById(R.id.message)
        val defaultCallLayout: RelativeLayout = itemView.findViewById(R.id.lay)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_number_contact_details, parent, false)
        return ViewHolderForPhoneNumberInContactDetails(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolderForPhoneNumberInContactDetails
        val number=number?.get(position)
        holder.number.text=number
        holder.defaultCallLayout.setOnClickListener {
            (context as ContactDetailsActivity).requestCallPermission(number)
        }
        holder.messageIcon.setOnClickListener {
            Toast.makeText(it.context, resources.getString(R.string.on_progress_feature), Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return number?.size ?: 0
    }
}