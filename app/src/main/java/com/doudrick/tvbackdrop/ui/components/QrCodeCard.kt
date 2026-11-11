package com.doudrick.tvbackdrop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doudrick.tvbackdrop.model.AuthState
import com.doudrick.tvbackdrop.ui.theme.GoogleBlue
import com.doudrick.tvbackdrop.ui.theme.GoogleBlueLight
import com.doudrick.tvbackdrop.ui.theme.GoogleGreen
import com.doudrick.tvbackdrop.ui.theme.TvSurface
import com.doudrick.tvbackdrop.ui.theme.TvSurfaceHighlight
import com.doudrick.tvbackdrop.ui.theme.TvSurfaceVariant
import com.doudrick.tvbackdrop.ui.theme.TvTextPrimary
import com.doudrick.tvbackdrop.ui.theme.TvTextSecondary
import com.doudrick.tvbackdrop.ui.tvFocusable
import com.doudrick.tvbackdrop.util.QrCodeGenerator

@Composable
fun QrCodeCard(
    authState: AuthState,
    onSimulateConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)
    val deviceAuth = authState.deviceAuth

    val qrContent = deviceAuth?.verificationUrl ?: "https://www.google.com/device"
    val qrBitmap = remember(qrContent) {
        QrCodeGenerator.generateQrBitmap(qrContent, 420)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qr_connect_card"),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = TvSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            // QR Code Box
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Scan QR code to connect Google Photos",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Connection Details & Instructions
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Connect Google Photos",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TvTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    if (authState.isConnected) {
                        Surface(
                            color = GoogleGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GoogleGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Connected (${authState.accountEmail})",
                                    color = GoogleGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Scan the QR code with your phone camera, or go to google.com/device\n" +
                            "2. Enter your TV code:\n" +
                            "3. Select albums to use as your Google TV wallpapers",
                    color = TvTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Big User Code Pill
                Surface(
                    color = TvSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TV Code: ",
                            color = TvTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = deviceAuth?.userCode ?: "WDGQ-7842",
                            color = GoogleBlueLight,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!authState.isConnected) {
                        Button(
                            onClick = onSimulateConnect,
                            modifier = Modifier
                                .testTag("btn_simulate_connect")
                                .tvFocusable(shape = RoundedCornerShape(10.dp), onClick = onSimulateConnect),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
                        ) {
                            Text("Simulate Mobile Approval (Instant)", color = Color.White)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier
                                .testTag("btn_disconnect")
                                .tvFocusable(shape = RoundedCornerShape(10.dp), onClick = onDisconnect),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Disconnect Account", color = Color.LightGray)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("btn_close_qr")
                            .tvFocusable(shape = RoundedCornerShape(10.dp), onClick = onDismiss),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Browse Albums", color = TvTextPrimary)
                    }
                }
            }
        }
    }
}
