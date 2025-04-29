package com.example.cahut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.cahut.navigation.Screen
import com.example.cahut.ui.screens.DatabaseDebugScreen
import com.example.cahut.ui.screens.GameLobbyScreen
import com.example.cahut.ui.screens.LoginScreen
import com.example.cahut.ui.screens.RegisterScreen
import com.example.cahut.ui.screens.CreateQuizInfoScreen
import com.example.cahut.ui.screens.CreateQuizSlideScreen
import com.example.cahut.ui.screens.PlayQuizScreen
import com.example.cahut.ui.screens.QuizScoreScreen
import com.example.cahut.ui.screens.QuizResultScreen
import com.example.cahut.ui.screens.WaitingRoomScreen
import com.example.cahut.ui.screens.EditQuestionsScreen
import com.example.cahut.ui.theme.GameLobbyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameLobbyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(navController)
                        }
                        composable(Screen.Register.route) {
                            RegisterScreen(navController)
                        }
                        composable(Screen.GameLobby.route) {
                            GameLobbyScreen(navController)
                        }
                        composable(Screen.DatabaseDebug.route) {
                            DatabaseDebugScreen(navController)
                        }
                        composable(Screen.CreateQuizInfo.route) {
                            CreateQuizInfoScreen(navController)
                        }
                        composable(
                            route = Screen.CreateQuizSlide.route,
                            arguments = listOf(
                                navArgument("examId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val examId = backStackEntry.arguments?.getString("examId") ?: ""
                            CreateQuizSlideScreen(navController, examId)
                        }
                        composable(
                            route = Screen.WaitingRoom.route,
                            arguments = listOf(
                                navArgument("roomId") { type = NavType.StringType },
                                navArgument("examId") { type = NavType.StringType },
                                navArgument("isHost") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                            val examId = backStackEntry.arguments?.getString("examId") ?: ""
                            val isHost = backStackEntry.arguments?.getBoolean("isHost") ?: false
                            WaitingRoomScreen(
                                navController = navController,
                                roomId = roomId,
                                examId = examId,
                                isHost = isHost
                            )
                        }
                        composable(
                            route = Screen.PlayQuiz.route,
                            arguments = listOf(
                                navArgument("roomId") { type = NavType.StringType },
                                navArgument("isHost") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                            val isHost = backStackEntry.arguments?.getBoolean("isHost") ?: false
                            PlayQuizScreen(navController, roomId, isHost)
                        }
                        composable(Screen.QuizScore.route) {
                            QuizScoreScreen(navController)
                        }
                        composable(Screen.QuizResult.route) {
                            QuizResultScreen(navController)
                        }
                        composable(
                            route = Screen.EditQuestions.route,
                            arguments = listOf(
                                navArgument("examId") { type = NavType.StringType },
                                navArgument("examName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val examId = backStackEntry.arguments?.getString("examId") ?: ""
                            val examName = backStackEntry.arguments?.getString("examName") ?: ""
                            EditQuestionsScreen(
                                navController = navController,
                                examId = examId,
                                examName = examName
                            )
                        }
                    }
                }
            }
        }
    }
}