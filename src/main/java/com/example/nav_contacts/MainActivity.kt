package com.example.nav_contacts


import android.content.ContentValues
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
        ContactMain.resources=resources
        ContactMain.contentResolver=contentResolver
        setContentView(R.layout.activity_main)
        if (savedInstanceState!=null) {
            gridForRecycler=savedInstanceState.getBoolean(GRID_VIEW)
            searchText=savedInstanceState.getString(SEARCH_TEXT,"")
        }
        supportFragmentManager.popBackStack()
        portrait = resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE

        if (savedInstanceState==null) {
            displayContactsFragment=ContactsDisplayFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.container_for_display_contact_fragments,displayContactsFragment).commit()
        }
        onClickContactsAndFavoriteButtons()
        loadData()
    }
    private fun loadData(){
        GlobalScope.launch(Dispatchers.IO) {
            val cursor=DatabaseFunctionalities.getAllContactDataFromDatabase()
            withContext(Dispatchers.Main){
                getAllContacts(cursor)
                refresh()
            }
        }
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
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateAdapterData(cursor: Cursor?,adapterPosition:Int,dbId:Int?){
        cursor?.moveToFirst()
        val data=ContactDataClass.getContact(cursor)
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
            }else{                         // update in Contacts
                if( data.favorite && !favoriteList.any { it.dbID == data.dbID } ){
                    favoriteList.add( data )
                    favoriteList.sortBy { it.firstName.uppercase() }
                }
                adapter.values[adapterPosition]=data
                adapter.notifyItemChanged(adapterPosition)
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
                val adapter:ContactAndFavoriteAdapter=(displayContactsFragment.recyler.adapter as ContactAndFavoriteAdapter)
                Toast.makeText(this@MainActivity, "${adapter.values.size} ada", Toast.LENGTH_SHORT).show()
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
            R.id.dummy -> {
                addDummyContacts()
                item.isVisible = false
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
        if (requestCode==PermissionUtils.CALL_PERMISSION_CODE && grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, resources.getString(R.string.permission_allowed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        findViewById<Button>(R.id.fav).setTextColor(savedInstanceState.getInt(
            FAVORITES_CONTACT_BUTTON_TEXT_COLOR_KEY))

        findViewById<Button>(R.id.contacts).setTextColor(savedInstanceState.getInt(
            CONTACT_BUTTON_TEXT_COLOR_KEY))

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
            displayContactsFragment=ContactsDisplayFragment()
            gridForRecycler=true
            favorites.setTextColor(getColor(R.color.blue))
            contacts.setTextColor(getColor(R.color.black))
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction().replace(R.id.container_for_display_contact_fragments,displayContactsFragment/*FavoritesFragment()*/).commit()
        }
        contacts.setOnClickListener {
            displayContactsFragment=ContactsDisplayFragment()
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
                val contactDataClass=ContactDataClass.getContact(cursor)
                if(contactDataClass!=null){
                    contactList.add(contactDataClass)
                }
                cursor.moveToNext()
            }
            cursor.close()
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
            adapter.assignAdapterData(favoriteList)
        } else{
            adapter.assignAdapterData(contactList)
        }
        adapter.notifyDataSetChanged()
    }
    private fun addDummyContacts(){
        val firstName = arrayOf(
            "Police",
            "Government",
            "SIM",
            "IRCTC",
            "Fire",
            "Women's ",
            "Corona TN",
            "Traffic",
            "Navaneethan"
        )
        val lastName = arrayOf(
            "",
            "Ambulance",
            "Complaint",
            "HelpLine",
            "Service",
            "HelpLine",
            "HelpLine",
            "HelpLine",
            ""
        )
        val numberdum = arrayOf(
            "100",
            "108",
            "199",
            "1800111139",
            "101",
            "1091",
            "+91-11-23978046",
            "1073",
            "8838900839"
        )
        for (i in firstName.indices) {
            val values = ContentValues()
            values.put(MyContentProvider.FIRST_NAME, firstName[i])
            values.put(MyContentProvider.LAST_NAME, lastName[i])
            values.put(MyContentProvider.NUMBER1, numberdum[i])
            values.put(MyContentProvider.NUMBER2, resources.getString(R.string.empty))
            values.put(MyContentProvider.EMAIL, "")
            values.put(MyContentProvider.FAVORITE, 1)
            values.put(MyContentProvider.PROFILE_IMAGE, firstName[i]+lastName[i]+resources.getString(R.string.image_format))
            DatabaseFunctionalities.insert(values)
        }
        loadData()
    }

}