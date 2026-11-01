package com.example.ui.screens.teams

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MintPrimary
import com.example.ui.viewmodel.TeamsViewModel

@Composable
fun TeamsScreen(
    viewModel: TeamsViewModel,
    onNavigateBack: () -> Unit = {},
    showBack: Boolean = false,
    onSearchClick: () -> Unit
) {
    val teams by viewModel.teams.collectAsState()
    val allPlayers by viewModel.allPlayers.collectAsState()
    val selectedTeam by viewModel.selectedTeam.collectAsState()

    var showCreateTeamDialog by remember { mutableStateOf(false) }

    GlassScaffoldBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                WicketXTopBar(
                    title = if (selectedTeam != null) selectedTeam!!.name else "Teams & Rosters",
                    canNavigateBack = selectedTeam != null || showBack,
                    onNavigateBack = {
                        if (selectedTeam != null) {
                            viewModel.selectTeam(teams.firstOrNull() ?: return@WicketXTopBar)
                        } else {
                            onNavigateBack()
                        }
                    },
                    onSearchClick = onSearchClick
                )

                if (selectedTeam != null) {
                    // Team Detail View with Squad Roster
                    val team = selectedTeam!!
                    val squad = allPlayers.filter { it.teamName == team.name }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                    ) {
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = MintPrimary.copy(alpha = 0.35f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(MintPrimary.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = MintPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = team.name,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Captain: ${team.captainName} • ${team.city}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ChampagneHighlight
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("Matches", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
                                        Text("${team.matchesPlayed}", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("Won", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
                                        Text("${team.matchesWon}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MintPrimary))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("Squad Size", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
                                        Text("${squad.size}", color = ChampagneHighlight, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Squad Players",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (squad.isEmpty()) {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "No players assigned yet. Add players to this team.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            items(squad) { player ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
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
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.08f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MintPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = player.name,
                                                    maxLines = 2,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${player.role} • ${player.battingStyle}",
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${player.runs} Runs",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${player.wickets} Wkts",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MintPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Teams List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                    ) {
                        items(teams) { team ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable { viewModel.selectTeam(team) }
                                    .testTag("team_card_${team.id}")
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
                                        Surface(
                                            shape = CircleShape,
                                            color = MintPrimary.copy(alpha = 0.18f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MintPrimary.copy(alpha = 0.35f)),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = team.shortName.take(3),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MintPrimary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = team.name,
                                                maxLines = 2,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Capt: ${team.captainName} • ${team.city}",
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.65f)
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${team.matchesWon}/${team.matchesPlayed} Won",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Tap for Squad",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MintPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showCreateTeamDialog = true },
                containerColor = MintPrimary,
                contentColor = BackgroundDark,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 84.dp)
                    .testTag("fab_create_team")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Team")
            }
        }

        if (showCreateTeamDialog) {
            var name by remember { mutableStateOf("") }
            var shortName by remember { mutableStateOf("") }
            var city by remember { mutableStateOf("") }
            var captain by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreateTeamDialog = false },
                containerColor = BackgroundDark,
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.8f),
                title = { Text("Register New Team", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Team Name", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = MintPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_create_team_name")
                        )
                        OutlinedTextField(
                            value = shortName,
                            onValueChange = { shortName = it },
                            label = { Text("Short Acronym (e.g. MWCC)", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City / Region", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = captain,
                            onValueChange = { captain = it },
                            label = { Text("Captain Name", color = Color.White.copy(alpha = 0.7f)) },
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
                            if (name.isNotBlank()) {
                                viewModel.createTeam(
                                    name = name.trim(),
                                    shortName = shortName.ifBlank { name.take(4) }.trim(),
                                    city = city.trim(),
                                    captainName = captain.trim()
                                )
                                showCreateTeamDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_create_team_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Register Team", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateTeamDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}
