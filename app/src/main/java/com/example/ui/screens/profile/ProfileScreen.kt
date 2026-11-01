package com.example.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthManager
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
fun ProfileScreen(
    authManager: AuthManager,
    scorerAuthViewModel: ScorerAuthViewModel,
    onSearchClick: () -> Unit,
    onScorerDashboardClick: () -> Unit = {},
    onPhoneAuthClick: () -> Unit = {}
) {
    val userAccount by scorerAuthViewModel.currentUser.collectAsState()
    val accountStatus = scorerAuthViewModel.getAccountStatus(userAccount)

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "Player & Scorer Profile",
                onSearchClick = onSearchClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_user_card"),
                    borderColor = MintPrimary.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MintPrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = MintPrimary,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userAccount?.displayName?.ifBlank { "Cricket Official" } ?: "Cricket Official",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Text(
                            text = userAccount?.phoneNumber ?: "+1 555 000 1111",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )

                        if (userAccount?.clubName?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userAccount!!.clubName,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = ChampagneHighlight
                            )
                        }

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
                                imageVector = if (accountStatus == AccountStatus.SCORER_ACTIVE) Icons.Default.VerifiedUser else Icons.Default.Security,
                                contentDescription = null,
                                tint = badgeFg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            GlassBadge(
                                text = accountStatus.label,
                                textColor = badgeFg,
                                backgroundColor = badgeBg,
                                borderColor = badgeFg.copy(alpha = 0.35f)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MintGlowButton(
                                text = "Scorer Hub",
                                onClick = onScorerDashboardClick,
                                icon = Icons.Default.SportsCricket,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_scorer_dashboard")
                            )

                            GlassButton(
                                text = "Phone OTP",
                                onClick = onPhoneAuthClick,
                                icon = Icons.Default.Phone,
                                textColor = ChampagneHighlight,
                                borderColor = ChampagneHighlight.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_phone_auth")
                            )
                        }
                    }
                }

                // Quick Test Role Switcher Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MintPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Account Switcher (Scorer vs. Viewer)",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Quickly toggle between roles to test authorized scorer controls vs. viewer read-only experience:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isScorer = accountStatus == AccountStatus.SCORER_ACTIVE
                        Button(
                            onClick = { scorerAuthViewModel.switchTestAccount(toScorer = true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isScorer) MintPrimary else Color.White.copy(alpha = 0.08f),
                                contentColor = if (isScorer) BackgroundDark else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_switch_scorer_role")
                        ) {
                            Text(
                                text = "Certified Scorer",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { scorerAuthViewModel.switchTestAccount(toScorer = false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isScorer) ChampagneHighlight else Color.White.copy(alpha = 0.08f),
                                contentColor = if (!isScorer) BackgroundDark else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_switch_viewer_role")
                        ) {
                            Text(
                                text = "Spectator / Viewer",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Career Highlights Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cricket Stats & Badges",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            label = "Matches Scored",
                            value = "${userAccount?.matchesScoredCount ?: 14}",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "Tournaments",
                            value = "${userAccount?.tournamentsManagedCount ?: 2}",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "Badge",
                            value = userAccount?.scorerBadge ?: "Certified Scorer",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = value,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MintPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}
