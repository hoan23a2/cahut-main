package com.example.cahut.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.cahut.R

fun Context.showCustomToast(message: String) {
    val inflater = LayoutInflater.from(this)
    val layout = inflater.inflate(R.layout.custom_toast, null)
    
    val textView = layout.findViewById<TextView>(R.id.toast_text)
    textView.text = message
    
    val toast = Toast(this)
    toast.duration = Toast.LENGTH_SHORT
    toast.view = layout
    toast.show()
} 