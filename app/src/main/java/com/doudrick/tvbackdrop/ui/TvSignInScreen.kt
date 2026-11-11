package com.doudrick.tvbackdrop.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doudrick.tvbackdrop.model.AuthState
import com.doudrick.tvbackdrop.ui.theme.GoogleBlue
import com.doudrick.tvbackdrop.ui.theme.GoogleBlueLight
import com.doudrick.tvbackdrop.ui.theme.GoogleGreen
import com.doudrick.tvbackdrop.ui.theme.GoogleRed
import com.doudrick.tvbackdrop.ui.theme.GoogleYellow
import com.doudrick.tvbackdrop.ui.theme.TvBackground
import com.doudrick.tvbackdrop.ui.theme.TvSurface
import com.doudrick.tvbackdrop.ui.theme.TvSurfaceHighlight
import com.doudrick.tvbackdrop.ui.theme.TvSurfaceVariant
import com.doudrick.tvbackdrop.ui.theme.TvTextPrimary
import com.doudrick.tvbackdrop.ui.theme.TvTextSecondary
import com.doudrick.tvbackdrop.util.QrCodeGenerator

@Composable
fun TvSignInScreen(
    authState: AuthState,
    onSignIn: (email: String) -> Unit,
    onRefreshCode: () -> Unit,
    onExploreAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceAuth = authState.deviceAuth
    val userCode = deviceAuth?.userCode ?: "WDGQ-7842"
    val verificationUrl = deviceAuth?.verificationUrl ?: "https://www.google.com/device?user_code=$userCode"

    val qrBitmap = remember(verificationUrl) {
        QrCodeGenerator.generateQrBitmap(verificationUrl, 512)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(horizontal = 48.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Google Photos Branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Google 4-Color icon accent
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TvSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(modifier = Modifier.size(24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GoogleBlue))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GoogleRed))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Google TV Photo Picker",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TvTextPrimary,
                                fontSize = 24.sp
                            )
                        )
                        Text(
                            text = "Sign in to connect your Google Photos library & albums",
                            color = TvTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Status chip
                Surface(
                    color = TvSurfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GoogleYellow)
                                .alpha(pulseAlpha)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Awaiting Pairing",
                            color = GoogleYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main 2-Column Pairing Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Big, Crisp QR Code Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TvSurface),
                    modifier = Modifier.testTag("card_qr_code")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap,
                                    contentDescription = "Scan QR code to sign in",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Scan with your phone camera",
                            color = TvTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "or go to google.com/device",
                            color = GoogleBlueLight,
                            fontSize = 12.sp
                        )
                    }
                }

                // Right Column: Instructions & Action Buttons
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Sign in to your Google Account",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TvTextPrimary,
                            fontSize = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Connect your Google Photos albums to browse and select 4K wallpapers for your Google TV home screen.",
                        color = TvTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3-Step Guide Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TvSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InstructionStep(
                                number = "1",
                                title = "Scan QR Code",
                                description = "Use your phone camera or visit google.com/device"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            InstructionStep(
                                number = "2",
                                title = "Confirm TV Pairing Code",
                                description = "Make sure the code matches the TV screen"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            InstructionStep(
                                number = "3",
                                title = "Select Albums",
                                description = "Choose which Google Photos albums to sync to your TV"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Prominent TV Code Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = TvSurfaceHighlight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.border(1.dp, GoogleBlueLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TV CODE: ",
                                    color = TvTextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = userCode,
                                    color = GoogleBlueLight,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        // Refresh Code Button
                        Surface(
                            modifier = Modifier
                                .testTag("btn_refresh_code")
                                .tvFocusable(shape = RoundedCornerShape(12.dp), onClick = onRefreshCode),
                            color = TvSurfaceHighlight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Code",
                                    tint = TvTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Code", color = TvTextSecondary, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Remote Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Primary Sign In Button (simulates phone sign-in / completes OAuth for Tyler.Doudrick@gmail.com)
                        Button(
                            onClick = { onSignIn("Tyler.Doudrick@gmail.com") },
                            modifier = Modifier
                                .testTag("btn_sign_in_google")
                                .tvFocusable(
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = { onSignIn("Tyler.Doudrick@gmail.com") }
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign In as Tyler (Approve Pairing)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // Guest Exploration Mode (allows user to see albums if they wish without signing in)
                        OutlinedButton(
                            onClick = onExploreAsGuest,
                            modifier = Modifier
                                .testTag("btn_explore_guest")
                                .tvFocusable(
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = onExploreAsGuest
                                ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = TvTextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Browse Demo Wallpapers",
                                color = TvTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer note
            Text(
                text = "Use your TV remote D-pad to navigate. Press SELECT on 'Sign In as Tyler' to immediately complete pairing.",
                color = TvTextSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun InstructionStep(
    number: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(GoogleBlue.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = GoogleBlueLight,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = TvTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = description,
                color = TvTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
