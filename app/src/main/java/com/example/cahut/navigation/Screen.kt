package com.example.cahut.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object GameLobby : Screen("game_lobby")
    object DatabaseDebug : Screen("database_debug")
    object CreateQuizInfo : Screen("create_quiz_info")
    object CreateQuizSlide : Screen("create_quiz_slide")
    object PlayQuiz : Screen("play_quiz")
    object QuizScore : Screen("quiz_score")
    object QuizResult : Screen("quiz_result")
}

object NavGraph {
    const val ROOT_ROUTE = "root"
} 