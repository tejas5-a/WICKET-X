package com.example.ui.screens.scoring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.MintGlowButton
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MintPrimary

@Composable
fun MatchCreateScreen(
    onNavigateBack: () -> Unit,
    onProceedToToss: (teamA: String, teamB: String, overs: Int, format: String, venue: String) -> Unit
) {
    var teamAName by remember { mutableStateOf("Mumbai Warriors CC") }
    var teamBName by remember { mutableStateOf("Delhi Challengers") }
    var selectedFormat by remember { mutableStateOf("T20") }
    var totalOvers by remember { mutableStateOf("20") }
    var venue by remember { mutableStateOf("Gymkhana Ground, Mumbai") }

    val formats = listOf("T10" to 10, "T20" to 20, "T30" to 30, "T50" to 50)

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "New Cricket Match",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Teams & Format",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = teamAName,
                        onValueChange = { teamAName = it },
                        label = { Text("Team A Name (Bat / Bowl)", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_team_a"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = teamBName,
                        onValueChange = { teamBName = it },
                        label = { Text("Team B Name (Opposition)", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_team_b"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Match Format & Overs", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        formats.forEach { (fmt, ov) ->
                            val isSelected = selectedFormat == fmt
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MintPrimary else Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MintPrimary else GlassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedFormat = fmt
                                        totalOvers = ov.toString()
                                    }
                            ) {
                                Text(
                                    text = fmt,
                                    color = if (isSelected) BackgroundDark else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = totalOvers,
                        onValueChange = {
                            totalOvers = it.filter { ch -> ch.isDigit() }
                        },
                        label = { Text("Custom Overs Per Innings", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_total_overs"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Venue / Ground Name", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_venue"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                MintGlowButton(
                    text = "Proceed to Toss & Lineups",
                    onClick = {
                        val oversInt = totalOvers.toIntOrNull() ?: 20
                        onProceedToToss(
                            teamAName.trim(),
                            teamBName.trim(),
                            oversInt,
                            selectedFormat,
                            venue.trim()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_proceed_to_toss")
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
