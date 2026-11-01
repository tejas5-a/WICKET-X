package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.supabase.SupabaseAuthService
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.MintGlowButton
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MintPrimary
import com.example.ui.viewmodel.AccountStatus
import com.example.ui.viewmodel.ScorerAuthViewModel

@Composable
fun PhoneAuthScreen(
    viewModel: ScorerAuthViewModel,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val otpCode by viewModel.otpCode.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val cooldownSeconds by viewModel.cooldownSeconds.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val accountStatus = viewModel.getAccountStatus(currentUser)

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "Phone Verification",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MintPrimary.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MintPrimary.copy(alpha = 0.18f), shape = RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Security",
                                tint = MintPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Secure Scorer Authentication",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Verify your phone number with Supabase OTP to unlock match scoring and tournament creation privileges.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Account Status Badge
                        val (badgeBg, badgeFg) = when (accountStatus) {
                            AccountStatus.SCORER_ACTIVE -> Pair(MintPrimary.copy(alpha = 0.18f), MintPrimary)
                            AccountStatus.PHONE_VERIFIED -> Pair(ChampagneHighlight.copy(alpha = 0.2f), ChampagneHighlight)
                            AccountStatus.SCORER_PENDING -> Pair(ChampagneHighlight.copy(alpha = 0.15f), ChampagneHighlight)
                            AccountStatus.ACCESS_RESTRICTED -> Pair(ErrorCrimson.copy(alpha = 0.18f), ErrorCrimson)
                            AccountStatus.PHONE_UNVERIFIED -> Pair(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.7f))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (accountStatus == AccountStatus.SCORER_ACTIVE) Icons.Default.VerifiedUser else Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = badgeFg
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            GlassBadge(
                                text = accountStatus.label,
                                textColor = badgeFg,
                                backgroundColor = badgeBg,
                                borderColor = badgeFg.copy(alpha = 0.35f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Input Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "1. Enter Mobile Number",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { viewModel.setPhoneNumber(it) },
                        label = { Text("Phone Number with Country Code", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = { Text("+15550001111 or +919876543210", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MintPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_phone_number"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    MintGlowButton(
                        text = if (isLoading && !isOtpSent) {
                            "Sending..."
                        } else if (cooldownSeconds > 0) {
                            "Resend Code in ${cooldownSeconds}s"
                        } else if (isOtpSent) {
                            "Resend OTP Code"
                        } else {
                            "Send Verification OTP"
                        },
                        onClick = { viewModel.sendOtp() },
                        enabled = !isLoading && cooldownSeconds == 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_send_otp")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OTP Input Card (Shown once OTP is requested)
                AnimatedVisibility(visible = isOtpSent) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = MintPrimary.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = "2. Enter 6-Digit OTP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { viewModel.setOtpCode(it) },
                            label = { Text("6-Digit Verification Code", color = Color.White.copy(alpha = 0.7f)) },
                            placeholder = { Text("123456", color = Color.White.copy(alpha = 0.4f)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MintPrimary)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_otp_code"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = MintPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.verifyOtp {
                                    onAuthSuccess()
                                }
                            },
                            enabled = !isLoading && otpCode.length == 6,
                            colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = BackgroundDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("btn_verify_otp")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = BackgroundDark, modifier = Modifier.size(24.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verify & Activate", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Status & Error Messages
                statusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MintPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MintPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = MintPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ErrorCrimson.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = ErrorCrimson,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Preset Test Accounts Section
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Instant Testing Credentials",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneHighlight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select a preset testing account below to fill credentials and test role-based behavior instantly:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassButton(
                        text = "Use Certified Scorer: ${SupabaseAuthService.TEST_SCORER_PHONE}",
                        onClick = {
                            viewModel.setPhoneNumber(SupabaseAuthService.TEST_SCORER_PHONE)
                            viewModel.setOtpCode(SupabaseAuthService.TEST_OTP_CODE)
                            viewModel.sendOtp()
                        },
                        textColor = MintPrimary,
                        borderColor = MintPrimary.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_test_scorer_account")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassButton(
                        text = "Use Grassroots Viewer: ${SupabaseAuthService.TEST_VIEWER_PHONE}",
                        onClick = {
                            viewModel.setPhoneNumber(SupabaseAuthService.TEST_VIEWER_PHONE)
                            viewModel.setOtpCode(SupabaseAuthService.TEST_OTP_CODE)
                            viewModel.sendOtp()
                        },
                        textColor = ChampagneHighlight,
                        borderColor = ChampagneHighlight.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_test_viewer_account")
                    )
                }
            }
        }
    }
}
