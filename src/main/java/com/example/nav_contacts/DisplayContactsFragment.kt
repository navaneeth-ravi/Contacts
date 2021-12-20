package com.example.nav_contacts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DisplayContactsFragment : Fragment() {
    lateinit var recyler:RecyclerView
    companion object{
        val CONTACTS=0
        val FAVORITES=1
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view1=inflater.inflate(R.layout.fragment_display_contacts, container, false)
        val buttonCreate=view1.findViewById<Button>(R.id.create)
        buttonCreate.visibility=View.VISIBLE
        addNewContactWhenClick(buttonCreate)
        recyler = view1.findViewById(R.id.recycler)
//        recyler.setHasFixedSize(true)
        val parent = activity as MainActivity
        if (parent.gridForRecycler) {
            buttonCreate.visibility=View.GONE
            if (!(activity as MainActivity).portrait)
                recyler.layoutManager = GridLayoutManager(activity, 3)
            else recyler.layoutManager = GridLayoutManager(activity, 2)
        }
        else recyler.layoutManager = LinearLayoutManager(activity)
        recyler.adapter = MyAdapter(parent.gridForRecycler)
        return view1
    }
    private fun addNewContactWhenClick(button: Button){
        button.setOnClickListener {
            val intent=Intent(this.context,ContactCreation::class.java)
            intent.putExtra("option",ContactCreation.CREATE)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        recyler.adapter?.notifyDataSetChanged()
    }
}