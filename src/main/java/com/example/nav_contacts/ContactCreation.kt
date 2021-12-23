package com.example.nav_contacts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.lang.Exception

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
    private fun getvalues():Contact{
        val firstName:String=findViewById<EditText>(R.id.first_name).text.toString()
        val lasName:String=findViewById<EditText>(R.id.last_name).text.toString()
        val number=ArrayList<String>()
        val number1=findViewById<EditText>(R.id.number1).text.toString()
        val number2=findViewById<EditText>(R.id.number2).text.toString()
        val email:String=findViewById<EditText>(R.id.email).text.toString()
        if(number1=="")
            number.add("empty")else number.add(number1)
        if(number2=="")
            number.add("empty")else number.add(number2)
        return Contact(firstName,lasName,number,email)
    }
    private fun saveContactWhileClick(option:Int){
        val firstName:String=findViewById<EditText>(R.id.first_name).text.toString()
        val lasName:String=findViewById<EditText>(R.id.last_name).text.toString()
        val number=ArrayList<String>()
        val number1=findViewById<EditText>(R.id.number1).text.toString()
        val number2=findViewById<EditText>(R.id.number2).text.toString()
        val email:String=findViewById<EditText>(R.id.email).text.toString()
        if(number1=="")
            number.add("empty")else number.add(number1)
        if(number2=="")
            number.add("empty")else number.add(number2)

        if(option== CREATE)
        findViewById<Button>(R.id.save).setOnClickListener {
//            number.add(number1)
            val detailContact=getvalues()
            if(detailContact.firstName != "" || detailContact.lastName != "") {
                val db = DBHelper(this, null)
                db.addToDatabase(detailContact.firstName, detailContact.lastName, detailContact.number[0], detailContact.number[1], detailContact.email, false)
                db.close()
                var deleteCount=0
                try {
//                    if (number.isNotEmpty())
//                        for (j in 0 until number.size)
//                            if (number[j] == "empty") {
//                                Toast.makeText(this, "$j", Toast.LENGTH_SHORT).show()
//                                number.removeAt(j - deleteCount)
//                                deleteCount += 1
//                            }
                }catch (e:Exception){}
                finally {
                    Database.list.add(Contact(firstName,lasName, number,email,false))
                    Database(this).getAlldata()
                }

                Toast.makeText(this, "$firstName $lasName  added", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
        else{
            findViewById<Button>(R.id.save).setOnClickListener {
//            number.add(number1)
                val detailContact=getvalues()
                val contact=intent.getSerializableExtra("contact") as Contact
                val db=DBHelper(this,null)
                detailContact.dbID=contact.dbID
                db.update(detailContact)
                Database(this).getAlldata()
//                for(i in Database.list){
//                    if(i.dbID==contact.dbID){
//                        i.firstName=firstName
//                        i.lastName=lasName
//                        i.number=number
//                        i.email=email
//                        val db=DBHelper(this,null)
//                        db.update(i)
//                        Toast.makeText(this, "edited", Toast.LENGTH_SHORT).show()
//                        db.close()
//                        try {
//                            for (j in 0 until number.size)
//                                if (i.number[j] == "empty") {
//                                    i.number.removeAt(j)
//                                }
//                        }catch (e:Exception){
//
//                        }
//                    }
//                }
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
        if(contact.number.isNotEmpty()) {
            if(contact.number[0]!="empty") {
                val number1 = findViewById<EditText>(R.id.number1)
                number1.setText(contact.number[0])
            }
            if(contact.number.size==2) {
                val number2 = findViewById<EditText>(R.id.number2)
                number2.setText(contact.number[1])
            }
        }
        val email=findViewById<EditText>(R.id.email)
        email.setText(contact.email)
    }
}