package com.example.cahut.data.repository

import android.content.Context
import com.example.cahut.data.api.ApiService
import com.example.cahut.data.api.CreateQuestionRequest
import com.example.cahut.data.api.EditQuestionRequest
import com.example.cahut.data.api.RetrofitClient
import com.example.cahut.data.model.Question

class QuestionRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.createService(context)
    private val authRepository = AuthRepository(context)

    suspend fun getQuestions(examId: String): List<Question> {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val response = apiService.getQuestions("Bearer $token", examId)
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to get questions")
        }
        return response.body() ?: emptyList()
    }

    suspend fun createQuestion(
        examId: String,
        question: String,
        options: List<String>,
        correctAnswer: String,
        timeLimit: Int
    ): Question {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val request = CreateQuestionRequest(examId, question, options, correctAnswer, timeLimit)
        val response = apiService.createQuestion("Bearer $token", request)
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to create question")
        }
        return response.body() ?: throw Exception("Empty response body")
    }

    suspend fun editQuestion(
        questionId: String,
        examId: String,
        question: String,
        options: List<String>,
        correctAnswer: String,
        timeLimit: Int
    ): Question {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val request = EditQuestionRequest(examId, question, options, correctAnswer, timeLimit)
        val response = apiService.editQuestion("Bearer $token", questionId, request)
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to update question")
        }
        return response.body() ?: throw Exception("Empty response body")
    }

    suspend fun deleteQuestion(questionId: String) {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val response = apiService.deleteQuestion("Bearer $token", questionId)
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to delete question")
        }
    }
} 