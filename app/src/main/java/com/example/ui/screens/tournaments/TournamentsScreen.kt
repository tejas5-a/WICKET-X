package com.example.ui.screens.tournaments

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MintPrimary
import com.example.ui.viewmodel.TournamentsViewModel

@Composable
fun TournamentsScreen(
    viewModel: TournamentsViewModel,
    onSearchClick: () -> Unit,
    onTournamentClick: (String) -> Unit = {}
) {
    val tournaments by viewModel.tournaments.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    val standings = viewModel.getStandingsForTournament("tour_premier")

    GlassScaffoldBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                WicketXTopBar(
                    title = "Tournaments & Leagues",
                    onSearchClick = onSearchClick
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
                ) {
                    // Points Table Card
                    item {
                        Text(
                            text = "Premier Cup Standings (Points Table)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        GlassCard(
                            modifier = Modifier.fillMaxWidth().testTag("points_table_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Team", modifier = Modifier.weight(3f), color = ChampagneHighlight, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("P", modifier = Modifier.weight(0.7f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("W", modifier = Modifier.weight(0.7f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("L", modifier = Modifier.weight(0.7f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("NRR", modifier = Modifier.weight(1.3f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Pts", modifier = Modifier.weight(0.8f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            standings.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}. ${item.teamName}",
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(3f)
                                            .padding(end = 4.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text("${item.played}", modifier = Modifier.weight(0.7f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
                                    Text("${item.won}", modifier = Modifier.weight(0.7f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
                                    Text("${item.lost}", modifier = Modifier.weight(0.7f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = String.format("%+.3f", item.netRunRate),
                                        modifier = Modifier.weight(1.3f),
                                        textAlign = TextAlign.End,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (item.netRunRate >= 0) MintPrimary else ErrorCrimson
                                        )
                                    )
                                    Text(
                                        text = "${item.points}",
                                        modifier = Modifier.weight(0.8f),
                                        textAlign = TextAlign.End,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MintPrimary)
                                    )
                                }
                                HorizontalDivider(color = GlassBorder)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Active & Upcoming Tournaments",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(tournaments) { tour ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onTournamentClick(tour.id) }
                                .testTag("tour_item_${tour.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tour.name,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                val isAct = tour.status == "active"
                                GlassBadge(
                                    text = tour.status.uppercase(),
                                    textColor = if (isAct) MintPrimary else ChampagneHighlight,
                                    backgroundColor = if (isAct) MintPrimary.copy(alpha = 0.18f) else ChampagneHighlight.copy(alpha = 0.18f),
                                    borderColor = if (isAct) MintPrimary.copy(alpha = 0.35f) else ChampagneHighlight.copy(alpha = 0.35f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${tour.format} • ${tour.teamCount} Teams • ${tour.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            // Floating Action Button
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MintPrimary,
                contentColor = BackgroundDark,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 84.dp)
                    .testTag("fab_create_tournament")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create League")
            }
        }

        if (showCreateDialog) {
            var name by remember { mutableStateOf("") }
            var format by remember { mutableStateOf("T20") }
            var loc by remember { mutableStateOf("") }
            var teamsCount by remember { mutableStateOf("8") }

            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = BackgroundDark,
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.8f),
                title = { Text("Create New Tournament", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Tournament Name", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = MintPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_tour_name")
                        )
                        OutlinedTextField(
                            value = format,
                            onValueChange = { format = it },
                            label = { Text("Format (e.g. T20, T10)", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = loc,
                            onValueChange = { loc = it },
                            label = { Text("City / Venue", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = teamsCount,
                            onValueChange = { teamsCount = it },
                            label = { Text("Number of Teams", color = Color.White.copy(alpha = 0.7f)) },
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
                                viewModel.createTournament(
                                    name = name.trim(),
                                    format = format.trim(),
                                    location = loc.trim(),
                                    teams = listOf("Mumbai Warriors CC", "Delhi Challengers", "Rajasthan Strikers", "Gujarat Super Titans")
                                )
                                showCreateDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_create_tour_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}
