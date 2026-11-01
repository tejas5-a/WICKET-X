package com.example.ui.screens.tournaments

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.tournament.TournamentFixture
import com.example.domain.tournament.TournamentPointsItem
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
import com.example.ui.viewmodel.TournamentsViewModel

@Composable
fun TournamentManageScreen(
    tournamentId: String,
    viewModel: TournamentsViewModel,
    onNavigateBack: () -> Unit,
    onScoreFixtureClick: (String) -> Unit
) {
    val tournament by viewModel.selectedTournament.collectAsState()
    val fixtures by viewModel.fixtures.collectAsState()
    val pointsTable by viewModel.pointsTable.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Points Table", "Fixtures Schedule", "Teams")

    LaunchedEffect(tournamentId) {
        viewModel.loadTournament(tournamentId)
    }

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = tournament?.name ?: "Manage Tournament",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            if (tournament == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MintPrimary)
                }
                return@Column
            }

            val tour = tournament!!

            // Tournament Overview Banner
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                borderColor = MintPrimary.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tour.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ChampagneHighlight
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${tour.location} • ${tour.format}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Status pill
                    val isAct = tour.status == "active"
                    GlassBadge(
                        text = tour.status.uppercase(),
                        textColor = if (isAct) MintPrimary else ChampagneHighlight,
                        backgroundColor = if (isAct) MintPrimary.copy(alpha = 0.18f) else ChampagneHighlight.copy(alpha = 0.18f),
                        borderColor = if (isAct) MintPrimary.copy(alpha = 0.35f) else ChampagneHighlight.copy(alpha = 0.35f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Update Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("upcoming", "active", "completed").forEach { status ->
                        val isSelected = tour.status == status
                        OutlinedButton(
                            onClick = { viewModel.updateTournamentStatus(tour.id, status) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MintPrimary else GlassBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MintPrimary.copy(alpha = 0.18f) else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = status.replaceFirstChar { it.uppercase() },
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MintPrimary else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Tab Navigation with Dark Glass Styling
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xCC131321),
                contentColor = MintPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MintPrimary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = if (selectedTab == index) MintPrimary else Color.White.copy(alpha = 0.65f),
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> PointsTableTab(pointsTable = pointsTable)
                1 -> FixturesScheduleTab(
                    fixtures = fixtures,
                    onScoreMatch = { onScoreFixtureClick(it) }
                )
                2 -> TeamsTab(teamsList = tour.teamsList)
            }
        }
    }
}

@Composable
fun PointsTableTab(pointsTable: List<TournamentPointsItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            // Table Header Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneHighlight,
                        modifier = Modifier.weight(0.5f)
                    )
                    Text(
                        text = "TEAM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneHighlight,
                        modifier = Modifier.weight(2.3f)
                    )
                    Text(
                        text = "P",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneHighlight,
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "W",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneHighlight,
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "L",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneHighlight,
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "PTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.Center,
                        color = MintPrimary
                    )
                    Text(
                        text = "NRR",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneHighlight,
                        modifier = Modifier.weight(1.3f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        items(pointsTable.mapIndexed { idx, item -> Pair(idx + 1, item) }) { (rank, item) ->
            val isTopQualifier = rank <= 2
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isTopQualifier) MintPrimary.copy(alpha = 0.35f) else GlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isTopQualifier) MintPrimary else Color.White,
                        modifier = Modifier.weight(0.5f)
                    )
                    Text(
                        text = item.teamName,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        modifier = Modifier
                            .weight(2.3f)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = "${item.played}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${item.won}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${item.lost}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${item.points}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MintPrimary,
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = item.nrrFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (item.nrr >= 0) MintPrimary else ErrorCrimson,
                        modifier = Modifier.weight(1.3f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun FixturesScheduleTab(
    fixtures: List<TournamentFixture>,
    onScoreMatch: (String) -> Unit
) {
    if (fixtures.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No fixtures scheduled yet", color = Color.White.copy(alpha = 0.6f))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(fixtures) { fix ->
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fix.roundName,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MintPrimary
                    )
                    val isComp = fix.status == "completed"
                    GlassBadge(
                        text = fix.status.uppercase(),
                        textColor = if (isComp) MintPrimary else ChampagneHighlight,
                        backgroundColor = if (isComp) MintPrimary.copy(alpha = 0.18f) else ChampagneHighlight.copy(alpha = 0.18f),
                        borderColor = if (isComp) MintPrimary.copy(alpha = 0.35f) else ChampagneHighlight.copy(alpha = 0.35f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fix.teamAName,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "vs",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = fix.teamBName,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                MintGlowButton(
                    text = if (fix.status == "completed") "View Scorecard" else "Score Match",
                    onClick = { onScoreMatch(fix.id) },
                    icon = Icons.Default.SportsCricket,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_score_fixture_${fix.id}")
                )
            }
        }
    }
}

@Composable
fun TeamsTab(teamsList: String) {
    val teams = teamsList.split(",").map { it.trim() }.filter { it.isNotBlank() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(teams) { team ->
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MintPrimary.copy(alpha = 0.18f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = team.take(2).uppercase(),
                            color = MintPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = team,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
