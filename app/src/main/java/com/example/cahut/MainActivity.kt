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
                        composable(Screen.CreateQuizSlide.route) {
                            CreateQuizSlideScreen(navController)
                        }
                        composable(Screen.PlayQuiz.route) {
                            PlayQuizScreen(navController)
                        }
                        composable(Screen.QuizScore.route) {
                            QuizScoreScreen(navController)
                        }
                        composable(Screen.QuizResult.route) {
                            QuizResultScreen(navController)
                        }
                    }
                }
            }
        }
    }
}