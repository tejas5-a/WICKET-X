package com.example.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MintPrimary
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun SearchScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onMatchClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val matches by viewModel.allMatches.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()

    val filteredMatches = if (searchQuery.isBlank()) emptyList() else matches.filter {
        it.teamAName.contains(searchQuery, ignoreCase = true) ||
            it.teamBName.contains(searchQuery, ignoreCase = true) ||
            it.venue.contains(searchQuery, ignoreCase = true)
    }

    val filteredTeams = if (searchQuery.isBlank()) emptyList() else teams.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.shortName.contains(searchQuery, ignoreCase = true) ||
            it.city.contains(searchQuery, ignoreCase = true)
    }

    val filteredTournaments = if (searchQuery.isBlank()) emptyList() else tournaments.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true)
    }

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "Search Cricket",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field"),
                    placeholder = { Text("Search matches, teams, grounds, leagues...", color = Color.White.copy(alpha = 0.45f)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MintPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MintPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = MintPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (searchQuery.isBlank()) {
                    Text(
                        text = "Type to search matches, teams, venues, and leagues across WicketX.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        if (filteredMatches.isNotEmpty()) {
                            item {
                                Text(
                                    "Matches (${filteredMatches.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MintPrimary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(filteredMatches) { match ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onMatchClick(match.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${match.teamAName} vs ${match.teamBName}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${match.matchFormat} • ${match.venue}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.65f)
                                            )
                                        }
                                        GlassBadge(
                                            text = match.status.uppercase(),
                                            textColor = if (match.status == "live") MintPrimary else ChampagneHighlight,
                                            backgroundColor = if (match.status == "live") MintPrimary.copy(alpha = 0.18f) else ChampagneHighlight.copy(alpha = 0.18f),
                                            borderColor = if (match.status == "live") MintPrimary.copy(alpha = 0.35f) else ChampagneHighlight.copy(alpha = 0.35f)
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredTeams.isNotEmpty()) {
                            item {
                                Text(
                                    "Teams (${filteredTeams.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ChampagneHighlight,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(filteredTeams) { team ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = team.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = team.city,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ChampagneHighlight
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredTournaments.isNotEmpty()) {
                            item {
                                Text(
                                    "Leagues & Tournaments (${filteredTournaments.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MintPrimary,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            items(filteredTournaments) { tour ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tour.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${tour.format} • ${tour.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }

                        if (filteredMatches.isEmpty() && filteredTeams.isEmpty() && filteredTournaments.isEmpty()) {
                            item {
                                Text(
                                    text = "No results found for \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
