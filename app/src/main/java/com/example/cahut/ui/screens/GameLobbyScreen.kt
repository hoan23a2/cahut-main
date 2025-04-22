package com.example.cahut.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cahut.ui.theme.GameLobbyTheme
import com.example.cahut.navigation.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.cahut.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun GameLobbyScreen(navController: NavController) {
    var gameRoomId by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Divider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Chỉnh Sửa Hồ Sơ") },
                    label = { Text("Chỉnh Sửa Hồ Sơ") },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            drawerState.close()
                            // Handle edit profile navigation
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Danh Sách Quiz") },
                    label = { Text("Danh Sách Quiz") },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            drawerState.close()
                            // Handle quiz list navigation
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Đăng Xuất") },
                    label = { Text("Đăng Xuất") },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            drawerState.close()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.GameLobby.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Hồ Sơ",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { scope.launch { drawerState.open() } },
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Room Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFA7A0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tham gia phòng?",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Nhập mã PIN:",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedTextField(
                            value = gameRoomId,
                            onValueChange = { 
                                // Only allow numbers and limit to 6 digits
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    gameRoomId = it
                                    // Check if the entered PIN is correct (123456)
                                    if (it == "123456") {
                                        // Navigate to WaitingRoom screen
                                        navController.navigate(Screen.WaitingRoom.route)
                                    }
                                }
                            },
                            modifier = Modifier
                                .height(50.dp)
                                .width(140.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            placeholder = { 
                                Text(
                                    "123 456", 
                                    color = Color.Gray,
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                ) 
                            },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Host Game Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF19444A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tạo phòng chơi mới",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { 
                            navController.navigate(Screen.WaitingRoom.createRoute(isHost = true))
                        },
                        modifier = Modifier
                            .width(160.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B074)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Tạo Phòng",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Quiz Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF19444A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tạo bộ Quiz riêng cho bạn",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { 
                            navController.navigate(Screen.CreateQuizInfo.route)
                        },
                        modifier = Modifier
                            .width(160.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6FEEFF)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Tạo Quiz",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Games Section
            Text(
                text = "Phòng Gần Đây",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recent games card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Chưa có phòng nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Game Lobby Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun GameLobbyScreenPreview() {
    GameLobbyTheme {
        GameLobbyScreen(rememberNavController())
    }
} 