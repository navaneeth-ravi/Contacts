package com.example.nav_contacts

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable
import android.graphics.PorterDuff
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class ContactsDisplayFragment() : Fragment(){
    lateinit var recyler:RecyclerView

    companion object{
        const val CONTACTS=0
        const val FAVORITES=1
        var SORT_BY_FIRST_NAME=true
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view1=inflater.inflate(R.layout.fragment_display_contacts, container, false)
//        val buttonCreate=view1.findViewById<Button>(R.id.create)
//        buttonCreate.visibility=View.VISIBLE
//        addNewContactWhenClick(buttonCreate)

        recyler = view1.findViewById(R.id.recycler)
        view1.findViewById<FloatingActionButton>(R.id.edit_icon).visibility=View.VISIBLE
//        recyler.setHasFixedSize(true)

        val parent = activity as MainActivity
        recyler.layoutManager=getLayoutManager(view1)

        val adapter = MyAdapter(parent.gridForRecycler, context = context)
        val swipeGestures:ItemTouchHelper.Callback

        if(!parent.gridForRecycler)
            swipeGestures=getContactSwipeGestures(adapter)
        else
            swipeGestures=getFavoriteSwipeGestures(adapter)

        val touchHelper = ItemTouchHelper(swipeGestures)
        touchHelper.attachToRecyclerView(recyler)
        recyler.adapter=adapter

        if(savedInstanceState!=null)
            onRestore(savedInstanceState)

        onClickCreateContact(view1)

        return view1
    }
    private fun getLayoutManager(view: View):RecyclerView.LayoutManager{
        val parent = activity as MainActivity
        return if (parent.gridForRecycler) {
            view.findViewById<FloatingActionButton>(R.id.edit_icon).visibility=View.GONE
//            buttonCreate.visibility=View.GONE
            if (!(activity as MainActivity).portrait)
                GridLayoutManager(activity, 3)
            else GridLayoutManager(activity, 2)
        }
        else LinearLayoutManager(activity)
    }
    private fun onClickCreateContact(view: View){
        view.findViewById<View>(R.id.edit_icon).setOnClickListener {
            val intent=Intent(this.context,ContactCreation::class.java)
            intent.putExtra("option",ContactCreation.CREATE)
            startActivity(intent)
        }
    }

    private fun onRestore(savedInstanceState: Bundle){
        (context as MainActivity).displayContactsFragment=this
        if(!savedInstanceState.getBoolean("sort"))
            sortByLastName()
    }



    private fun addNewContactWhenClick(button: Button){
        button.visibility=View.GONE
        button.setOnClickListener {
            val intent=Intent(this.context,ContactCreation::class.java)
            intent.putExtra("option",ContactCreation.CREATE)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        val adapter=recyler.adapter as MyAdapter
        if(adapter!=null &&!(activity as MainActivity).gridForRecycler) {
            adapter.setAdapterContactData(Database.list)
            adapter.notifyDataSetChanged()
//            (recyler.adapter as MyAdapter).notifyItemRangeChanged(0,Database.list.size)
        }else if((activity as MainActivity).gridForRecycler){
            adapter.setAdapterFavoritesData(Database.favList)
            adapter.notifyDataSetChanged()
        }
    }
    fun sortByFirstName(){
        val adapter=recyler.adapter as MyAdapter
        SORT_BY_FIRST_NAME=true
        var sortedList:List<Contact>
        if(!(activity as MainActivity).gridForRecycler){
            sortedList=Database.list.sortedBy { it.firstName }
            adapter.setAdapterContactData(ArrayList(sortedList))
        }else {
            sortedList=Database.favList.sortedBy { it.firstName }
            adapter.setAdapterFavoritesData(ArrayList(sortedList))
        }
        adapter.notifyItemRangeChanged(0,sortedList.size)
    }

    fun sortByLastName(){
        val adapter=recyler.adapter as MyAdapter
        SORT_BY_FIRST_NAME=false
        var sortedList:List<Contact>
        if(!(activity as MainActivity).gridForRecycler){
            sortedList=Database.list.sortedBy { it.lastName }
            adapter.setAdapterContactData(ArrayList(sortedList))
        }else {
            sortedList=Database.favList.sortedBy { it.lastName }
//            sortedList=Database.favList.filter { it.firstName.contains("z") }
            adapter.setAdapterFavoritesData(ArrayList(sortedList))
        }
        adapter.notifyItemRangeChanged(0,sortedList.size)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("sort", SORT_BY_FIRST_NAME)
    }
    private fun getFavoriteSwipeGestures(adapter: MyAdapter):ItemTouchHelper.Callback{
        return object : SwipeGestures(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val data = (recyler.adapter as MyAdapter).values.get(viewHolder.adapterPosition)
                val dbH=Database(context)
                when (direction) {
                    ItemTouchHelper.RIGHT->{
                        val position=viewHolder.adapterPosition
                        data.favorite = false
                        adapter.values.removeAt(position)
                        dbH.update(data)
                        Snackbar.make(recyler, "", Snackbar.LENGTH_LONG)
                            .setText("${data.firstName} ${data.lastName} removed from favorites")
                            .setTextColor(Color.WHITE).setMaxInlineActionWidth(12).show()
                        adapter.notifyItemRemoved(position)
                    }
                }
                super.onSwiped(viewHolder, direction)
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
    private fun getContactSwipeGestures(adapter: MyAdapter):ItemTouchHelper.Callback{
        return object : SwipeGestures() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val data = (recyler.adapter as MyAdapter).values.get(viewHolder.adapterPosition)
                val dbH=Database(context)
                when (direction) {
                    ItemTouchHelper.LEFT -> {
//                        Snackbar.ANIMATION_MODE_FADE
                        data.favorite = true
                        dbH.update(data)
                        Snackbar.make(recyler, "", Snackbar.LENGTH_LONG)
                            .setText("${data.firstName} ${data.lastName} added to favorite")
                            .setTextColor(Color.WHITE).setMaxInlineActionWidth(12).setAnimationMode(Snackbar.ANIMATION_MODE_FADE).show()
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    ItemTouchHelper.RIGHT -> {
                        val position = viewHolder.adapterPosition
                        adapter.values.removeAt(position)
                        dbH.delete(data)
                        dbH.getAlldata()
                        adapter.notifyItemRemoved(position)
                        Snackbar.make(recyler, "", Snackbar.LENGTH_LONG)
                            .setText("${data.firstName} ${data.lastName} deleted")
                            .setTextColor(Color.WHITE)
                            .setAction("undo") {
                                adapter.values.add(position, data)
                                dbH.insert(data)
                                dbH.getAlldata()
                                adapter.notifyItemInserted(position)
                            }.show()
                    }
                }
                super.onSwiped(viewHolder, direction)
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