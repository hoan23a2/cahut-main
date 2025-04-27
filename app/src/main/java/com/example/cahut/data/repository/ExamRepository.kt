package com.example.cahut.data.repository

import android.content.Context
import com.example.cahut.data.api.ApiService
import com.example.cahut.data.api.CreateExamRequest
import com.example.cahut.data.api.EditExamRequest
import com.example.cahut.data.api.RetrofitClient
import com.example.cahut.data.model.Exam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExamRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.createService(context)
    private val authRepository = AuthRepository(context)

    suspend fun getExams(): List<Exam> {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val response = apiService.getExams("Bearer $token")
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to get exams")
        }
        return response.body() ?: emptyList()
    }

    suspend fun createExam(examName: String): Exam {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val request = CreateExamRequest(examName)
        val response = apiService.createExam("Bearer $token", request)
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to create exam")
        }
        return response.body() ?: throw Exception("Empty response body")
    }

    suspend fun editExam(examId: String, examName: String): Exam {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val request = EditExamRequest(examName)
        val response = apiService.editExam("Bearer $token", examId, request)
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to edit exam")
        }
        return response.body() ?: throw Exception("Empty response body")
    }

    suspend fun deleteExam(examId: String) {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val response = apiService.deleteExam("Bearer $token", examId)
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to delete exam")
        }
    }
} 