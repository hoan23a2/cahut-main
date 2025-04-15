package com.example.cahut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cahut.data.db.AccountDatabase
import com.example.cahut.data.model.Account
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.cahut.ui.theme.GameLobbyTheme

@Composable
fun DatabaseDebugScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { AccountDatabase(context) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }

    LaunchedEffect(Unit) {
        accounts = database.getAllAccounts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Database Contents",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(accounts) { account ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("ID: ${account.id}")
                        Text("Email: ${account.email}")
                        Text("Username: ${account.username}")
                        Text("Password: ${account.password}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Preview(
    name = "Database Debug Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun DatabaseDebugScreenPreview() {
    GameLobbyTheme {
        DatabaseDebugScreen(rememberNavController())
    }
} 