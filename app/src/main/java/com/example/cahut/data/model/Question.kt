package com.example.cahut.data.model

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("_id")
    val _id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val timeLimit: Int,
    val examId: String? = null,
    val type: String = "normal",
    val imageUrl: String? = null
) 