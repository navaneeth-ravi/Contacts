package com.example.nav_contacts

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

class MyContentProvider : ContentProvider() {
    private var db: SQLiteDatabase? = null
    companion object{
        const val TABLE_NAME = "contacts"
        const val ID = "id"
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val NUMBER1="number1"
        const val NUMBER2="number2"
        const val EMAIL="email"
        const val FAVORITE="favorite"
        const val PROFILE_IMAGE="image"

        private const val PROVIDER_NAME="com.nav.contact.provider"
        private const val URL="content://$PROVIDER_NAME/users"
        val CONTENT_URI: Uri = Uri.parse(URL)
        const val uriCode = 1
        var uriMatcher: UriMatcher? = null
        private val values: HashMap<String, String>? = null
        const val DATABASE_NAME = "UserDB"
        const val DATABASE_VERSION = 1
        const val CREATE_DB_TABLE = ("CREATE TABLE " +TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY, " +
                FIRST_NAME + " TEXT," +
                LAST_NAME + " TEXT," +
                NUMBER1 + " TEXT," +
                NUMBER2 + " TEXT," +
                FAVORITE + " TEXT," +
                EMAIL + " TEXT," +
                PROFILE_IMAGE + " TEXT" +")")

        init {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher!!.addURI(
                PROVIDER_NAME,
                "users",
                uriCode
            )
        }
    }
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val count: Int = when(uriMatcher!!.match(uri)){
            uriCode->db!!.delete(TABLE_NAME,selection,selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri,null)
        return count
    }
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val count: Int = when(uriMatcher!!.match(uri)){
            uriCode->db!!.update(TABLE_NAME,values,selection,selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri,null)
        return count
    }
    override fun getType(uri: Uri): String {
        return when (uriMatcher!!.match(uri)) {
            uriCode -> "vnd.android.cursor.dir/users"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val rowId=db!!.insert(TABLE_NAME,"",values)
        if(rowId > 0){
            val urI = ContentUris.withAppendedId(CONTENT_URI,rowId)
            context!!.contentResolver.notifyChange(urI,null)
            return urI
        }
        throw SQLiteException("Failed to add a record into $uri")
    }

    override fun onCreate(): Boolean {
        val context = context
        val dbHelper = context?.let { DatabaseHelper(it) }
        db = dbHelper?.writableDatabase
        return db != null
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = TABLE_NAME
        when ( uriMatcher!!.match(uri) ) {
            uriCode-> {
                queryBuilder.projectionMap = values
            }
            else->{
                throw IllegalArgumentException( "Unknown URI $uri" )
            }
        }
        val cursor=queryBuilder.query(db,projection,selection,selectionArgs,null,null,sortOrder)
        cursor.setNotificationUri(context!!.contentResolver,uri)
        return cursor
    }

    private class DatabaseHelper(context: Context?) : SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(
            db: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

}