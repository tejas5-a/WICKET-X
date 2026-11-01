package com.example.ui.screens.tournaments

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.MintGlowButton
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MintPrimary
import com.example.ui.viewmodel.TournamentsViewModel

@Composable
fun CreateTournamentScreen(
    viewModel: TournamentsViewModel,
    onNavigateBack: () -> Unit,
    onTournamentCreated: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Wankhede & Gymkhana Grounds") }
    var selectedFormat by remember { mutableStateOf("T20") }
    var newTeamInput by remember { mutableStateOf("") }

    val teams = remember {
        mutableStateListOf(
            "Mumbai Warriors CC",
            "Delhi Challengers",
            "Rajasthan Strikers",
            "Gujarat Super Titans"
        )
    }

    val formats = listOf("T10", "T20", "T30", "T40", "T50", "Custom")

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "Create Cricket League",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "League Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tournament Name", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = { Text("e.g. Grassroots Champions Cup 2026", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MintPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tournament_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Venue / City", color = Color.White.copy(alpha = 0.7f)) },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = ChampagneHighlight)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tournament_location"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = MintPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Match Format",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        formats.take(4).forEach { format ->
                            val isSelected = selectedFormat == format
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MintPrimary else Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MintPrimary else GlassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFormat = format }
                            ) {
                                Text(
                                    text = format,
                                    color = if (isSelected) BackgroundDark else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Participating Teams (${teams.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Min 2 teams",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChampagneHighlight
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTeamInput,
                            onValueChange = { newTeamInput = it },
                            placeholder = { Text("Add custom team name", color = Color.White.copy(alpha = 0.4f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_new_team_name")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTeamInput.isNotBlank()) {
                                    teams.add(newTeamInput.trim())
                                    newTeamInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = BackgroundDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_add_team")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    teams.forEachIndexed { index, team ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                                        .size(28.dp)
                                        .background(MintPrimary.copy(alpha = 0.18f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = MintPrimary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = team,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White
                                )
                            }

                            if (teams.size > 2) {
                                IconButton(
                                    onClick = { teams.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                MintGlowButton(
                    text = "Generate Round Robin Fixtures & Points Table",
                    onClick = {
                        if (name.isNotBlank() && teams.size >= 2) {
                            viewModel.createTournament(
                                name = name,
                                format = selectedFormat,
                                location = location,
                                teams = teams.toList(),
                                onCreated = onTournamentCreated
                            )
                        }
                    },
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_submit_create_tournament")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
