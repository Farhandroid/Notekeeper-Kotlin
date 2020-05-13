package tanvir.notekeepersample.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText

import com.sdsmdg.tastytoast.TastyToast
import kotlinx.android.synthetic.main.activity_note_edit.*
import kotlinx.android.synthetic.main.layout_to_inflate_in_recyclerview.*
import java.util.Calendar
import tanvir.notekeepersample.Database.DatabaseHelper
import tanvir.notekeepersample.ModelClass.NoteMC
import tanvir.notekeepersample.R

class NoteEdit : AppCompatActivity() {

    private var isItSaveButton = false
    private var whereFrom: String? = ""
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var noteBody: EditText
    private var noteTitleFromMA: String?=null
    private var noteBodyFromMA: String?=null
    private var noteDateAndTimeFromMA: String?=null

    val time: String
        get() {
            val c = Calendar.getInstance()
            val df = java.text.SimpleDateFormat("dd-MM-yyyy hh:mm:s aaa")

            return df.format(c.time)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)
        databaseHelper = DatabaseHelper(this@NoteEdit)
        noteTitleFromMA = intent.getStringExtra("note_title")
        noteBodyFromMA = intent.getStringExtra("note_body")
        noteDateAndTimeFromMA = intent.getStringExtra("note_dateAndTime")
        noteBody= noteBodyET as EditText

        if (noteTitleFromMA != null && noteBodyFromMA != null) {
            noteTitleET!!.setText(noteTitleFromMA)
            noteBody!!.setText(noteBodyFromMA)

        } else {
            noteBodyFromMA = ""
            noteTitleFromMA = ""
        }
        checkEditBoxFocus()
    }

    fun checkEditBoxFocus() {

        val sharedPreferences = getSharedPreferences("EditBoxFocus", Context.MODE_PRIVATE)
        whereFrom = sharedPreferences.getString("wheree_are_you_came_from?", "")

        if (whereFrom == "recyclerview") {
            val editor = sharedPreferences.edit()
            editor.putString("wheree_are_you_came_from?", "NOteEdit")
            editor.commit()
            dynamicButton!!.setBackgroundResource(R.drawable.pencil_icon)
            noteTitleET!!.isEnabled = false
            noteBody!!.isEnabled = false
        }
    }

    fun EditBoxFocusOn(view: View) {

        if (isItSaveButton == false) {
            noteTitleET!!.isEnabled = true
            noteBody!!.isEnabled = true
            noteBody!!.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(noteBody, InputMethodManager.SHOW_IMPLICIT)
            isItSaveButton = true
            dynamicButton!!.setBackgroundResource(R.drawable.right)
        } else {
            saveAndUpdateNote()
        }
    }

    class LineEditText// we need this constructor for LayoutInflater
    (context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {
        private val mRect: Rect
        private val mPaint: Paint
        init {
            mRect = Rect()
            mPaint = Paint()
            mPaint.style = Paint.Style.FILL_AND_STROKE
            mPaint.color = Color.BLACK
        }

        override fun onDraw(canvas: Canvas) {

            val height = height
            val line_height = lineHeight
            var count = height / line_height
            if (lineCount > count)
                count = lineCount
            val r = mRect
            val paint = mPaint
            var baseline = getLineBounds(0, r)
            for (i in 0 until count) {
                canvas.drawLine(r.left.toFloat(), (baseline + 1).toFloat(), r.right.toFloat(), (baseline + 1).toFloat(), paint)
                baseline += lineHeight
                super.onDraw(canvas)
            }

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onBackPressed() {
        super.onBackPressed()
        saveAndUpdateNote()


    }

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Asynctask For Update Note~~~~~~~~~~~~~~~~~~~*/

    inner class updateNote constructor(private var noteMC: NoteMC,
                                                      private var oldDateAndTime: String) : AsyncTask<Void, Void, Void>() {
        private var result: Boolean = false
        override fun doInBackground(vararg noteDateAndTime: Void): Void? {
            result = databaseHelper.updateNote(noteMC, oldDateAndTime)
            return null
        }

        override fun onPostExecute(args: Void?) {
            super.onPostExecute(args)
            this@NoteEdit.runOnUiThread {
                if (result)
                    TastyToast.makeText(applicationContext, "Update Success", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS)
                else
                    TastyToast.makeText(applicationContext, "Update failed", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS)
            }
        }

    }


    fun saveAndUpdateNote() {
        var title = noteTitleET!!.text.toString()
        val body = noteBody!!.text.toString()
        if (title.length == 0 && body.length > 0)
            title = "Untitled NOte"
        val noteMC = NoteMC()
        noteMC.title = title
        noteMC.body = body
        noteMC.time = time
        val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("came_from_which_activity?", "noteEdit")
        editor.commit()
        if ((title.length > 0 || body.length > 0) && whereFrom != "recyclerview") {
            val result = databaseHelper.insertDataInDatabase(noteMC)
            if (result)
                TastyToast.makeText(applicationContext, "NOte Inserted Successfully", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS)
            else
                TastyToast.makeText(applicationContext, "Insertion failed", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS)


        } else if (noteTitleFromMA != title || noteBodyFromMA != body) {
            noteDateAndTimeFromMA?.let { updateNote(noteMC, it).execute() }
        }
        val myIntent = Intent(this@NoteEdit, MainActivity::class.java)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        this@NoteEdit.startActivity(myIntent)
        overridePendingTransition(R.anim.down_in, R.anim.down_out)
        finish()

    }
}
