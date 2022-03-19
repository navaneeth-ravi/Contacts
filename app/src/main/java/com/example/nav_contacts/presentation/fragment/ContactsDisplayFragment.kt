package com.example.nav_contacts.presentation.fragment

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nav_contacts.*
import com.example.nav_contacts.data.db.local_db.DatabaseFunctionalities
import com.example.nav_contacts.data.db.local_db.MyContentProvider
import com.example.nav_contacts.domain.entity.ContactDataClass
import com.example.nav_contacts.presentation.adapter.ContactAndFavoriteAdapter
import com.example.nav_contacts.presentation.activity.CreateAndEditContactActivity
import com.example.nav_contacts.presentation.activity.MainActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_display_contacts.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsDisplayFragment : Fragment() {
    lateinit var recyler:RecyclerView
    private lateinit var contactDisplayView: View
    private lateinit var parent: MainActivity
    companion object {
        const val CONTACTS=0
        const val FAVORITES=1
        var SORT_BY_FIRST_NAME=true
        const val SORT_KEY="sort"
        const val EMPTY_STRING="empty"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        contactDisplayView=inflater.inflate(R.layout.fragment_display_contacts, container, false)
        initializeRecyclerView()
        if (savedInstanceState!=null) {
            onRestore(savedInstanceState)
        }
        onClickFloatingButtonAddContact()
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        if (parent.gridForRecycler){
            adapter.setAdapterContactData( parent.favoriteList )
        } else if (!parent.gridForRecycler) {
            adapter.setAdapterContactData(parent.contactList)
        }
        return contactDisplayView
    }
    private fun initializeRecyclerView(){
        parent = activity as MainActivity
        recyler = contactDisplayView.findViewById(R.id.recycler)
        recyler.layoutManager=getLayoutManager()
        recyler.adapter= ContactAndFavoriteAdapter(parent.gridForRecycler, context = context)
        recycler?.setHasFixedSize(true)
        val swipeGestures:ItemTouchHelper.Callback =
            if (!parent.gridForRecycler) {
                getContactSwipeGestures()
            } else {
                getFavoriteSwipeGestures()
            }
        ItemTouchHelper(swipeGestures).attachToRecyclerView(recyler)
    }
    private fun getLayoutManager():RecyclerView.LayoutManager{
        contactDisplayView.findViewById<FloatingActionButton>(R.id.edit_icon).visibility=View.VISIBLE
        return if (parent.gridForRecycler) {
            contactDisplayView.findViewById<FloatingActionButton>(R.id.edit_icon).visibility=View.GONE
            if (!parent.portrait) {
                GridLayoutManager(activity, 3)
            }
            else{
                GridLayoutManager(activity, 2)
            }
        }
        else {
            LinearLayoutManager(activity)
        }
    }
    private val doCreateNewContact=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode==Activity.RESULT_OK) {
            GlobalScope.launch(Dispatchers.IO) {
                val cursor= DatabaseFunctionalities.getAllContactDataFromDatabase()
                withContext(Dispatchers.Main){
                    updateAdapterData(cursor)
                }
            }
        }
    }
    private fun updateAdapterData( cursor:Cursor? ){
        parent.getAllContacts(cursor)
        parent.contactList.sortBy { it.firstName.uppercase() }
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        for(i in 0 until parent.contactList.size){
            if(adapter.values[i].dbID!= parent.contactList[i].dbID){
                adapter.values.add(i,parent.contactList[i])
                adapter.notifyItemInserted(i)
            }
        }
    }
    private fun onClickFloatingButtonAddContact(){
        contactDisplayView.findViewById<View>(R.id.edit_icon).setOnClickListener {
            createNewContact()
        }
    }
    private fun createNewContact(){
        val intent=Intent(this.context, CreateAndEditContactActivity::class.java)
        intent.putExtra(CreateAndEditContactActivity.OPTION, CreateAndEditContactActivity.CREATE)
        doCreateNewContact.launch(intent)
    }
    private fun onRestore(savedInstanceState: Bundle){
        (context as MainActivity).displayContactsFragment=this
        if (!savedInstanceState.getBoolean(SORT_KEY)) {
            sortByLastName()
        }
    }

    fun sortByFirstName(){
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        SORT_BY_FIRST_NAME =true
        val sortedList:List<ContactDataClass>
        if (!parent.gridForRecycler) {
            sortedList=parent.contactList.sortedBy { it.firstName.uppercase() }
            adapter.setAdapterContactData(ArrayList(sortedList))
        } else {
            sortedList=parent.favoriteList.sortedBy { it.firstName.uppercase() }
            adapter.setAdapterContactData(ArrayList(sortedList))
        }
        adapter.notifyItemRangeChanged(0,sortedList.size)
    }

    fun sortByLastName(){
        val adapter = recyler.adapter as ContactAndFavoriteAdapter
        SORT_BY_FIRST_NAME = false
        val sortedList:List<ContactDataClass>
        if (!parent.gridForRecycler) {
            sortedList = parent.contactList.sortedBy { it.lastName.uppercase() }
            adapter.setAdapterContactData(ArrayList(sortedList))
        } else {
            sortedList = parent.favoriteList.sortedBy { it.lastName.uppercase() }
            adapter.setAdapterContactData(ArrayList(sortedList))
        }
        adapter.notifyItemRangeChanged(0,sortedList.size)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SORT_KEY, SORT_BY_FIRST_NAME)
    }
    private fun removeContactFromFavoriteGesture(viewHolder: RecyclerView.ViewHolder){
        val adapter = recyler.adapter as ContactAndFavoriteAdapter
        val data = adapter.values[viewHolder.adapterPosition]
        val position=viewHolder.adapterPosition
        data.favorite = false
        adapter.values.removeAt(position)
        val values = ContentValues()
        values.put(MyContentProvider.FAVORITE, 0)
        DatabaseFunctionalities.update(values, data.dbID)
        Snackbar.make(recyler, "", Snackbar.LENGTH_LONG)
            .setText(data.firstName+" "+data.lastName+" "+resources.getString(R.string.remove_from_favorite))
            .setTextColor(Color.WHITE).setMaxInlineActionWidth(12).show()
        adapter.notifyItemRemoved(position)
    }
    private fun getFavoriteSwipeGestures():ItemTouchHelper.Callback{
        return object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction==ItemTouchHelper.RIGHT) {
                    removeContactFromFavoriteGesture(viewHolder)
                }
            }
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
            override fun onChildDrawOver(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder?,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                ).addSwipeRightActionIcon(R.drawable.favorite).addSwipeRightLabel(resources.getString(
                    R.string.un_favorite
                )).create().decorate()

                super.onChildDrawOver(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
    }
    private fun addContactToFavoriteGesture(viewHolder: RecyclerView.ViewHolder){
        val adapter=(recyler.adapter as ContactAndFavoriteAdapter)
        val data = adapter.values[viewHolder.adapterPosition]
        data.favorite = true
        if(!parent.favoriteList.contains(data)) {
            parent.favoriteList.add(data)
            parent.favoriteList.sortBy { it.firstName.uppercase() }
        }
        val values = ContentValues()
        values.put(MyContentProvider.FAVORITE, 1)
        DatabaseFunctionalities.update(values, data.dbID)
        Toast.makeText(context, data.firstName+" "+data.lastName+" "+resources.getString(R.string.add_to_favorite), Toast.LENGTH_SHORT).show()
        adapter.notifyItemChanged(viewHolder.adapterPosition)
    }
    private fun deleteContactGesture(viewHolder: RecyclerView.ViewHolder){
        val adapter=(recyler.adapter as ContactAndFavoriteAdapter)
        val data = adapter.values[viewHolder.adapterPosition]
        val position = viewHolder.adapterPosition
        adapter.values.removeAt(position)
        parent.contactList.removeIf { it.dbID==data.dbID }
        parent.favoriteList.removeIf { it.dbID==data.dbID }
        DatabaseFunctionalities.delete(data.dbID.toString())
        adapter.notifyItemRemoved(position)
        val snackBar=Snackbar.make(recyler, data.firstName+" "+data.lastName+" "+resources.getString(
            R.string.deleted
        ), Snackbar.LENGTH_SHORT)
            .setTextColor(Color.WHITE)
            .setAction(resources.getString(R.string.undo)) {
                undoDeletedContact(position,data)
            }
        val snackBarLayout = snackBar.view as SnackbarLayout
        snackBarLayout.setPadding(100, 0, 150, 0)
        snackBar.show()
    }
    private fun undoDeletedContact(position:Int,data: ContactDataClass){
        val adapter=(recyler.adapter as ContactAndFavoriteAdapter)
        adapter.values.add(position, data)
        adapter.notifyItemInserted(position)
        parent.favoriteList.add(data)
        parent.favoriteList.sortBy { it.firstName.uppercase() }
        val values= ContactDataClass.getContentValuesForContact(data)
        DatabaseFunctionalities.insert(values)
    }
    private fun getContactSwipeGestures():ItemTouchHelper.Callback{
        return object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        addContactToFavoriteGesture(viewHolder)
                    }
                    ItemTouchHelper.RIGHT -> {
                        deleteContactGesture(viewHolder)
                    }
                }
            }
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onChildDrawOver(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder?,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeRightActionIcon(R.drawable.delete_sweep)
                    .addSwipeRightBackgroundColor(0xffC40B0B.toInt())
                    .addSwipeRightLabel(resources.getString(R.string.delete)).setSwipeRightLabelColor(Color.WHITE)
                    .setSwipeRightActionIconTint(Color.WHITE)
                    .addSwipeLeftActionIcon(R.drawable.favorite).addSwipeLeftLabel(resources.getString(
                        R.string.favorite
                    ))
                    .create().decorate()

                super.onChildDrawOver(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

        }
    }
}
