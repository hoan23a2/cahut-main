package com.example.cahut.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cahut.data.model.Account

class AccountDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "account.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_ACCOUNTS = "accounts"

        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_ACCOUNTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
        onCreate(db)
    }

    fun insertAccount(account: Account): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, account.email)
            put(COLUMN_USERNAME, account.username)
            put(COLUMN_PASSWORD, account.password)
        }
        return db.insert(TABLE_ACCOUNTS, null, values)
    }

    fun getAccountByEmail(email: String): Account? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_ACCOUNTS,
            null,
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) {
            Account(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            )
        } else null
    }

    fun getAccountByEmailAndPassword(email: String, password: String): Account? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_ACCOUNTS,
            null,
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password),
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) {
            Account(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            )
        } else null
    }

    fun getAccountByUsernameAndPassword(username: String, password: String): Account? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_ACCOUNTS,
            null,
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) {
            Account(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            )
        } else null
    }

    fun getAllAccounts(): List<Account> {
        val accounts = mutableListOf<Account>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_ACCOUNTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val account = Account(
                    id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
                    email = getString(getColumnIndexOrThrow(COLUMN_EMAIL)),
                    username = getString(getColumnIndexOrThrow(COLUMN_USERNAME)),
                    password = getString(getColumnIndexOrThrow(COLUMN_PASSWORD))
                )
                accounts.add(account)
            }
        }
        cursor.close()
        return accounts
    }
} 