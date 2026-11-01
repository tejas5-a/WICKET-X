package com.example.ui.screens.scoring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.MintGlowButton
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MintPrimary
import com.example.ui.viewmodel.MatchScoringViewModel

@Composable
fun TossScreen(
    teamAName: String,
    teamBName: String,
    overs: Int,
    format: String,
    venue: String,
    viewModel: MatchScoringViewModel,
    onNavigateBack: () -> Unit,
    onMatchStarted: (String) -> Unit
) {
    var tossWinner by remember { mutableStateOf(teamAName) }
    var tossDecision by remember { mutableStateOf("bat") }

    var striker by remember { mutableStateOf("Rohit Sharma") }
    var nonStriker by remember { mutableStateOf("Ishan Kishan") }
    var bowler by remember { mutableStateOf("Jasprit Bumrah") }

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "Toss & Openers",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Toss Section
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MintPrimary.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Toss",
                                tint = MintPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Who won the toss?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(teamAName, teamBName).forEach { team ->
                            val isSelected = tossWinner == team
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MintPrimary else Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MintPrimary else GlassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tossWinner = team }
                                    .testTag("toss_winner_${team.take(4)}")
                            ) {
                                Text(
                                    text = team,
                                    color = if (isSelected) BackgroundDark else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Elected to:", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("bat" to "Bat First", "bowl" to "Bowl First").forEach { (decision, label) ->
                            val isSelected = tossDecision == decision
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ChampagneHighlight else Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ChampagneHighlight else GlassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tossDecision = decision }
                                    .testTag("toss_decision_$decision")
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BackgroundDark else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Openers Selection
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Opening Batters & Bowler",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = striker,
                        onValueChange = { striker = it },
                        label = { Text("Striker (On Strike)", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_striker"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nonStriker,
                        onValueChange = { nonStriker = it },
                        label = { Text("Non-Striker", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_non_striker"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bowler,
                        onValueChange = { bowler = it },
                        label = { Text("Opening Bowler", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_opening_bowler"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                MintGlowButton(
                    text = "Start Ball-by-Ball Scoring",
                    onClick = {
                        viewModel.startNewMatch(
                            teamAName = teamAName,
                            teamBName = teamBName,
                            totalOvers = overs,
                            matchFormat = format,
                            venue = venue,
                            tossWinner = tossWinner,
                            tossDecision = tossDecision,
                            team1Batters = listOf(striker.trim(), nonStriker.trim()),
                            team2Bowlers = listOf(bowler.trim()),
                            onMatchCreated = onMatchStarted
                        )
                    },
                    icon = Icons.Default.SportsCricket,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_start_match_scoring")
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
