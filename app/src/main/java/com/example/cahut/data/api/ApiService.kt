package com.example.cahut.data.api

import com.example.cahut.data.model.Exam
import com.example.cahut.data.model.Question
import com.example.cahut.data.model.Room
import com.example.cahut.data.model.CreateRoomResponse
import com.example.cahut.data.model.JoinRoomResponse
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val message: String? = null
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String
)

data class CreateRoomRequest(
    val examId: String
)

data class JoinRoomRequest(
    val roomId: String
)

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/exam/get")
    suspend fun getExams(@Header("Authorization") token: String): Response<List<Exam>>

    @POST("api/exam/create")
    suspend fun createExam(
        @Header("Authorization") token: String,
        @Body request: CreateExamRequest
    ): Response<Exam>

    @PUT("api/exam/edit/{examId}")
    suspend fun editExam(
        @Header("Authorization") token: String,
        @Path("examId") examId: String,
        @Body request: EditExamRequest
    ): Response<Exam>

    @DELETE("api/exam/delete/{examId}")
    suspend fun deleteExam(
        @Header("Authorization") token: String,
        @Path("examId") examId: String
    ): Response<Unit>

    @GET("api/question/get/{examId}")
    suspend fun getQuestions(
        @Header("Authorization") token: String,
        @Path("examId") examId: String
    ): Response<List<Question>>

    @POST("api/question/create")
    suspend fun createQuestion(
        @Header("Authorization") token: String,
        @Body request: CreateQuestionRequest
    ): Response<Question>

    @PUT("api/question/edit/{questionId}")
    suspend fun editQuestion(
        @Header("Authorization") token: String,
        @Path("questionId") questionId: String,
        @Body request: EditQuestionRequest
    ): Response<Question>

    @DELETE("api/question/delete/{questionId}")
    suspend fun deleteQuestion(
        @Header("Authorization") token: String,
        @Path("questionId") questionId: String
    ): Response<Unit>

    @POST("api/room/create")
    suspend fun createRoom(
        @Header("Authorization") token: String,
        @Body request: CreateRoomRequest
    ): Response<CreateRoomResponse>

    @POST("api/room/join")
    suspend fun joinRoom(
        @Header("Authorization") token: String,
        @Body request: JoinRoomRequest
    ): Response<JoinRoomResponse>
}

data class CreateExamRequest(
    val examName: String
)

data class EditExamRequest(
    val examName: String
)

data class CreateQuestionRequest(
    val examId: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val timeLimit: Int
)

data class EditQuestionRequest(
    val examId: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val timeLimit: Int
) 