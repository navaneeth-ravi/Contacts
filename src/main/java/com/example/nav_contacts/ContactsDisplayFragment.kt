package com.example.nav_contacts

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.Display
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ContactsDisplayFragment() : Fragment(){
    lateinit var recyler:RecyclerView
    private lateinit var contactDisplayView: View
    private lateinit var parent: MainActivity
    companion object{
        const val CONTACTS=0
        const val FAVORITES=1
        var SORT_BY_FIRST_NAME=true
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contactDisplayView=inflater.inflate(R.layout.fragment_display_contacts, container, false)
        initializeRecyclerView()
        if(savedInstanceState!=null){
            onRestore(savedInstanceState)
        }
        onClickCreateContact()
        return contactDisplayView
    }
    private fun initializeRecyclerView(){
        parent = activity as MainActivity
        recyler = contactDisplayView.findViewById(R.id.recycler)
        recyler.layoutManager=getLayoutManager()
        recyler.adapter=ContactAndFavoriteAdapter(parent.gridForRecycler, context = context)
        val swipeGestures:ItemTouchHelper.Callback =
            if(!parent.gridForRecycler){
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
    private fun onClickCreateContact(){
        contactDisplayView.findViewById<View>(R.id.edit_icon).setOnClickListener {
            val intent=Intent(this.context,CreateAndEditContactActivity::class.java)
            intent.putExtra("option",CreateAndEditContactActivity.CREATE)
            startActivity(intent)
        }
    }

    private fun onRestore(savedInstanceState: Bundle){
        (context as MainActivity).displayContactsFragment=this
        if(!savedInstanceState.getBoolean("sort")) {
            sortByLastName()
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        if(adapter!=null &&!(activity as MainActivity).gridForRecycler) {
            adapter.setAdapterContactData(Database.list)
            adapter.notifyDataSetChanged()
        }else if((activity as MainActivity).gridForRecycler){
            adapter.setAdapterFavoritesData(Database.favList)
            adapter.notifyDataSetChanged()
        }
    }
    fun sortByFirstName(){
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        SORT_BY_FIRST_NAME=true
        val sortedList:List<ContactDataClass>
        if(!parent.gridForRecycler){
            sortedList=Database.list.sortedBy { it.firstName }
            adapter.setAdapterContactData(ArrayList(sortedList))
        }else {
            sortedList=Database.favList.sortedBy { it.firstName }
            adapter.setAdapterFavoritesData(ArrayList(sortedList))
        }
        adapter.notifyItemRangeChanged(0,sortedList.size)
    }

    fun sortByLastName(){
        val adapter=recyler.adapter as ContactAndFavoriteAdapter
        SORT_BY_FIRST_NAME=false
        val sortedList:List<ContactDataClass>
        if(!parent.gridForRecycler){
            sortedList=Database.list.sortedBy { it.lastName }
            adapter.setAdapterContactData(ArrayList(sortedList))
        }else {
            sortedList=Database.favList.sortedBy { it.lastName }
            adapter.setAdapterFavoritesData(ArrayList(sortedList))
        }
        adapter.notifyItemRangeChanged(0,sortedList.size)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("sort", SORT_BY_FIRST_NAME)
    }
    private fun getFavoriteSwipeGestures():ItemTouchHelper.Callback{
        return object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=(recyler.adapter as ContactAndFavoriteAdapter)
                val data = adapter.values.get(viewHolder.adapterPosition)
                when (direction) {
                    ItemTouchHelper.RIGHT->{
                        val position=viewHolder.adapterPosition
                        data.favorite = false
                        adapter.values.removeAt(position)
                        GlobalScope.launch {
                            Database.update(data)
                        }
                        Snackbar.make(recyler, "", Snackbar.LENGTH_LONG)
                            .setText("${data.firstName} ${data.lastName} removed from favorites")
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
                ).addSwipeRightActionIcon(R.drawable.favorite).addSwipeRightLabel("Un Favorite").create().decorate()

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

//            @SuppressLint("RestrictedApi")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=(recyler.adapter as ContactAndFavoriteAdapter)
                val data = adapter.values.get(viewHolder.adapterPosition)
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        data.favorite = true
                        GlobalScope.launch {
                            Database.update(data)
                        }
                        Toast.makeText(context, "${data.firstName} ${data.lastName} added to favorite", Toast.LENGTH_SHORT).show()
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    ItemTouchHelper.RIGHT -> {
                        val position = viewHolder.adapterPosition
                        adapter.values.removeAt(position)
                        GlobalScope.launch {
                            Database.delete(data)
                            Database.getAlldata()
                        }
                        adapter.notifyItemRemoved(position)
                        val snackbar=Snackbar.make(recyler, "${data.firstName} ${data.lastName} deleted", Snackbar.LENGTH_SHORT)
                            .setTextColor(Color.WHITE)
                            .setAction("undo") {
                                adapter.values.add(position, data)
                                GlobalScope.launch {
                                    Database.insert(data)
                                    Database.getAlldata()
                                }
                                adapter.notifyItemInserted(position)
                            }
                        val snackbarLayout = snackbar.view as SnackbarLayout
//                        snackbarLayout.setBackgroundResource(R.drawable.snack_layout)
                        snackbarLayout.setPadding(100, 0, 150, 0)
//                        snackbar.setMaxInlineActionWidth(100)
                        snackbar.show()
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
                    .addSwipeRightLabel("Delete").setSwipeRightLabelColor(Color.WHITE)
                    .setSwipeRightActionIconTint(Color.WHITE)
                    .addSwipeLeftActionIcon(R.drawable.favorite).addSwipeLeftLabel("Favorite")
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






//val swipeGestures= object :SwipeGestures(){
//    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        when(direction){
//            ItemTouchHelper.LEFT->{
//                adapter.values.removeAt(viewHolder.adapterPosition)
//                adapter.notifyDataSetChanged()
//            }
//        }
//        super.onSwiped(viewHolder, direction)
//    }
//
//    override fun onChildDraw(
//        c: Canvas,
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        dX: Float,
//        dY: Float,
//        actionState: Int,
//        isCurrentlyActive: Boolean
//    ) {
////                background = new ColorDrawable(Color.WHITE)
////                val aa=context?.let { ContextCompat.getDrawable(it,R.color.teal_700) }
////                background= ColorDrawable(aa as Int)
//        val itemView=viewHolder.itemView
////                val icon=BitmapFactory.decodeResource((context as MainActivity).resources,R.mipmap.ic_launcher)
////                val p=Paint()
//////                p.setARGB(255, 255, 0, 0);
////                p.color=Color.RED
////                c.drawRect(itemView.left.toFloat(),itemView.top.toFloat(),dX,itemView.bottom.toFloat(),p)
//
////                if(icon===null) Toast.makeText(context, "swipe", Toast.LENGTH_SHORT).show()
////                else
////                    c.drawBitmap(icon, itemView.left.toFloat() +(16.0f * 16 + 0.5f).toInt(),
////                        itemView.top.toFloat()+itemView.bottom.toFloat()-itemView.top.toFloat(), p)
//
//
//        val context:Context=(context as MainActivity)
//        background=ColorDrawable(Color.RED)
//        xMark= ContextCompat.getDrawable(context,R.drawable.star)!!
//        xMark.colorFilter=PorterDuffColorFilter(Color.RED,PorterDuff.Mode.SRC)
//        background.setBounds(itemView.right +dX.toInt(),itemView.top,itemView.right,itemView.bottom)
//        background.draw(c)
////                val xMarkMargin:Int =14
//////                    this@MainActivity.getResources().getDimension(R.dimen.ic_clear_margin) as Int
////                val itemHeight = itemView.bottom - itemView.top
////                val intrinsicWidth = xMark.intrinsicWidth
////                val intrinsicHeight = xMark.intrinsicWidth
////                val xMarkLeft: Int = itemView.right - xMarkMargin - intrinsicWidth
////                val xMarkRight: Int = itemView.right - xMarkMargin
////                val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
////                val xMarkBottom = xMarkTop + intrinsicHeight
////                xMark.setBounds(xMarkLeft,xMarkTop,xMarkRight,xMarkBottom)
////                xMark.draw(c)
//
//        var iconSize=0
//        var iconHorizontalMargin=5
//        val icon = ContextCompat.getDrawable(recyclerView.context,R.drawable.mail_icon)
//        if (icon != null) {
//            Toast.makeText(context, "make", Toast.LENGTH_SHORT).show()
//            iconSize = icon.intrinsicHeight
//            val halfIcon: Int = iconSize / 2
//            val top =
//                viewHolder.itemView.top + ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
//            icon.setBounds(
//                viewHolder.itemView.left + iconHorizontalMargin,
//                top,
//                viewHolder.itemView.left + iconHorizontalMargin + icon.intrinsicWidth,
//                top + icon.intrinsicHeight
//            )
////                    if (swipeRightActionIconTint != null) icon.setColorFilter(
////                        swipeRightActionIconTint,
////                        PorterDuff.Mode.SRC_IN
////                    )
//            icon.draw(c)
//        }
//
//        super.onChildDraw(
//            c,
//            recyclerView,
//            viewHolder,
//            dX,
//            dY,
//            actionState,
//            isCurrentlyActive
//        )
//    }
//}