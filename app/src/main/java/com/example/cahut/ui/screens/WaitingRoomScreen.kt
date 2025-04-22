package com.example.cahut.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cahut.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.clickable
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap.createBitmap

@Composable
fun WaitingRoomScreen(
    navController: NavController, 
    roomCode: String = "123456",
    isHost: Boolean = false
) {
    val context = LocalContext.current
    var showQrDialog by remember { mutableStateOf(false) }
    
    if (showQrDialog) {
        QrCodeDialog(
            roomCode = roomCode,
            onDismiss = { showQrDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f3a40)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF23616A).copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top section with dark background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF19444A).copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Cahut Logo",
                            modifier = Modifier
                                .height(80.dp)
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Bạn đã tham gia với mã PIN:",
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        Text(
                            text = roomCode,
                            color = Color(0xFF98FF90),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Room Code", roomCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Đã sao chép mã phòng!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Sao chép",
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = { showQrDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "Mã QR",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Bottom section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "10 người chơi:",
                        color = Color.White,
                        fontSize = 16.sp
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(10) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00B074))
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { /* Handle edit character */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2D7C85)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chỉnh sửa nhân vật",
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isHost) {
                        Button(
                            onClick = { /* Handle start game */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00B074)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Bắt đầu",
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 4.dp),
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF19444A)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Đang chờ chủ phòng bắt đầu...",
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QrCodeDialog(roomCode: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã QR phòng",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val qrBitmap = remember(roomCode) {
                    generateQrCode(roomCode)
                }
                qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Mã QR",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(16.dp)
                    )
                }
                Text(
                    text = "Mã phòng: $roomCode",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        containerColor = Color(0xFF23616A),
        confirmButton = {},
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.9f)  // Take 90% of screen width
    )
}

private fun generateQrCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) BLACK else WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
} 