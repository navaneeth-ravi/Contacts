package com.example.nav_contacts


import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import kotlin.properties.Delegates
import android.view.MenuItem.OnActionExpandListener


class MainActivity : AppCompatActivity() {
    var gridForRecycler:Boolean=false
    var portrait by Delegates.notNull<Boolean>()
    private var menu:Menu?=null
    private var searchText:String=""
    lateinit var displayContactsFragment: ContactsDisplayFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Database.openDatabase(this)
        Database.getAlldata()
        setContentView(R.layout.activity_main)

        if(savedInstanceState!=null){
            gridForRecycler=savedInstanceState.getBoolean("grid")
            searchText=savedInstanceState.getString("search","")
        }

        supportFragmentManager.popBackStack()
        portrait = getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE

        if(savedInstanceState==null) {
            displayContactsFragment=ContactsDisplayFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.container_for_display_contact_fragments,displayContactsFragment).commit()
        }
        onclickButtons()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(menu!=null) {
            val search=menu.findItem(R.id.search)
            if (searchText!=""){
                search.expandActionView()
                (search.actionView as SearchView).setQuery(searchText,false)
                val s=(search.actionView as SearchView)
                if(findViewById<LinearLayout>(R.id.activity_main_portrait)==null)
                    s.clearFocus()
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
    private fun expandSearchListener(searchItem:MenuItem,menu: Menu){
        searchItem.setOnActionExpandListener(object : OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                menu.findItem(R.id.sort).setVisible(false)
                findViewById<LinearLayout>(R.id.footer).visibility=View.GONE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                menu.findItem(R.id.sort).setVisible(true)
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
                if(search!=null)
                    if(!gridForRecycler) {
                        Database.list.forEach() {
                            if ("${it.firstName} ${it.lastName}".contains(search, true))
                                searchAdapterValues.add(it)
                        }
                    }else{
                        Database.favList.forEach() {
                            if ("${it.firstName} ${it.lastName}".contains(search, true))
                                searchAdapterValues.add(it)
                        }
                    }

                adapter.setAdapterContactData(searchAdapterValues)
                adapter.notifyDataSetChanged()
                return false
            }
            override fun onQueryTextSubmit(p0: String?): Boolean {
//                Toast.makeText(this@MainActivity, "searched", Toast.LENGTH_SHORT).show()
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
            R.id.first_name -> displayContactsFragment.sortByFirstName()
            R.id.last_name-> displayContactsFragment.sortByLastName()
            R.id.dummy->{
                Database.dummy()
                Toast.makeText(this, "Dummy added.\nClick Contact button below", Toast.LENGTH_SHORT).show()
                item.setVisible(false)
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
        if(requestCode==PermissionUtils.CALL_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission allowed", Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        findViewById<Button>(R.id.fav).setTextColor(savedInstanceState.getInt("fav"))
        findViewById<Button>(R.id.contacts).setTextColor(savedInstanceState.getInt("con"))
        searchText=savedInstanceState.getString("search","")
//        Toast.makeText(this, "restore  $searchText", Toast.LENGTH_SHORT).show()


    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("grid",gridForRecycler)
        val favColor=findViewById<Button>(R.id.fav).textColors
        val contactColor=findViewById<Button>(R.id.contacts).textColors
        outState.putInt("fav",favColor.defaultColor)
        outState.putInt("con",contactColor.defaultColor)
        if(menu!!.findItem(R.id.search)!!.isActionViewExpanded){
            outState.putString("search","${(menu!!.findItem(R.id.search).actionView as SearchView).query}")
        }
    }

    private fun onclickButtons(){
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
            DBHelper(context=this,null ).writableDatabase
            favorites.setTextColor(getColor(R.color.black))
            contacts.setTextColor(getColor(R.color.blue))
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction().replace(R.id.container_for_display_contact_fragments,displayContactsFragment).commit()
        }
    }

}