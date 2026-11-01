package com.example.ui.screens.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassScaffoldBackground
import com.example.ui.components.WicketXTopBar
import com.example.ui.screens.home.GlassLiveMatchCard
import com.example.ui.screens.home.GlassRecentMatchCard
import com.example.ui.theme.MintPrimary

@Composable
fun LiveMatchesScreen(
    viewModel: com.example.ui.viewmodel.HomeViewModel,
    onViewScorecard: (String) -> Unit,
    onScoreLive: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Live Now", "All Matches", "Completed")

    val displayedMatches = when (selectedTab) {
        0 -> liveMatches
        1 -> allMatches
        2 -> allMatches.filter { it.status == "completed" }
        else -> liveMatches
    }

    GlassScaffoldBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WicketXTopBar(
                title = "Cricket Matches",
                onSearchClick = onSearchClick
            )

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
                                color = if (selectedTab == index) MintPrimary else Color.White.copy(alpha = 0.65f),
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_matches_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (displayedMatches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matches found in this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                ) {
                    items(displayedMatches) { match ->
                        if (match.status == "live") {
                            GlassLiveMatchCard(
                                match = match,
                                onViewScorecard = { onViewScorecard(match.id) },
                                onScoreLive = { onScoreLive(match.id) }
                            )
                        } else {
                            GlassRecentMatchCard(
                                match = match,
                                onClick = { onViewScorecard(match.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
