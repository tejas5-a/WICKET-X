package com.example.ui.screens.scorer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MatchEntity
import com.example.data.local.TournamentEntity
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
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.MintPrimary
import com.example.ui.viewmodel.AccountStatus
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.ScorerAuthViewModel
import com.example.ui.viewmodel.TournamentsViewModel

@Composable
fun ScorerDashboardScreen(
    authViewModel: ScorerAuthViewModel,
    tournamentsViewModel: TournamentsViewModel,
    homeViewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onCreateTournamentClick: () -> Unit,
    onCreateMatchClick: () -> Unit,
    onStartScoringClick: (String) -> Unit,
    onManageTournamentClick: (String) -> Unit,
    onViewScorecardClick: (String) -> Unit,
    onPhoneAuthClick: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val tournaments by tournamentsViewModel.tournaments.collectAsState()
    val liveMatches by homeViewModel.liveMatches.collectAsState()
    val allMatches by homeViewModel.allMatches.collectAsState()
    val completedMatches = remember(allMatches) { allMatches.filter { it.status == "completed" } }
    val accountStatus = authViewModel.getAccountStatus(currentUser)

    var showRequestScorerDialog by remember { mutableStateOf(false) }

    GlassScaffoldBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("scorer_dashboard_screen")
        ) {
            WicketXTopBar(
                title = "Scorer Dashboard",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Scorer Profile Header Glass Card
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(MintPrimary.copy(alpha = 0.15f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsCricket,
                                        contentDescription = null,
                                        tint = MintPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.displayName?.ifBlank { "Grassroots Official" } ?: "Cricket Official",
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = currentUser?.clubName?.ifBlank { "Affiliated League Official" } ?: "League not linked",
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            // Account Status Badge
                            val (badgeBg, badgeFg) = when (accountStatus) {
                                AccountStatus.SCORER_ACTIVE -> Pair(MintPrimary.copy(alpha = 0.18f), MintPrimary)
                                AccountStatus.PHONE_VERIFIED -> Pair(ChampagneHighlight.copy(alpha = 0.2f), ChampagneHighlight)
                                AccountStatus.SCORER_PENDING -> Pair(ChampagneHighlight.copy(alpha = 0.15f), ChampagneHighlight)
                                AccountStatus.ACCESS_RESTRICTED -> Pair(ErrorCrimson.copy(alpha = 0.18f), ErrorCrimson)
                                AccountStatus.PHONE_UNVERIFIED -> Pair(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.7f))
                            }
                            GlassBadge(
                                text = accountStatus.label,
                                textColor = badgeFg,
                                backgroundColor = badgeBg,
                                borderColor = badgeFg.copy(alpha = 0.35f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = GlassBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${currentUser?.matchesScoredCount ?: 14}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MintPrimary
                                )
                                Text(
                                    text = "Matches Scored",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${currentUser?.tournamentsManagedCount ?: 2}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ChampagneHighlight
                                )
                                Text(
                                    text = "Tournaments",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "100%",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MintPrimary
                                )
                                Text(
                                    text = "Ball Accuracy",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }

                        if (accountStatus != AccountStatus.SCORER_ACTIVE) {
                            Spacer(modifier = Modifier.height(14.dp))
                            MintGlowButton(
                                text = if (accountStatus == AccountStatus.PHONE_UNVERIFIED) "Verify Phone to Unlock Scoring" else "Activate Scorer Role",
                                onClick = {
                                    if (accountStatus == AccountStatus.PHONE_UNVERIFIED) {
                                        onPhoneAuthClick()
                                    } else {
                                        showRequestScorerDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_activate_scorer_banner")
                            )
                        }
                    }
                }

                // 2. Primary Scorer Actions
                item {
                    Text(
                        text = "Scorer Management Actions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onCreateTournamentClick() }
                                .testTag("card_action_create_tournament"),
                            borderColor = MintPrimary.copy(alpha = 0.35f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MintPrimary.copy(alpha = 0.18f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MintPrimary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Create Tournament",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Auto fixtures & NRR",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onCreateMatchClick() }
                                .testTag("card_action_create_match"),
                            borderColor = ChampagneHighlight.copy(alpha = 0.35f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(ChampagneHighlight.copy(alpha = 0.18f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SportsCricket, contentDescription = null, tint = ChampagneHighlight)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Create Match",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Toss, overs & teams",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // 3. Start Scoring (Live Matches)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Start Scoring Live Matches",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        GlassBadge(
                            text = "${liveMatches.size} LIVE",
                            textColor = MintPrimary,
                            backgroundColor = MintPrimary.copy(alpha = 0.15f),
                            borderColor = MintPrimary.copy(alpha = 0.35f)
                        )
                    }
                }

                if (liveMatches.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No live matches available right now. Tap 'Create Match' to start a new match.",
                                color = Color.White.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                items(liveMatches) { match ->
                    MatchScorerCard(
                        match = match,
                        onScoreClick = { onStartScoringClick(match.id) },
                        onViewClick = { onViewScorecardClick(match.id) }
                    )
                }

                // 4. My Tournaments Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "My Managed Tournaments",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        TextButton(onClick = onCreateTournamentClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MintPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New League", color = MintPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (tournaments.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No tournaments managed yet. Tap 'Create Tournament' to organize a league.",
                                color = Color.White.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                items(tournaments) { tour ->
                    TournamentScorerCard(
                        tournament = tour,
                        onManageClick = { onManageTournamentClick(tour.id) }
                    )
                }

                // 5. Match History
                item {
                    Text(
                        text = "Match History & Audited Results",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                if (completedMatches.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Completed matches with audited scores will appear here.",
                                color = Color.White.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                items(completedMatches) { match ->
                    CompletedMatchHistoryCard(
                        match = match,
                        onViewClick = { onViewScorecardClick(match.id) }
                    )
                }
            }

            // Dialog: Request Scorer Access
            if (showRequestScorerDialog) {
                RequestScorerAccessDialog(
                    initialName = currentUser?.displayName ?: "",
                    initialClub = currentUser?.clubName ?: "",
                    onDismiss = { showRequestScorerDialog = false },
                    onSubmit = { name, club, exp ->
                        authViewModel.requestScorerAccess(name, club, exp) { success ->
                            if (success) {
                                showRequestScorerDialog = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MatchScorerCard(
    match: MatchEntity,
    onScoreClick: () -> Unit,
    onViewClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = MintPrimary.copy(alpha = 0.25f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${match.matchFormat} • ${match.venue}",
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.65f)
            )
            GlassBadge(
                text = "Innings ${match.currentInnings}",
                textColor = ChampagneHighlight,
                backgroundColor = ChampagneHighlight.copy(alpha = 0.15f),
                borderColor = ChampagneHighlight.copy(alpha = 0.35f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.teamAName,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "${match.teamAScore}/${match.teamAWickets} (${match.teamAOvers} ov)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MintPrimary
                )
            }

            Text(
                text = "vs",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = match.teamBName,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "${match.teamBScore}/${match.teamBWickets} (${match.teamBOvers} ov)",
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ChampagneHighlight
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MintGlowButton(
                text = "Score Match",
                onClick = onScoreClick,
                icon = Icons.Default.SportsCricket,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_score_match_${match.id}")
            )

            GlassButton(
                text = "View",
                onClick = onViewClick,
                modifier = Modifier.testTag("btn_view_match_${match.id}")
            )
        }
    }
}

@Composable
fun TournamentScorerCard(
    tournament: TournamentEntity,
    onManageClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tournament.name,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            val (badgeBg, badgeFg) = when (tournament.status) {
                "active" -> Pair(MintPrimary.copy(alpha = 0.18f), MintPrimary)
                "upcoming" -> Pair(ChampagneHighlight.copy(alpha = 0.18f), ChampagneHighlight)
                else -> Pair(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.7f))
            }
            GlassBadge(
                text = tournament.status.uppercase(),
                textColor = badgeFg,
                backgroundColor = badgeBg,
                borderColor = badgeFg.copy(alpha = 0.35f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${tournament.format} • ${tournament.location} • ${tournament.teamCount} Teams",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.65f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassButton(
            text = "Manage Fixtures & Points Table",
            onClick = onManageClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_manage_tournament_${tournament.id}")
        )
    }
}

@Composable
fun CompletedMatchHistoryCard(
    match: MatchEntity,
    onViewClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() }
    ) {
        Text(
            text = "${match.teamAName} vs ${match.teamBName}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = match.resultSummary.ifBlank { "Match completed" },
            style = MaterialTheme.typography.bodySmall,
            color = MintPrimary
        )
    }
}

@Composable
fun RequestScorerAccessDialog(
    initialName: String,
    initialClub: String,
    onDismiss: () -> Unit,
    onSubmit: (name: String, club: String, exp: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var club by remember { mutableStateOf(initialClub) }
    var experience by remember { mutableStateOf("Club Scorer (Level 1)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.8f),
        title = {
            Text("Request Scorer Role Access", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Submit your cricket association or league credentials to unlock official match scoring privileges.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MintPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = MintPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_scorer_name")
                )

                OutlinedTextField(
                    value = club,
                    onValueChange = { club = it },
                    label = { Text("Cricket Club / League Affiliation", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MintPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = MintPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_scorer_club")
                )

                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Scoring Experience / Level", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MintPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = MintPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_scorer_exp")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(name, club, experience) },
                enabled = name.isNotBlank() && club.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintPrimary,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_submit_scorer_request")
            ) {
                Text("Activate Scorer Role", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}
