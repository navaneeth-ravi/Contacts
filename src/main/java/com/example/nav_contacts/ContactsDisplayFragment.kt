package com.example.nav_contacts

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class ContactsDisplayFragment : Fragment(){
    lateinit var recyler:RecyclerView
    private lateinit var contactDisplayView: View
    private lateinit var parent: MainActivity
    private lateinit var contentResolver: ContentResolver
    companion object {
        const val CONTACTS=0
        const val FAVORITES=1
        var SORT_BY_FIRST_NAME=true
        const val SORT_KEY="sort"
        private const val EMPTY_STRING="empty"
    }
    fun isRecyclerInitialized():Boolean{
        return this::recyler.isInitialized
    }
    fun setContentResolver(contentResolver: ContentResolver) {
        this.contentResolver=contentResolver
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
        return contactDisplayView
    }
    private fun initializeRecyclerView(){
        parent = activity as MainActivity
        recyler = contactDisplayView.findViewById(R.id.recycler)
        recyler.layoutManager=getLayoutManager()
        recyler.adapter=ContactAndFavoriteAdapter(parent.gridForRecycler, context = context)
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
    private val createNewContact=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode==Activity.RESULT_OK) {
            val adapter=recyler.adapter as ContactAndFavoriteAdapter
            adapter.notifyDataSetChanged()
        }
    }
    private fun onClickFloatingButtonAddContact(){
        contactDisplayView.findViewById<View>(R.id.edit_icon).setOnClickListener {
            val intent=Intent(this.context,CreateAndEditContactActivity::class.java)
            intent.putExtra(CreateAndEditContactActivity.OPTION,CreateAndEditContactActivity.CREATE)
            createNewContact.launch(intent)
        }
    }

    private fun onRestore(savedInstanceState: Bundle){
        (context as MainActivity).displayContactsFragment=this
        if (!savedInstanceState.getBoolean(SORT_KEY)) {
            sortByLastName()
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        if (!(activity as MainActivity).gridForRecycler) {
            adapter.setAdapterContactData(MainActivity.contactList)
            adapter.notifyDataSetChanged()
        } else if ((activity as MainActivity).gridForRecycler){
            adapter.setAdapterFavoritesData(MainActivity.favoriteContactList)
            adapter.notifyDataSetChanged()
        }
    }
    fun sortByFirstName(){
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        SORT_BY_FIRST_NAME=true
        val sortedList:List<ContactDataClass>
        if (!parent.gridForRecycler) {
            sortedList=MainActivity.contactList.sortedBy { it.firstName }
            adapter.setAdapterContactData(ArrayList(sortedList))
        } else {
            sortedList=MainActivity.favoriteContactList.sortedBy { it.firstName }
            adapter.setAdapterFavoritesData(ArrayList(sortedList))
        }
        adapter.notifyItemRangeChanged(0,sortedList.size)
    }

    fun sortByLastName(){
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        SORT_BY_FIRST_NAME=false
        val sortedList:List<ContactDataClass>
        if (!parent.gridForRecycler) {
            sortedList=MainActivity.contactList.sortedBy { it.lastName }
            adapter.setAdapterContactData(ArrayList(sortedList))
        } else {
            sortedList=MainActivity.favoriteContactList.sortedBy { it.lastName }
            adapter.setAdapterFavoritesData(ArrayList(sortedList))
        }
        adapter.notifyItemRangeChanged(0,sortedList.size)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SORT_KEY, SORT_BY_FIRST_NAME)
    }

    private fun getFavoriteSwipeGestures():ItemTouchHelper.Callback{
        return object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyler.adapter as ContactAndFavoriteAdapter
                val data = adapter.values[viewHolder.adapterPosition]
                when (direction) {
                    ItemTouchHelper.RIGHT->{
                        val position=viewHolder.adapterPosition
                        data.favorite = false
                        adapter.values.removeAt(position)
                        val values = ContentValues()
                        values.put(MyContentProvider.FAVORITE, 0)
                        DatabaseFunctionalities().update(values,data,contentResolver)
                        Snackbar.make(recyler, "", Snackbar.LENGTH_LONG)
                            .setText("${data.firstName} ${data.lastName} ${resources.getString(R.string.remove_from_favorite)}")
                            .setTextColor(Color.WHITE).setMaxInlineActionWidth(12).show()
                        adapter.notifyItemRemoved(position)
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
                ).addSwipeRightActionIcon(R.drawable.favorite).addSwipeRightLabel(resources.getString(R.string.un_favorite)).create().decorate()

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

    private fun getContactSwipeGestures():ItemTouchHelper.Callback{
        return object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=(recyler.adapter as ContactAndFavoriteAdapter)
                val data = adapter.values.get(viewHolder.adapterPosition)
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        data.favorite = true
                        if(!MainActivity.favoriteContactList.contains(data)) {
                            MainActivity.favoriteContactList.add(data)
                        }
                        val values = ContentValues()
                        values.put(MyContentProvider.FAVORITE, 1)
                        DatabaseFunctionalities().update(values,data,contentResolver)
                        Toast.makeText(context, "${data.firstName} ${data.lastName} ${resources.getString(R.string.add_to_favorite)}", Toast.LENGTH_SHORT).show()
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    ItemTouchHelper.RIGHT -> {
                        val position = viewHolder.adapterPosition
                        adapter.values.removeAt(position)
                        DatabaseFunctionalities().delete(data.dbID.toString(),contentResolver)
                        adapter.notifyItemRemoved(position)
                        val snackBar=Snackbar.make(recyler, "${data.firstName} ${data.lastName} ${resources.getString(R.string.deleted)}", Snackbar.LENGTH_SHORT)
                            .setTextColor(Color.WHITE)
                            .setAction(resources.getString(R.string.undo)) {
                                adapter.values.add(position, data)
                                val values=ContentValues()
                                values.put(MyContentProvider.ID,data.dbID)
                                values.put(MyContentProvider.FIRST_NAME,data.firstName)
                                values.put(MyContentProvider.LAST_NAME,data.lastName)
                                try {
                                    values.put(MyContentProvider.NUMBER1, data.number[0])
                                    values.put(MyContentProvider.NUMBER2, data.number[1])
                                }catch (exception:IndexOutOfBoundsException){
                                    if(data.number.size>0){
                                        values.put(MyContentProvider.NUMBER1, data.number[0])
                                        values.put(MyContentProvider.NUMBER2, EMPTY_STRING)
                                    }else{
                                        values.put(MyContentProvider.NUMBER2, EMPTY_STRING)
                                        values.put(MyContentProvider.NUMBER1, EMPTY_STRING)
                                    }
                                }
                                values.put(MyContentProvider.EMAIL,data.email)
                                values.put(MyContentProvider.FAVORITE,data.favorite)
                                values.put(MyContentProvider.PROFILE_IMAGE, "${data.firstName}${data.lastName}${resources.getString(R.string.image_format)}")
                                DatabaseFunctionalities().insert(values,contentResolver)
                                adapter.notifyItemInserted(position)
                            }
                        val snackBarLayout = snackBar.view as SnackbarLayout
                        snackBarLayout.setPadding(100, 0, 150, 0)
                        snackBar.show()
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
                    .addSwipeLeftActionIcon(R.drawable.favorite).addSwipeLeftLabel(resources.getString(R.string.favorite))
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
