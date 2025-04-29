package com.example.cahut.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object GameLobby : Screen("game_lobby")
    object DatabaseDebug : Screen("database_debug")
    object CreateQuizInfo : Screen("create_quiz_info")
    object CreateQuizSlide : Screen("create_quiz_slide/{examId}") {
        fun createRoute(examId: String) = "create_quiz_slide/$examId"
    }
    object WaitingRoom : Screen("waiting_room/{roomId}/{examId}/{isHost}") {
        fun createRoute(roomId: String, examId: String, isHost: Boolean) = "waiting_room/$roomId/$examId/$isHost"
    }
    object PlayQuiz : Screen("play_quiz")
    object QuizScore : Screen("quiz_score")
    object QuizResult : Screen("quiz_result")
    object EditQuestions : Screen("edit_questions/{examId}/{examName}") {
        fun createRoute(examId: String, examName: String) = "edit_questions/$examId/$examName"
    }
}

object NavGraph {
    const val ROOT_ROUTE = "root"
} 