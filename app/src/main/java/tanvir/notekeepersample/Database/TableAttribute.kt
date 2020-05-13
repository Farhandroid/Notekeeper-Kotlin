package tanvir.notekeepersample.Database
class TableAttribute {
    fun tableCreation(): String {
        return "CREATE TABLE $TABLE_NAME( $COL_TITLE TEXT, $COL_BODY TEXT, $COL_DATE_AND_TIME TEXT PRIMARY KEY) "
    }
    companion object {
        val DATABASE_NAME = "DatabaseForNote"
        val DATABASE_VERSION = 1
        val TABLE_NAME = "NoteTable"
        val COL_TITLE = "Title"
        val COL_BODY = "Body"
        val COL_DATE_AND_TIME = "DateAndTime"
    }

}
