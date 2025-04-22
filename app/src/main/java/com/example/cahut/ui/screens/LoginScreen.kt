package com.example.cahut.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cahut.navigation.Screen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.cahut.ui.theme.GameLobbyTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cahut.ui.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel()
    val loginState by viewModel.loginState.collectAsState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn) {
            navController.navigate(Screen.GameLobby.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    LaunchedEffect(loginState.error) {
        loginState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.ime),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Chào mừng trở lại",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu", color = MaterialTheme.colorScheme.onSurface) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp)
            )

            TextButton(
                onClick = { /* Handle forgot password */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        viewModel.login(email, password)
                    } else {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !loginState.isLoading
            ) {
                if (loginState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Đăng nhập",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tạo tài khoản",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(
    name = "Login Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun LoginScreenPreview() {
    GameLobbyTheme {
        LoginScreen(rememberNavController())
    }
} 