package com.example.cahut.data.repository

import android.content.Context
import com.example.cahut.data.db.AccountDatabase
import com.example.cahut.data.model.Account

class AccountRepository(context: Context) {
    private val database = AccountDatabase(context)

    fun registerAccount(email: String, username: String, password: String): Boolean {
        return try {
            val account = Account(email = email, username = username, password = password)
            database.insertAccount(account) != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun loginAccount(email: String, password: String): Account? {
        return database.getAccountByEmailAndPassword(email, password)
    }

    fun loginAccountByUsername(username: String, password: String): Account? {
        return database.getAccountByUsernameAndPassword(username, password)
    }

    fun isEmailTaken(email: String): Boolean {
        return database.getAccountByEmail(email) != null
    }
} 