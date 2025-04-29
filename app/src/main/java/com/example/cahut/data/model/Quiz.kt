package com.example.cahut.data.model

data class PlayingQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val timeLimit: Int
)

data class QuizResult(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)

data class LeaderboardEntry(
    val id: String,
    val rank: Int,
    val username: String,
    val score: Int
) 