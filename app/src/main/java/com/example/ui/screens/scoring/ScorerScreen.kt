package com.example.ui.screens.scoring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.DeliveryEvent
import com.example.domain.engine.DismissalType
import com.example.domain.engine.ExtraType
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassKeypadButton
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.MintGlowButton
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.MintPrimary
import com.example.ui.viewmodel.MatchScoringViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorerScreen(
    matchId: String,
    viewModel: MatchScoringViewModel,
    onNavigateBack: () -> Unit,
    onViewScorecard: (String) -> Unit
) {
    val state by viewModel.engineState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAuthorizedScorer by viewModel.isAuthorizedScorer.collectAsState()
    val audits by viewModel.deliveryAudits.collectAsState()

    var showWicketDialog by remember { mutableStateOf(false) }
    var showChangeBowlerDialog by remember { mutableStateOf(false) }
    var showNextInningsDialog by remember { mutableStateOf(false) }
    var showAuditsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = state?.let { "${it.teamAName} vs ${it.teamBName}" } ?: "Live Scoring",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = { showAuditsDialog = true },
                        modifier = Modifier.testTag("top_bar_audits_btn")
                    ) {
                        Text("Audits", color = ChampagneHighlight, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = { onViewScorecard(matchId) },
                        modifier = Modifier.testTag("top_bar_scorecard_btn")
                    ) {
                        Text("Scorecard", color = MintPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )

            if (isLoading || state == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MintPrimary)
                }
                return@Column
            }

            val match = state!!
            val currentInn = match.currentInnings

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
            ) {
                // Free hit banner if active
                if (currentInn.isFreeHit) {
                    Surface(
                        color = ErrorCrimson.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "⚡ FREE HIT! No dismissal except run-out",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                // Digital Scoreboard Glass Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("digital_scoreboard_card"),
                    borderColor = MintPrimary.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentInn.battingTeamName,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            GlassBadge(
                                text = "Innings ${match.currentInningsIndex}",
                                textColor = ChampagneHighlight,
                                backgroundColor = ChampagneHighlight.copy(alpha = 0.15f),
                                borderColor = ChampagneHighlight.copy(alpha = 0.35f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Big Score Display in Mint Neon (responsive to narrow screens & large fonts)
                        Text(
                            text = "${currentInn.totalRuns}/${currentInn.wickets}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 42.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MintPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "(${currentInn.oversFormatted} / ${currentInn.maxOvers} ov)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.75f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Run Rate Stats Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "CRR",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    String.format("%.2f", currentInn.currentRunRate),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ChampagneHighlight
                                )
                            }
                            if (match.currentInningsIndex == 2 && match.target != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Target",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        "${match.target}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "RRR",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        String.format("%.2f", match.requiredRunRate ?: 0.0),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MintPrimary
                                    )
                                }
                            }
                        }

                        if (match.currentInningsIndex == 2 && match.requiredRuns != null && match.remainingBalls != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Need ${match.requiredRuns} runs in ${match.remainingBalls} balls to win",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MintPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Batters on Crease Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Batters on Crease",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.switchStrike() }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Strike",
                                tint = MintPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch Strike", style = MaterialTheme.typography.labelSmall, color = MintPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Striker
                    val striker = currentInn.batters[currentInn.strikerName]
                    BatterRow(
                        name = currentInn.strikerName,
                        isStriker = true,
                        runs = striker?.runs ?: 0,
                        balls = striker?.ballsFaced ?: 0,
                        fours = striker?.fours ?: 0,
                        sixes = striker?.sixes ?: 0,
                        strikeRate = striker?.strikeRate ?: 0.0
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = GlassBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Non-Striker
                    val nonStriker = currentInn.batters[currentInn.nonStrikerName]
                    BatterRow(
                        name = currentInn.nonStrikerName,
                        isStriker = false,
                        runs = nonStriker?.runs ?: 0,
                        balls = nonStriker?.ballsFaced ?: 0,
                        fours = nonStriker?.fours ?: 0,
                        sixes = nonStriker?.sixes ?: 0,
                        strikeRate = nonStriker?.strikeRate ?: 0.0
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Bowler Glass Card
                val bowler = currentInn.bowlers[currentInn.currentBowlerName]
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Bowler: ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = currentInn.currentBowlerName,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${bowler?.oversString ?: "0.0"} ov • ${bowler?.maidens ?: 0} M • ${bowler?.runsConceded ?: 0} R • ${bowler?.wickets ?: 0} W • Econ ${String.format("%.1f", bowler?.economy ?: 0.0)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChampagneHighlight
                            )
                        }
                        GlassButton(
                            text = "Change",
                            onClick = { showChangeBowlerDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recent Deliveries Reel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Balls (This Over)",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${currentInn.recentDeliveries.size} deliveries",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    if (currentInn.recentDeliveries.isEmpty()) {
                        item {
                            Text(
                                "Over beginning...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(currentInn.recentDeliveries) { del ->
                            BallBubble(del)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Authorization Notice if viewing without scorer permissions
                if (!isAuthorizedScorer) {
                    Surface(
                        color = ErrorCrimson.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ErrorCrimson, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Viewer Mode: Match controls are disabled. Only assigned scorers can record deliveries.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Scoring Controls Keypad
                Text(
                    text = "Scoring Keypad",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Tactile Glass Runs Keypad in a responsive 2-row grid so buttons fit cleanly on 320dp+ phones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: 0, 1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 1, 2, 3).forEach { run ->
                            GlassKeypadButton(
                                text = "$run",
                                onClick = { if (isAuthorizedScorer) viewModel.scoreBall(runsBat = run) },
                                isBoundary = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("run_btn_$run")
                            )
                        }
                    }
                    // Row 2: 4 (Boundary), 6 (Boundary), 5
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4, 6, 5).forEach { run ->
                            val isBoundary = run == 4 || run == 6
                            GlassKeypadButton(
                                text = "$run",
                                onClick = { if (isAuthorizedScorer) viewModel.scoreBall(runsBat = run) },
                                isBoundary = isBoundary,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("run_btn_$run")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Extras Buttons: Wide, No-Ball, Bye, Leg-Bye in frosted dark glass
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("WD", ExtraType.WIDE, "btn_extra_wd"),
                        Triple("NB", ExtraType.NO_BALL, "btn_extra_nb"),
                        Triple("BYE", ExtraType.BYE, "btn_extra_bye"),
                        Triple("LB", ExtraType.LEG_BYE, "btn_extra_lb")
                    ).forEach { (label, extraType, tag) ->
                        GlassButton(
                            text = label,
                            onClick = {
                                if (isAuthorizedScorer) {
                                    viewModel.scoreBall(runsBat = 0, extraType = extraType, extraRuns = 1)
                                }
                            },
                            textColor = ChampagneHighlight,
                            borderColor = ChampagneHighlight.copy(alpha = 0.35f),
                            modifier = Modifier
                                .weight(1f)
                                .testTag(tag)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Action Buttons: OUT/WICKET & UNDO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { if (isAuthorizedScorer) showWicketDialog = true },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("btn_wicket_action"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorCrimson)
                    ) {
                        Text(
                            text = "OUT / WICKET",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }

                    GlassButton(
                        text = "Undo",
                        onClick = { if (isAuthorizedScorer) viewModel.undoLastDelivery() },
                        icon = Icons.AutoMirrored.Filled.Undo,
                        textColor = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_undo_delivery")
                    )
                }

                // Innings completion button
                if (currentInn.isInningsComplete && match.currentInningsIndex == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    MintGlowButton(
                        text = "1st Innings Complete! Start 2nd Innings",
                        onClick = { showNextInningsDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_start_2nd_innings")
                    )
                }
            }
        }

        // Dismissal Selection Dialog
        if (showWicketDialog) {
            WicketDialog(
                strikerName = state!!.currentInnings.strikerName,
                nonStrikerName = state!!.currentInnings.nonStrikerName,
                onDismiss = { showWicketDialog = false },
                onConfirmWicket = { dismissal, outBatter, fielder, nextBatter ->
                    viewModel.scoreBall(
                        runsBat = 0,
                        isWicket = true,
                        dismissalType = dismissal,
                        dismissedPlayer = outBatter,
                        fielder = fielder,
                        nextBatter = nextBatter
                    )
                    showWicketDialog = false
                }
            )
        }

        // Delivery Audit Trail Dialog
        if (showAuditsDialog) {
            AlertDialog(
                onDismissRequest = { showAuditsDialog = false },
                containerColor = BackgroundDark,
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.8f),
                title = { Text("Delivery Audit Trail", fontWeight = FontWeight.Bold) },
                text = {
                    if (audits.isEmpty()) {
                        Text("No audit events recorded yet for this match.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(audits) { audit ->
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    borderColor = if (audit.action == "UNDONE") ErrorCrimson.copy(alpha = 0.5f) else GlassBorder
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = audit.action,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (audit.action == "UNDONE") ErrorCrimson else MintPrimary
                                        )
                                        Text(
                                            text = audit.scorerName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = audit.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAuditsDialog = false }) {
                        Text("Close", color = MintPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Change Bowler Dialog
        if (showChangeBowlerDialog) {
            var bowlerInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showChangeBowlerDialog = false },
                containerColor = BackgroundDark,
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.8f),
                title = { Text("Select Bowler", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter next bowler's name:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = bowlerInput,
                            onValueChange = { bowlerInput = it },
                            label = { Text("Bowler Name", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = MintPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_new_bowler")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (bowlerInput.isNotBlank()) {
                                viewModel.changeBowler(bowlerInput.trim())
                                showChangeBowlerDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_bowler_change_btn")
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangeBowlerDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // Start 2nd Innings Dialog
        if (showNextInningsDialog) {
            var op1 by remember { mutableStateOf("Batter 1") }
            var op2 by remember { mutableStateOf("Batter 2") }
            var bw1 by remember { mutableStateOf("Bowler 1") }
            AlertDialog(
                onDismissRequest = { showNextInningsDialog = false },
                containerColor = BackgroundDark,
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.8f),
                title = { Text("Start 2nd Innings", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Target: ${state!!.innings1.totalRuns + 1} runs",
                            color = ChampagneHighlight,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = op1,
                            onValueChange = { op1 = it },
                            label = { Text("Opening Striker", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = op2,
                            onValueChange = { op2 = it },
                            label = { Text("Opening Non-Striker", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bw1,
                            onValueChange = { bw1 = it },
                            label = { Text("Opening Bowler", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.startSecondInnings(
                                team2Batters = listOf(op1.trim(), op2.trim()),
                                team1Bowlers = listOf(bw1.trim())
                            )
                            showNextInningsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Begin Chase", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNextInningsDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}

@Composable
fun BatterRow(
    name: String,
    isStriker: Boolean,
    runs: Int,
    balls: Int,
    fours: Int,
    sixes: Int,
    strikeRate: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            if (isStriker) {
                Icon(
                    imageVector = Icons.Default.SportsCricket,
                    contentDescription = "On Strike",
                    tint = MintPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = name + (if (isStriker) " *" else ""),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isStriker) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isStriker) Color.White else Color.White.copy(alpha = 0.75f)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$runs (${balls}b)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isStriker) MintPrimary else Color.White
            )
            Text(
                text = "4s:$fours • 6s:$sixes • SR:${String.format("%.1f", strikeRate)}",
                style = MaterialTheme.typography.labelSmall,
                color = ChampagneHighlight,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun BallBubble(delivery: DeliveryEvent) {
    val (bgColor, borderCol, text, textCol) = when {
        delivery.isWicket -> Quadruple(ErrorCrimson.copy(alpha = 0.3f), ErrorCrimson, "W", ErrorCrimson)
        delivery.extraType == ExtraType.WIDE -> Quadruple(ChampagneHighlight.copy(alpha = 0.2f), ChampagneHighlight, "Wd", ChampagneHighlight)
        delivery.extraType == ExtraType.NO_BALL -> Quadruple(ChampagneHighlight.copy(alpha = 0.25f), ChampagneHighlight, "Nb", ChampagneHighlight)
        delivery.runsBat == 6 -> Quadruple(MintPrimary.copy(alpha = 0.25f), MintPrimary, "6", MintPrimary)
        delivery.runsBat == 4 -> Quadruple(MintPrimary.copy(alpha = 0.18f), MintPrimary.copy(alpha = 0.6f), "4", MintPrimary)
        delivery.runsBat == 0 -> Quadruple(Color.White.copy(alpha = 0.05f), GlassBorder, "•", Color.White.copy(alpha = 0.5f))
        else -> Quadruple(Color.White.copy(alpha = 0.08f), GlassBorder, "${delivery.runsBat}", Color.White)
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderCol, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textCol,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun WicketDialog(
    strikerName: String,
    nonStrikerName: String,
    onDismiss: () -> Unit,
    onConfirmWicket: (DismissalType, String, String?, String) -> Unit
) {
    var selectedDismissal by remember { mutableStateOf(DismissalType.BOWLED) }
    var outBatter by remember { mutableStateOf(strikerName) }
    var fielderName by remember { mutableStateOf("") }
    var nextBatterName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.8f),
        title = {
            Text("Fall of Wicket", fontWeight = FontWeight.Bold, color = ErrorCrimson)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Dismissal Type:", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(6.dp))
                DismissalType.values().take(7).forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDismissal = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (selectedDismissal == type) MintPrimary else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MintPrimary),
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type.displayName, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Batter Dismissed:", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { outBatter = strikerName }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (outBatter == strikerName) MintPrimary else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MintPrimary),
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strikerName, color = Color.White)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { outBatter = nonStrikerName }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (outBatter == nonStrikerName) MintPrimary else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MintPrimary),
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(nonStrikerName, color = Color.White)
                    }
                }

                if (selectedDismissal in listOf(DismissalType.CAUGHT, DismissalType.RUN_OUT, DismissalType.STUMPED)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fielderName,
                        onValueChange = { fielderName = it },
                        label = { Text("Fielder / Keeper Name", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_fielder_name")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nextBatterName,
                    onValueChange = { nextBatterName = it },
                    label = { Text("Next Incoming Batter Name", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("e.g. Surya", color = Color.White.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MintPrimary,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_next_batter")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nextName = if (nextBatterName.isNotBlank()) nextBatterName.trim() else "New Batter"
                    onConfirmWicket(selectedDismissal, outBatter, fielderName.ifBlank { null }, nextName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorCrimson),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_wicket_btn")
            ) {
                Text("Confirm Out", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}
