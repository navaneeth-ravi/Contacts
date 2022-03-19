package com.example.nav_contacts.presentation.activity


import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.nav_contacts.*
import com.example.nav_contacts.data.db.local_db.DatabaseFunctionalities
import com.example.nav_contacts.data.db.remote_db.SystemContact
import com.example.nav_contacts.domain.entity.ContactDataClass
import com.example.nav_contacts.presentation.adapter.ContactAndFavoriteAdapter
import com.example.nav_contacts.presentation.fragment.ContactsDisplayFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    var gridForRecycler:Boolean=false
    var portrait : Boolean = true
    private var menu:Menu?=null
    private var searchText:String=""
    lateinit var displayContactsFragment: ContactsDisplayFragment
    var contactList=ArrayList<ContactDataClass>()
    var favoriteList=ArrayList<ContactDataClass>()

    companion object {
        private const val GRID_VIEW = "grid"
        private const val SEARCH_TEXT = "search"
        private const val CONTACT_BUTTON_TEXT_COLOR_KEY = "contactColor"
        private const val FAVORITES_CONTACT_BUTTON_TEXT_COLOR_KEY = "favoriteColor"
    }
    private fun getFavoriteContactList() {
        favoriteList = ArrayList()
        for (i in contactList) {
            if (i.favorite) {
                favoriteList.add(i)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(!isContactPermission()){
            buildAlertMessageToContinueOrCloseApplication()
        }else{
            doAppInstallationAction()
        }
        if (savedInstanceState!=null) {
            gridForRecycler=savedInstanceState.getBoolean(GRID_VIEW)
            searchText=savedInstanceState.getString(SEARCH_TEXT,"")
        }
        supportFragmentManager.popBackStack()
        portrait = resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE

        if (savedInstanceState==null) {
            displayContactsFragment= ContactsDisplayFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.container_for_display_contact_fragments,displayContactsFragment).commit()
        }
        onClickContactsAndFavoriteButtons()

    }

    private fun doAppInstallationAction(){
        val sharedPreferences:SharedPreferences = getSharedPreferences("PREF_NAME",0)
        val firstTime: Boolean=sharedPreferences.getBoolean("FIRST",true)
        if(firstTime){
            buildInfo()
            Toast.makeText(this, resources.getString(R.string.loading), Toast.LENGTH_SHORT).show()
            getSystemContact()
            val edit=sharedPreferences.edit()
            edit.putBoolean("FIRST",false)
            edit.apply()
        }else {
            loadData()
        }
    }
    private fun loadData(){
        GlobalScope.launch(Dispatchers.IO) {
            val cursor= DatabaseFunctionalities.getAllContactDataFromDatabase()
            withContext(Dispatchers.Main){
                getAllContacts(cursor)
                refresh()
            }
        }
    }
    private fun isContactPermission():Boolean{
        return PermissionUtils.hasPermission(this, Manifest.permission.READ_CONTACTS)
    }
    private fun buildAlertMessageToContinueOrCloseApplication(){
        AlertDialog.Builder(this).setTitle(resources.getString(R.string.allow_app))
            .setPositiveButton(resources.getString(R.string.ok)){ _, _ ->
                PermissionUtils.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PermissionUtils.CONTACTS_PERMISSION_CODE
                )
        }.setNegativeButton(resources.getString(R.string.cancel)){ dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
        }.setCancelable(false).show()
    }
    private fun buildInfo(){
        AlertDialog.Builder(this).setTitle(resources.getString(R.string.allow_app))
            .setPositiveButton(resources.getString(R.string.ok)){ dialogInterface, _ ->
                dialogInterface.dismiss()
            }.setCancelable(false).setTitle("Attention Users!!!")
            .setMessage(resources.getString(R.string.instructionsInfo)).show()
    }

    val details=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK) {
            val intent = it.data
            val dbId = intent?.getIntExtra(ContactDetailsActivity.CONTACT_KEY, -1)
            val adapterPosition = intent?.getIntExtra(ContactDetailsActivity.POSITION_IN_ADAPTER,-1)
            GlobalScope.launch(Dispatchers.IO) {
                if (dbId != -1 && adapterPosition != -1 && adapterPosition!=null) {
                    val cursor = DatabaseFunctionalities.getContact(dbId.toString())
                    withContext(Dispatchers.Main) {
                        updateAdapterData(cursor,adapterPosition,dbId)
                    }
                }

            }
        }
        if(it.resultCode == RESULT_CANCELED) {
            Toast.makeText(this, resources.getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateAdapterData(cursor: Cursor?,adapterPosition:Int,dbId:Int?){
        cursor?.moveToFirst()
        val data= ContactDataClass.getContact(cursor)
        val adapter =
            displayContactsFragment.recyler.adapter as ContactAndFavoriteAdapter
        if(data!=null){
            if(gridForRecycler) {           // update in Favorites
                if (!data.favorite ) {
                    adapter.values.removeAt( adapterPosition )
                    adapter.notifyItemRemoved( adapterPosition )
                }else{
                    adapter.values[adapterPosition]=data
                    adapter.notifyItemChanged(adapterPosition)
                }
                val contact=contactList.find { it.dbID==data.dbID }
                val index = contactList.indexOf(contact)
                contactList[index] = data
                Toast.makeText(this, "${contact?.firstName}", Toast.LENGTH_SHORT).show()
            }else{                         // update in Contacts
                if( data.favorite && !favoriteList.any { it.dbID == data.dbID } ){
                    favoriteList.add( data )
                    favoriteList.sortBy { it.firstName.uppercase() }
                }
                if(!data.favorite && favoriteList.any{it.dbID == data.dbID}){
                    favoriteList.removeIf { it.dbID==data.dbID }
                }
                adapter.values[adapterPosition]=data
                adapter.notifyItemChanged(adapterPosition)
                val contact=favoriteList.find { it.dbID==data.dbID }
                val index = favoriteList.indexOf(contact)
//                favoriteList[index]=data
            }
        } else{                           // update if contact is deleted
            adapter.values.removeAt(adapterPosition)
            contactList.removeIf { it.dbID==dbId }
            favoriteList.removeIf { it.dbID==dbId }
            adapter.notifyItemRemoved(adapterPosition)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu !=null) {
            val emptySearchText = ""
            val search = menu.findItem(R.id.search)
            if (searchText!=emptySearchText){
                search.expandActionView()
                (search.actionView as SearchView).setQuery(searchText,false)
                val searchView=(search.actionView as SearchView)
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    searchView.clearFocus()
                }
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
    private fun expandSearchListener(searchItem:MenuItem,menu: Menu){
        searchItem.setOnActionExpandListener(object : OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                menu.findItem(R.id.sort).isVisible = false
                findViewById<LinearLayout>(R.id.footer).visibility=View.GONE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                menu.findItem(R.id.sort).isVisible = true
                findViewById<LinearLayout>(R.id.footer).visibility=View.VISIBLE
                return true
            }
        })
    }
    private fun setSearchListener(search: SearchView){
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            var searchAdapterValues=ArrayList<ContactDataClass>()
            override fun onQueryTextChange(search: String?): Boolean {
                searchAdapterValues.clear()
                val adapter: ContactAndFavoriteAdapter =(displayContactsFragment.recyler.adapter as ContactAndFavoriteAdapter)
                if (search!=null) {
                    if (!gridForRecycler) {
                        contactList.forEach {
                            if ((it.firstName+" "+it.lastName).contains(search, true) ||
                                (it.firstName+" "+it.lastName).contains(search, true))
                                {
                                searchAdapterValues.add(it)
                            }
                        }
                    } else {
                        favoriteList.forEach {
                            if ((it.firstName+" "+it.lastName).contains(search, true) ||
                                (it.firstName+" "+it.lastName).contains(search, true))
                                {
                                searchAdapterValues.add(it)
                            }
                        }
                    }
                }
                adapter.setAdapterContactData(searchAdapterValues)
                adapter.notifyDataSetChanged()
                return false
            }
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        this.menu=menu
        menuInflater.inflate(R.menu.contact_app_menu,menu)
        if(menu!=null){
            val searchItem=menu.findItem(R.id.search)
            expandSearchListener(searchItem,menu)
            val search: SearchView = searchItem.actionView as SearchView
            search.maxWidth= Int.MAX_VALUE
            setSearchListener(search)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.first_name -> {
                displayContactsFragment.sortByFirstName()
            }
            R.id.last_name -> {
                displayContactsFragment.sortByLastName()
            }
            R.id.instructions -> {
                buildInfo()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== PermissionUtils.CALL_PERMISSION_CODE && grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, resources.getString(R.string.permission_allowed), Toast.LENGTH_SHORT).show()
        }
        if (requestCode== PermissionUtils.CONTACTS_PERMISSION_CODE && grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, resources.getString(R.string.permission_allowed), Toast.LENGTH_SHORT).show()
            doAppInstallationAction()
        }else{
            Toast.makeText(this,resources.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            if(requestCode== PermissionUtils.CONTACTS_PERMISSION_CODE) {
                finish()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        findViewById<Button>(R.id.fav).setTextColor(savedInstanceState.getInt(
            FAVORITES_CONTACT_BUTTON_TEXT_COLOR_KEY
        ))

        findViewById<Button>(R.id.contacts).setTextColor(savedInstanceState.getInt(
            CONTACT_BUTTON_TEXT_COLOR_KEY
        ))

        searchText=savedInstanceState.getString(SEARCH_TEXT,"")
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(GRID_VIEW,gridForRecycler)
        val favColor=findViewById<Button>(R.id.fav).textColors
        val contactColor=findViewById<Button>(R.id.contacts).textColors
        outState.putInt(FAVORITES_CONTACT_BUTTON_TEXT_COLOR_KEY,favColor.defaultColor)
        outState.putInt(CONTACT_BUTTON_TEXT_COLOR_KEY,contactColor.defaultColor)
        if (menu!!.findItem(R.id.search)!!.isActionViewExpanded) {
            outState.putString(SEARCH_TEXT,"${(menu!!.findItem(R.id.search).actionView as SearchView).query}")
        }
    }

    private fun onClickContactsAndFavoriteButtons(){
        val favorites:Button=findViewById(R.id.fav)
        val contacts:Button=findViewById(R.id.contacts)
        favorites.setOnClickListener {
            displayContactsFragment= ContactsDisplayFragment()
            gridForRecycler=true
            favorites.setTextColor(getColor(R.color.blue))
            contacts.setTextColor(getColor(R.color.black))
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction().replace(R.id.container_for_display_contact_fragments,displayContactsFragment/*FavoritesFragment()*/).commit()
        }
        contacts.setOnClickListener {
            displayContactsFragment= ContactsDisplayFragment()
            gridForRecycler=false
            favorites.setTextColor(getColor(R.color.black))
            contacts.setTextColor(getColor(R.color.blue))
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction().replace(R.id.container_for_display_contact_fragments,displayContactsFragment).commit()
        }
    }

    fun getAllContacts(cursor: Cursor?){
        contactList= ArrayList()
        if (cursor!!.moveToNext()) {
            while (!cursor.isAfterLast) {
                val contactDataClass= ContactDataClass.getContact(cursor)
                if(contactDataClass!=null){
                    contactList.add(contactDataClass)
                }
                cursor.moveToNext()
            }
            cursor.close()
        }
    }
    private fun getSystemContact() {
        GlobalScope.launch(Dispatchers.IO) {
            SystemContact.getAllContacts()
            withContext(Dispatchers.Main){
                loadData()
            }
        }
    }

    private fun refresh(){
        contactList.sortBy { it.firstName.uppercase() }
        getFavoriteContactList()
        refreshRecycler()
    }

    private fun refreshRecycler(){
        val adapter=(displayContactsFragment.recyler.adapter as ContactAndFavoriteAdapter)
        if (gridForRecycler) {
            adapter.setAdapterContactData(favoriteList)
        } else{
            adapter.setAdapterContactData(contactList)
        }
        adapter.notifyDataSetChanged()
    }
}