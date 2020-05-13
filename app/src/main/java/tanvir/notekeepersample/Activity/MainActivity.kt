package tanvir.notekeepersample.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.MenuItemCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sdsmdg.tastytoast.TastyToast
import java.util.ArrayList
import java.util.Collections
import tanvir.notekeepersample.Database.DatabaseHelper
import tanvir.notekeepersample.Database.TableAttribute
import tanvir.notekeepersample.ModelClass.NoteMC
import tanvir.notekeepersample.R

import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private var adapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var databaseHelper: DatabaseHelper
    private var appbar_menu: Menu? = null
    private var was_it_in_searchview = false
    private var entered_in_pause = false
    private var entered_in_restart = false
    private val view: View? = null
    private val p = Paint()
    private var noteTitleList = ArrayList<String>()
    private var noteDateTimeList = ArrayList<String>()
    private var noteBodyList = ArrayList<String>()
    private var TAG="MainActivityLog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        databaseHelper = DatabaseHelper(this)
        val checkLockResult = checkLockResult()
        Log.d(TAG,"checkLockResult : "+checkLockResult);
        if (!checkLockResult) {
            val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("came_from_which_activity?", "")
            editor.commit()
            initSwipe()
            setSupportActionBar(toolbarlayoutinmainactivity as Toolbar?)
            actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbarlayoutinmainactivity as Toolbar?, R.string.drawer_open, R.string.drawer_close)
            drawerLayout!!.addDrawerListener(actionBarDrawerToggle)

            val nav_Menu = navigationView!!.menu
            nav_Menu.findItem(R.id.HOmeId).isChecked = true

            navigationView!!.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.Setting -> {
                        val myIntent = Intent(this@MainActivity, SettingsActivity::class.java)

                        supportActionBar!!.title = "Setting...."
                        item.isChecked = true
                        drawerLayout!!.closeDrawers()
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        this@MainActivity.startActivity(myIntent)
                        overridePendingTransition(R.anim.left_in, R.anim.left_out)
                        finish()
                    }
                }
                true
            }
            showNoteList().execute()
        } else {
            startLoginActivity()
        }
    }


    fun clickOnFloatingActionButton(view: View) {
        val myIntent = Intent(this@MainActivity, NoteEdit::class.java)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        this@MainActivity.startActivity(myIntent)
        overridePendingTransition(R.anim.up_in, R.anim.up_out)
        finish()
    }


    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        appbar_menu = menu
        menuInflater.inflate(R.menu.menu_items, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(menuItem) as SearchView
        searchView.setOnQueryTextListener(this as SearchView.OnQueryTextListener)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        ///floatBtton.hide();
        was_it_in_searchview = true
        val searchedTitle = ArrayList<String>()
        val searchedDate = ArrayList<String>()
        val searchedBOdy = ArrayList<String>()
        for (i in noteTitleList.indices) {
            if (noteTitleList[i].toLowerCase().contains(newText.toLowerCase())) {
                searchedTitle.add(noteTitleList[i])
                searchedDate.add(noteDateTimeList[i])
                searchedBOdy.add(noteBodyList[i])
            }
        }
        updateRecyclerView(searchedTitle, searchedBOdy, searchedDate)
        return true
    }

    private inner class showNoteList : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg voids: Void): Void? {
            val res = databaseHelper.allData
            Log.d(TAG,"showNoteList "+res.count)
            if (res.count == 0) {
                ///Toast.makeText(MainActivity.this, "Error"+"Not Found", Toast.LENGTH_SHORT).show();

            } else {

                while (res.moveToNext()) {
                    noteTitleList.add(res.getString(res.getColumnIndex(TableAttribute.COL_TITLE)))
                    noteBodyList.add(res.getString(res.getColumnIndex(TableAttribute.COL_BODY)))
                    noteDateTimeList.add(res.getString(res.getColumnIndex(TableAttribute.COL_DATE_AND_TIME)))
                }
            }
            Collections.reverse(noteTitleList)
            Collections.reverse(noteDateTimeList)
            Collections.reverse(noteBodyList)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            ///Log.d(TAG,"onPostExecute")
            Log.d(TAG,"onPostExecute "+noteTitleList.size)
           updateRecyclerView(noteTitleList, noteBodyList, noteDateTimeList)
        }
    }


    fun updateRecyclerView(noteTitleList: ArrayList<String>, noteBodyList: ArrayList<String>, noteDateTimeList: ArrayList<String>
    ) {
        recyclerView.apply {
            Log.d(TAG,"updateRecyclerView : "+noteTitleList.size)
            layoutManager=LinearLayoutManager(this@MainActivity)
            adapter = RecyclerAdapter(this@MainActivity, noteTitleList, noteBodyList, noteDateTimeList)
        }

    }


    /*~~~~~~~~~~~~~~~~~~~~~~~~Recycler Adapter~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    inner class RecyclerAdapter(private var context: Context,
                                noteTitleList: ArrayList<String>,
                                noteBodyList: ArrayList<String>,
                                noteDateTimeList: ArrayList<String>) : RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>() {

        private var activity: Activity? = null
        private var noteTitleList = ArrayList<String>()
        private var noteBodyList = ArrayList<String>()
        private var noteDateTimeList = ArrayList<String>()

        init {
            Log.d(TAG,"init RecyclerAdapter: ")
            this.noteTitleList = noteTitleList
            this.noteDateTimeList = noteDateTimeList
            this.noteBodyList = noteBodyList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_to_inflate_in_recyclerview, parent, false)
            Log.d(TAG,"onCreateViewHolder ")
            return RecyclerViewHolder(view, context, noteTitleList, noteBodyList, noteDateTimeList)
        }

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            Log.d(TAG,"onBindViewHolder "+ noteTitleList[position])

            holder.textView1.text = noteTitleList[position]
            holder.textView2.text = noteDateTimeList[position]
            var s = ""
            for (i in noteTitleList.indices) {
                s += noteTitleList[0]
            }
        }

        override fun getItemCount(): Int {
            Log.d(TAG,"getItemCount "+ noteTitleList.size)
            return noteTitleList.size
        }


        inner class RecyclerViewHolder(view: View, private var context: Context, private var noteTitleList: ArrayList<String>,
                                       noteBodyLIst: ArrayList<String>,
                                       private var noteDateTimeList: ArrayList<String>) : RecyclerView.ViewHolder(view), View.OnClickListener {
            internal var textView1: TextView
            internal var textView2: TextView

            init {
                Log.d(TAG,"RecyclerViewHolder ")
                textView1 = view.findViewById<View>(R.id.noteTitle) as TextView
                textView2 = view.findViewById<View>(R.id.noteDateAndTime) as TextView
                textView1.setOnClickListener(this)
                textView2.setOnClickListener(this)
                textView1.setOnLongClickListener {
                    val position = adapterPosition
                    val s = noteDateTimeList[position]
                    deleteNote().execute(s)
                    noteTitleList.removeAt(position)
                    noteDateTimeList.removeAt(position)
                    notifyItemRemoved(position)
                    true
                }
            }

            override fun onClick(view: View) {
                val position = adapterPosition
                val sharedPreferences = getSharedPreferences("EditBoxFocus", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("wheree_are_you_came_from?", "recyclerview")
                editor.commit()

                val intent = Intent(this.context, NoteEdit::class.java)
                intent.putExtra("note_title", noteTitleList[position])
                intent.putExtra("note_body", noteBodyList[position])
                intent.putExtra("note_dateAndTime", noteDateTimeList[position])
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                this.context.startActivity(intent)
                finish()

            }
        }
    }

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Asynctask For Delete Note~~~~~~~~~~~~~~~~~~~*/

    private inner class deleteNote : AsyncTask<String, Void, Void>() {
        private var result: Boolean = false
        private lateinit var s: String
        override fun doInBackground(vararg noteDateAndTime: String): Void? {
            s = noteDateAndTime[0]
            result = databaseHelper.deleteNOteFromDatabase(s)
            return null
        }
        override fun onPostExecute(args: Void?) {
            this@MainActivity.runOnUiThread {
                if (!result)
                    TastyToast.makeText(applicationContext, "Delete failed from database", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val result = checkLockResult()
        if (result && (entered_in_pause || entered_in_restart)) {
            entered_in_pause = false
            entered_in_restart = false
            startLoginActivity()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (was_it_in_searchview) {
            was_it_in_searchview = false
            floattingButton!!.show()
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            if (was_it_in_searchview) {
                floattingButton!!.show()
                was_it_in_searchview = false
            }
        }
        return true
    }

    fun checkLockResult(): Boolean {
        val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
        val checkPassword = sharedPreferences.getString("is_it_lock_in_settings?", "")
        val checkActivity = sharedPreferences.getString("came_from_which_activity?", "")

        return !(!checkPassword!!.contains("yes") ||
                checkActivity!!.isNotEmpty() ||
                checkActivity.isEmpty() &&
                !checkPassword.contains("yes"))
    }

    fun startLoginActivity() {
        val myIntent = Intent(this@MainActivity, LoginActivity::class.java)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        this@MainActivity.startActivity(myIntent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        entered_in_pause = true
    }

    override fun onRestart() {
        super.onRestart()
        entered_in_restart = true
    }


    private fun initSwipe() {

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT) {
                    val titleToDelete = noteTitleList[position]
                    val bodyToDelete = noteBodyList[position]
                    val dateAndTimeToDelete = noteDateTimeList[position]
                    noteBodyList.removeAt(position)
                    noteTitleList.removeAt(position)
                    noteDateTimeList.removeAt(position)
                    ///adapter!!.notifyItemRemoved(position)
                    adapter!!.notifyDataSetChanged();
                    Snackbar.make(viewHolder.itemView, "$titleToDelete was removed", Snackbar.LENGTH_LONG).setAction("UNDO") {
                        noteTitleList.add(position, titleToDelete)
                        noteBodyList.add(position, bodyToDelete)
                        noteDateTimeList.add(position, dateAndTimeToDelete)
                        insertInLeftAndRightSwapAfterUndo(titleToDelete, bodyToDelete, dateAndTimeToDelete)
                        adapter!!.notifyItemInserted(position)
                        adapter!!.notifyDataSetChanged()
                    }.show()
                    deleteNote().execute(dateAndTimeToDelete)
                } else {
                    val titleToDelete = noteTitleList[position]
                    val bodyToDelete = noteBodyList[position]
                    val dateAndTimeToDelete = noteDateTimeList[position]

                    noteBodyList.removeAt(position)
                    noteTitleList.removeAt(position)
                    noteDateTimeList.removeAt(position)
                    adapter!!.notifyItemRemoved(position)

                    Snackbar.make(viewHolder.itemView, "$titleToDelete was removed", Snackbar.LENGTH_LONG).setAction("UNDO") {
                        noteTitleList.add(position, titleToDelete)
                        noteBodyList.add(position, bodyToDelete)
                        noteDateTimeList.add(position, dateAndTimeToDelete)
                        insertInLeftAndRightSwapAfterUndo(titleToDelete, bodyToDelete, dateAndTimeToDelete)
                        adapter!!.notifyItemInserted(position)
                        adapter!!.notifyDataSetChanged()
                    }.show()
                    deleteNote().execute(dateAndTimeToDelete)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                val icon: Bitmap
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    if (dX > 0) {
                        p.color = Color.parseColor("#D32F2F")
                        val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = BitmapFactory.decodeResource(resources, R.drawable.ic_delete_white)
                        val icon_dest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    } else {
                        p.color = Color.parseColor("#D32F2F")
                        val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = BitmapFactory.decodeResource(resources, R.drawable.ic_delete_white)
                        val icon_dest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    fun insertInLeftAndRightSwapAfterUndo(titleToDelete: String, bodyToDelete: String, dateAndTimeToDelete: String) {
        val noteMC = NoteMC()
        noteMC.title = titleToDelete
        noteMC.body = bodyToDelete
        noteMC.time = dateAndTimeToDelete
        val result = databaseHelper.insertDataInDatabase(noteMC)
    }

}



