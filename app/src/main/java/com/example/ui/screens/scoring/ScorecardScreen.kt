package com.example.ui.screens.scoring

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.example.domain.engine.InningsState
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MatchScoringViewModel

@Composable
fun ScorecardScreen(
    matchId: String,
    viewModel: MatchScoringViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.engineState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "Match Scorecard",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )

            if (isLoading || state == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MintPrimary)
                }
                return@Column
            }

            val match = state!!
            val inn1 = match.innings1
            val inn2 = match.innings2

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
            ) {
                // Match Header Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MintPrimary.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${match.teamAName} vs ${match.teamBName}",
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        GlassBadge(
                            text = if (match.status == "live") "LIVE SYNCED" else "OFFICIAL",
                            textColor = MintPrimary,
                            backgroundColor = MintPrimary.copy(alpha = 0.15f),
                            borderColor = MintPrimary.copy(alpha = 0.35f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${match.matchFormat} Match • ${match.venue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = match.resultSummary.ifBlank { "Match in progress" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (match.status == "completed") MintPrimary else ChampagneHighlight,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Innings Switcher Tabs with Dark Glass Styling
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
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "${inn1.battingTeamName} (${inn1.totalRuns}/${inn1.wickets})",
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = if (selectedTab == 0) MintPrimary else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = inn2?.let { "${it.battingTeamName} (${it.totalRuns}/${it.wickets})" } ?: "2nd Innings",
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = if (selectedTab == 1) MintPrimary else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val currentDisplayInnings = if (selectedTab == 0 || inn2 == null) inn1 else inn2
                InningsScorecardContent(currentDisplayInnings)
            }
        }
    }
}

@Composable
fun InningsScorecardContent(innings: InningsState) {
    // Batting Table Glass Card
    GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("batting_scorecard_table")
    ) {
        Text(
            text = "Batting — ${innings.battingTeamName}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text("Batter", modifier = Modifier.weight(2.4f), color = ChampagneHighlight, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("R", modifier = Modifier.weight(0.8f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("B", modifier = Modifier.weight(0.8f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("4s", modifier = Modifier.weight(0.7f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("6s", modifier = Modifier.weight(0.7f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("SR", modifier = Modifier.weight(1.2f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(6.dp))

        innings.batters.values.forEach { batter ->
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(2.4f)
                            .padding(end = 6.dp)
                    ) {
                        Text(
                            text = batter.playerName,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = batter.dismissalText,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.55f))
                        )
                    }
                    Text("${batter.runs}", modifier = Modifier.weight(0.8f), color = MintPrimary, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    Text("${batter.ballsFaced}", modifier = Modifier.weight(0.8f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End)
                    Text("${batter.fours}", modifier = Modifier.weight(0.7f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End)
                    Text("${batter.sixes}", modifier = Modifier.weight(0.7f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End)
                    Text(String.format("%.1f", batter.strikeRate), modifier = Modifier.weight(1.2f), color = ChampagneHighlight, textAlign = TextAlign.End)
                }
                HorizontalDivider(modifier = Modifier.padding(top = 6.dp), color = GlassBorder)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Extras & Total
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Extras", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
            Text(
                text = "${innings.extras.total} (w ${innings.extras.wides}, nb ${innings.extras.noBalls}, b ${innings.extras.byes}, lb ${innings.extras.legByes})",
                color = ChampagneHighlight,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = "${innings.totalRuns}/${innings.wickets} (${innings.oversFormatted} ov)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MintPrimary)
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Fall of Wickets
    if (innings.fallOfWickets.isNotEmpty()) {
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fall of Wickets", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            val fowText = innings.fallOfWickets.joinToString(separator = ", ") { fow ->
                "${fow.teamScore}/${fow.wicketNumber} (${fow.batterName}, ${fow.overs} ov)"
            }
            Text(
                text = fowText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
    }

    // Bowling Table Glass Card
    GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("bowling_scorecard_table")
    ) {
        Text(
            text = "Bowling — ${innings.bowlingTeamName}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text("Bowler", modifier = Modifier.weight(2.5f), color = ChampagneHighlight, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("O", modifier = Modifier.weight(0.8f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("M", modifier = Modifier.weight(0.7f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("R", modifier = Modifier.weight(0.8f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("W", modifier = Modifier.weight(0.8f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Text("Econ", modifier = Modifier.weight(1.2f), color = ChampagneHighlight, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(6.dp))

        innings.bowlers.values.forEach { bowler ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(bowler.playerName, modifier = Modifier.weight(2.5f), color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(bowler.oversString, modifier = Modifier.weight(0.8f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End)
                Text("${bowler.maidens}", modifier = Modifier.weight(0.7f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End)
                Text("${bowler.runsConceded}", modifier = Modifier.weight(0.8f), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.End)
                Text("${bowler.wickets}", modifier = Modifier.weight(0.8f), color = MintPrimary, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                Text(String.format("%.1f", bowler.economy), modifier = Modifier.weight(1.2f), color = ChampagneHighlight, textAlign = TextAlign.End)
            }
            HorizontalDivider(color = GlassBorder)
        }
    }
}
