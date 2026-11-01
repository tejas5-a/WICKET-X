package com.example.ui.screens.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MatchEntity
import com.example.data.local.TournamentEntity
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.MintGlowButton
import com.example.ui.components.WicketXTopBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintGlow
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateMatchClick: () -> Unit,
    onMatchClick: (String) -> Unit,
    onScoreMatchClick: (String) -> Unit,
    onTeamsClick: () -> Unit,
    onTournamentsClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val tournaments by viewModel.tournaments.collectAsState()

    val completedMatches = allMatches.filter { it.status == "completed" }

    GlassScaffoldBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            WicketXTopBar(
                title = "WICKETX",
                onSearchClick = onSearchClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Hero Brand Glass Banner
                item {
                    GlassHeroBanner(onCreateMatchClick = onCreateMatchClick)
                }

                // Quick Actions Glass Row
                item {
                    GlassQuickActionsSection(
                        onCreateMatch = onCreateMatchClick,
                        onTeams = onTeamsClick,
                        onTournaments = onTournamentsClick,
                        onCommunity = onCommunityClick
                    )
                }

                // Live Matches Section
                if (liveMatches.isNotEmpty()) {
                    item {
                        GlassSectionHeader(
                            title = "Live Matches",
                            badge = "LIVE",
                            badgeColor = MintPrimary
                        )
                    }
                    items(liveMatches) { match ->
                        GlassLiveMatchCard(
                            match = match,
                            onViewScorecard = { onMatchClick(match.id) },
                            onScoreLive = { onScoreMatchClick(match.id) }
                        )
                    }
                }

                // Featured Leagues & Tournaments
                if (tournaments.isNotEmpty()) {
                    item {
                        GlassSectionHeader(
                            title = "Featured Leagues & Tournaments",
                            actionText = "See All",
                            onAction = onTournamentsClick
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(tournaments) { tour ->
                                GlassTournamentCard(tournament = tour, onClick = onTournamentsClick)
                            }
                        }
                    }
                }

                // Recent Results
                if (completedMatches.isNotEmpty()) {
                    item {
                        GlassSectionHeader(title = "Recent Results")
                    }
                    items(completedMatches) { match ->
                        GlassRecentMatchCard(
                            match = match,
                            onClick = { onMatchClick(match.id) }
                        )
                    }
                }

                // Grassroots Feed Snippet
                item {
                    GlassCricketActivitySection(onCommunityClick = onCommunityClick)
                }
            }
        }
    }
}

@Composable
fun GlassHeroBanner(onCreateMatchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x2E46F0D2), // Mint translucent tint
                        Color(0x1A131321),
                        Color(0x22FBE2B4)  // Champagne subtle tint
                    )
                )
            )
            .border(1.dp, MintBorder, RoundedCornerShape(22.dp))
            .padding(20.dp)
            .testTag("hero_banner_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassBadge(
                    text = "100% FREE",
                    color = MintPrimary,
                    backgroundColor = MintPrimary.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Grassroots Cricket Platform",
                    color = ChampagneHighlight,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Free cricket scoring.\nReal stats. Live moments.",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            MintGlowButton(
                text = "Start Scoring Match",
                onClick = onCreateMatchClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Match",
                        tint = BackgroundDark,
                        modifier = Modifier.size(18.dp)
                    )
                },
                testTag = "hero_start_match_button"
            )
        }
    }
}

@Composable
fun GlassQuickActionsSection(
    onCreateMatch: () -> Unit,
    onTeams: () -> Unit,
    onTournaments: () -> Unit,
    onCommunity: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassQuickActionButton(
            label = "New Match",
            icon = Icons.Default.SportsCricket,
            accentColor = MintPrimary,
            tag = "action_new_match",
            modifier = Modifier.weight(1f),
            onClick = onCreateMatch
        )
        GlassQuickActionButton(
            label = "Teams",
            icon = Icons.Default.Groups,
            accentColor = ChampagneHighlight,
            tag = "action_teams",
            modifier = Modifier.weight(1f),
            onClick = onTeams
        )
        GlassQuickActionButton(
            label = "Leagues",
            icon = Icons.Default.EmojiEvents,
            accentColor = MintPrimary,
            tag = "action_tournaments",
            modifier = Modifier.weight(1f),
            onClick = onTournaments
        )
        GlassQuickActionButton(
            label = "Feed",
            icon = Icons.Default.Public,
            accentColor = ChampagneHighlight,
            tag = "action_community",
            modifier = Modifier.weight(1f),
            onClick = onCommunity
        )
    }
}

@Composable
fun GlassQuickActionButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimary
        )
    }
}

@Composable
fun GlassSectionHeader(
    title: String,
    badge: String? = null,
    badgeColor: Color = MintPrimary,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = title,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                GlassBadge(text = badge, color = badgeColor)
            }
        }
        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MintPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
fun GlassLiveMatchCard(
    match: MatchEntity,
    onViewScorecard: () -> Unit,
    onScoreLive: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("match_card_${match.id}"),
        isMintHighlighted = true
    ) {
        // Header: Format, venue, LIVE indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                GlassBadge(
                    text = "LIVE",
                    color = MintPrimary,
                    backgroundColor = MintPrimary.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${match.matchFormat} • ${match.venue}",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.teamAName,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "${match.teamAScore}/${match.teamAWickets} (${match.teamAOvers})",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.teamBName,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (match.currentInnings == 2) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (match.currentInnings == 2) TextPrimary else TextSecondary
            )
            Text(
                text = if (match.currentInnings == 2) "${match.teamBScore}/${match.teamBWickets} (${match.teamBOvers})" else "Yet to bat",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (match.currentInnings == 2) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (match.currentInnings == 2) MintPrimary else TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = match.resultSummary,
            style = MaterialTheme.typography.bodySmall.copy(color = ChampagneHighlight, fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onViewScorecard,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_scorecard_${match.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Text(
                    text = "Scorecard",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onScoreLive,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_score_live_${match.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintPrimary,
                    contentColor = BackgroundDark
                )
            ) {
                Text(
                    text = "Score Match",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GlassRecentMatchCard(match: MatchEntity, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("recent_match_${match.id}")
    ) {
        Text(
            text = "${match.matchFormat} • ${match.venue}",
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.teamAName,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
            Text(
                text = "${match.teamAScore}/${match.teamAWickets} (${match.teamAOvers})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.teamBName,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
            Text(
                text = "${match.teamBScore}/${match.teamBWickets} (${match.teamBOvers})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = match.resultSummary,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MintPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun GlassTournamentCard(tournament: TournamentEntity, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .width(230.dp)
            .clickable(onClick = onClick)
            .testTag("tournament_card_${tournament.id}")
    ) {
        GlassBadge(
            text = tournament.format,
            color = MintPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = tournament.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${tournament.teamCount} Teams • ${tournament.location}",
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun GlassCricketActivitySection(onCommunityClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        GlassSectionHeader(
            title = "Grassroots Cricket Feed",
            actionText = "Join",
            onAction = onCommunityClick
        )
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onCommunityClick)
                .testTag("cricket_feed_preview")
        ) {
            Text(
                text = "Milestone Reached",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MintPrimary
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rohit Sharma reached 50 runs off 28 deliveries against Delhi Challengers at Gymkhana!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "15 minutes ago • Gymkhana Grounds",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
