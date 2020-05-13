package tanvir.notekeepersample.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import tanvir.notekeepersample.ModelClass.NoteMC

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, TableAttribute.DATABASE_NAME, null, TableAttribute.DATABASE_VERSION) {
    private var TAG="DatabaseHelperLog"
    val allData: Cursor
        get() {
            val db = this.writableDatabase
            return db.rawQuery("SELECT * FROM " + TableAttribute.TABLE_NAME, null)
        }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val tableAttribute = TableAttribute()
        val query = tableAttribute.tableCreation()
        sqLiteDatabase.execSQL(query)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
    }

    fun insertDataInDatabase(noteMC: NoteMC): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TableAttribute.COL_TITLE, noteMC.title)
        contentValues.put(TableAttribute.COL_BODY, noteMC.body)
        contentValues.put(TableAttribute.COL_DATE_AND_TIME, noteMC.time)
        Log.d(TAG,"insertDataInDatabase   title : "+noteMC.title+" body : "+noteMC.body+" date&time : "+noteMC.time );
        val result = db.insert(TableAttribute.TABLE_NAME, null, contentValues)

        if (result > 0) {
            db.close()
            return true
        } else {
            db.close()
            return false
        }
    }

    fun deleteAllData(): Boolean {
        val db = this.writableDatabase

        val result = db.delete(TableAttribute.TABLE_NAME, null, null)
        db.close()

        return result > 0
    }


    fun deleteNOteFromDatabase(timeAndDate: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TableAttribute.TABLE_NAME, TableAttribute.COL_DATE_AND_TIME + " = ?", arrayOf(timeAndDate))
        db.close()

        return if (result > 0) {
            true
        } else {
            false
        }
    }

    fun updateNote(noteMC: NoteMC, oldDateAndTime: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TableAttribute.COL_TITLE, noteMC.title)
        contentValues.put(TableAttribute.COL_BODY, noteMC.body)
        contentValues.put(TableAttribute.COL_DATE_AND_TIME, noteMC.time)

        val result = db.update(TableAttribute.TABLE_NAME, contentValues, TableAttribute.COL_DATE_AND_TIME + "= ?", arrayOf(oldDateAndTime))
        return if (result > 0) {
            true
        } else {
            false
        }
    }
}
