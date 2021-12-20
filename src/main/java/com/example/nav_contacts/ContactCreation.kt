package com.example.nav_contacts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class ContactCreation : AppCompatActivity() {
    companion object{
        const val EDIT=0
        const val CREATE=1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_creation)
        val option=intent.getIntExtra("option",1)
        if(option== EDIT)
            editContact()
        saveContactWhileClick(option)
        clickCloseButton()
    }
    private fun saveContactWhileClick(option:Int){
        if(option== CREATE)
        findViewById<Button>(R.id.save).setOnClickListener {
            val firstName:String=findViewById<EditText>(R.id.first_name).text.toString()
            val lasName:String=findViewById<EditText>(R.id.last_name).text.toString()
            val number=ArrayList<String>()
            number.add(findViewById<EditText>(R.id.number1).text.toString())
            number.add(findViewById<EditText>(R.id.number2).text.toString())
            val email:String=findViewById<EditText>(R.id.email).text.toString()
            val newContact=Contact(firstName,lasName,number,email)
            Database.list.add(newContact)
            Toast.makeText(this, "$firstName $lasName  added", Toast.LENGTH_SHORT).show()
            finish()
        }
        else{
            findViewById<Button>(R.id.save).setOnClickListener {
                val firstName:String=findViewById<EditText>(R.id.first_name).text.toString()
                val lasName:String=findViewById<EditText>(R.id.last_name).text.toString()
                val number=ArrayList<String>()
                number.add(findViewById<EditText>(R.id.number1).text.toString())
                number.add(findViewById<EditText>(R.id.number2).text.toString())
                val email:String=findViewById<EditText>(R.id.email).text.toString()
                val contact=intent.getSerializableExtra("contact") as Contact
                for(i in Database.list){
                    if(i.userId==contact.userId){
                        i.firstName=firstName
                        i.lastName=lasName
                        i.number=number
                        i.email=email
                    }
                }
//                val newContact=Contact(firstName,lasName,number,email)
//                Database.list.add(newContact)
                Toast.makeText(this, "$firstName $lasName  edited", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun clickCloseButton(){
        findViewById<ImageView>(R.id.close).setOnClickListener {
            finish()
        }
    }
    private fun editContact(){
        findViewById<TextView>(R.id.header_title).text="Edit contact"
        val contact=intent.getSerializableExtra("contact") as Contact
        val first_name=findViewById<EditText>(R.id.first_name)
        first_name.setText(contact.firstName)
        val last_name=findViewById<EditText>(R.id.last_name)
        last_name.setText(contact.lastName)
        val number1=findViewById<EditText>(R.id.number1)
        number1.setText(contact.number[0])
        val number2=findViewById<EditText>(R.id.number2)
        number2.setText(contact.number[1])
        val email=findViewById<EditText>(R.id.email)
        email.setText(contact.email)
    }
}