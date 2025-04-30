package com.example.cahut.data.repository

import android.content.Context
import android.net.Uri
import com.example.cahut.data.api.ApiService
import com.example.cahut.data.api.CreateQuestionRequest
import com.example.cahut.data.api.EditQuestionRequest
import com.example.cahut.data.api.RetrofitClient
import com.example.cahut.data.model.Question
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class QuestionRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.createService(context)
    private val authRepository = AuthRepository(context)
    private val gson = Gson()
    private val appContext = context.applicationContext

    private fun getFileFromUri(uri: Uri): File {
        val inputStream: InputStream? = appContext.contentResolver.openInputStream(uri)
        val fileName = "temp_${System.currentTimeMillis()}.jpg"
        val file = File(appContext.cacheDir, fileName)
        
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        
        return file
    }

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
        timeLimit: Int,
        type: String = "normal",
        imageUri: Uri? = null
    ): Question {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        
        val request = CreateQuestionRequest(examId, question, options, correctAnswer, timeLimit, type)
        
        if (imageUri != null && type == "image") {
            val file = getFileFromUri(imageUri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val examIdPart = examId.toRequestBody("text/plain".toMediaTypeOrNull())
            val questionPart = question.toRequestBody("text/plain".toMediaTypeOrNull())
            val optionsPart = gson.toJson(options).toRequestBody("text/plain".toMediaTypeOrNull())
            val correctAnswerPart = correctAnswer.toRequestBody("text/plain".toMediaTypeOrNull())
            val timeLimitPart = timeLimit.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.createQuestionWithImage(
                "Bearer $token",
                examIdPart,
                questionPart,
                optionsPart,
                correctAnswerPart,
                timeLimitPart,
                typePart,
                imagePart
            )
            if (!response.isSuccessful) {
                throw Exception(response.errorBody()?.string() ?: "Failed to create question")
            }
            return response.body() ?: throw Exception("Empty response body")
        } else {
            val response = apiService.createQuestionWithImage(
                "Bearer $token",
                examId.toRequestBody("text/plain".toMediaTypeOrNull()),
                question.toRequestBody("text/plain".toMediaTypeOrNull()),
                gson.toJson(options).toRequestBody("text/plain".toMediaTypeOrNull()),
                correctAnswer.toRequestBody("text/plain".toMediaTypeOrNull()),
                timeLimit.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                type.toRequestBody("text/plain".toMediaTypeOrNull()),
                null
            )
            if (!response.isSuccessful) {
                throw Exception(response.errorBody()?.string() ?: "Failed to create question")
            }
            return response.body() ?: throw Exception("Empty response body")
        }
    }

    suspend fun editQuestion(
        questionId: String,
        examId: String,
        question: String,
        options: List<String>,
        correctAnswer: String,
        timeLimit: Int,
        type: String = "normal",
        imageUri: Uri? = null,
        isNewImage: Boolean = false
    ): Question {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        
        val request = EditQuestionRequest(examId, question, options, correctAnswer, timeLimit, type)
        
        if (type == "image" && isNewImage && imageUri != null) {
            val file = getFileFromUri(imageUri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val examIdPart = examId.toRequestBody("text/plain".toMediaTypeOrNull())
            val questionPart = question.toRequestBody("text/plain".toMediaTypeOrNull())
            val optionsPart = gson.toJson(options).toRequestBody("text/plain".toMediaTypeOrNull())
            val correctAnswerPart = correctAnswer.toRequestBody("text/plain".toMediaTypeOrNull())
            val timeLimitPart = timeLimit.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.editQuestionWithImage(
                "Bearer $token",
                questionId,
                examIdPart,
                questionPart,
                optionsPart,
                correctAnswerPart,
                timeLimitPart,
                typePart,
                imagePart
            )
            if (!response.isSuccessful) {
                throw Exception(response.errorBody()?.string() ?: "Failed to update question")
            }
            return response.body() ?: throw Exception("Empty response body")
        } else {
            val response = apiService.editQuestionWithImage(
                "Bearer $token",
                questionId,
                examId.toRequestBody("text/plain".toMediaTypeOrNull()),
                question.toRequestBody("text/plain".toMediaTypeOrNull()),
                gson.toJson(options).toRequestBody("text/plain".toMediaTypeOrNull()),
                correctAnswer.toRequestBody("text/plain".toMediaTypeOrNull()),
                timeLimit.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                type.toRequestBody("text/plain".toMediaTypeOrNull()),
                null
            )
            if (!response.isSuccessful) {
                throw Exception(response.errorBody()?.string() ?: "Failed to update question")
            }
            return response.body() ?: throw Exception("Empty response body")
        }
    }

    suspend fun deleteQuestion(questionId: String) {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val response = apiService.deleteQuestion("Bearer $token", questionId)
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Failed to delete question")
        }
    }
} 