package com.example.timecapsule

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "time_capsule.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_CAPSULES = "capsules"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_MESSAGE = "message"
        private const val COLUMN_CREATED = "created_date"
        private const val COLUMN_UNLOCK = "unlock_date"
        private const val COLUMN_OPENED = "opened_date"
        private const val COLUMN_LOCKED = "is_locked"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_CAPSULES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_MESSAGE TEXT NOT NULL,
                $COLUMN_CREATED INTEGER NOT NULL,
                $COLUMN_UNLOCK INTEGER NOT NULL,
                $COLUMN_OPENED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_LOCKED INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CAPSULES")
        onCreate(db)
    }

    fun insertCapsule(title: String, message: String, unlockDate: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_MESSAGE, message)
            put(COLUMN_CREATED, System.currentTimeMillis())
            put(COLUMN_UNLOCK, unlockDate)
            put(COLUMN_OPENED, 0L)
            put(COLUMN_LOCKED, 1)
        }
        val id = db.insert(TABLE_CAPSULES, null, values)
        db.close()
        return id
    }

    fun getAllCapsules(): List<Capsule> {
        val capsules = mutableListOf<Capsule>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CAPSULES ORDER BY $COLUMN_UNLOCK ASC", null
        )
        cursor.use {
            while (it.moveToNext()) capsules.add(cursorToCapsule(it))
        }
        db.close()
        return capsules
    }

    fun getCapsuleById(id: Long): Capsule? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CAPSULES WHERE $COLUMN_ID = ?", arrayOf(id.toString())
        )
        var capsule: Capsule? = null
        cursor.use {
            if (it.moveToFirst()) capsule = cursorToCapsule(it)
        }
        db.close()
        return capsule
    }

    fun unlockCapsule(id: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LOCKED, 0)
            put(COLUMN_OPENED, System.currentTimeMillis())
        }
        db.update(TABLE_CAPSULES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    // Auto-unlock capsules whose date already passed (e.g. app was closed)
    fun unlockExpiredCapsules() {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LOCKED, 0)
            put(COLUMN_OPENED, System.currentTimeMillis())
        }
        db.update(
            TABLE_CAPSULES, values,
            "$COLUMN_LOCKED = 1 AND $COLUMN_UNLOCK <= ?",
            arrayOf(System.currentTimeMillis().toString())
        )
        db.close()
    }

    fun deleteCapsule(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_CAPSULES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    private fun cursorToCapsule(cursor: Cursor): Capsule {
        return Capsule(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)),
            createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED)),
            unlockDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UNLOCK)),
            openedDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OPENED)),
            isLocked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOCKED)) == 1
        )
    }
}