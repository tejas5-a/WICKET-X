package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.data.auth.AuthManager
import com.example.ui.navigation.Routes
import com.example.ui.navigation.Screen
import com.example.ui.screens.auth.PhoneAuthScreen
import com.example.ui.screens.community.CommunityScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.live.LiveMatchesScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.scorer.ScorerDashboardScreen
import com.example.ui.screens.scoring.MatchCreateScreen
import com.example.ui.screens.scoring.ScorecardScreen
import com.example.ui.screens.scoring.ScorerScreen
import com.example.ui.screens.scoring.TossScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.teams.TeamsScreen
import com.example.ui.screens.tournaments.CreateTournamentScreen
import com.example.ui.screens.tournaments.TournamentManageScreen
import com.example.ui.screens.tournaments.TournamentsScreen
import com.example.ui.theme.WicketXTheme
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.MatchScoringViewModel
import com.example.ui.viewmodel.ScorerAuthViewModel
import com.example.ui.viewmodel.TeamsViewModel
import com.example.ui.viewmodel.TournamentsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager
    private val homeViewModel: HomeViewModel by viewModels()
    private val scoringViewModel: MatchScoringViewModel by viewModels()
    private val teamsViewModel: TeamsViewModel by viewModels()
    private val tournamentsViewModel: TournamentsViewModel by viewModels()
    private val scorerAuthViewModel: ScorerAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager = AuthManager(applicationContext)
        authManager.attemptAutoSignIn(lifecycleScope)

        setContent {
            WicketXTheme {
                WicketXApp(
                    authManager = authManager,
                    homeViewModel = homeViewModel,
                    scoringViewModel = scoringViewModel,
                    teamsViewModel = teamsViewModel,
                    tournamentsViewModel = tournamentsViewModel,
                    scorerAuthViewModel = scorerAuthViewModel
                )
            }
        }
    }
}

@Composable
fun WicketXApp(
    authManager: AuthManager,
    homeViewModel: HomeViewModel,
    scoringViewModel: MatchScoringViewModel,
    teamsViewModel: TeamsViewModel,
    tournamentsViewModel: TournamentsViewModel,
    scorerAuthViewModel: ScorerAuthViewModel
) {
    var currentScreen by remember { mutableStateOf(Routes.HOME) }
    var activeMatchId by remember { mutableStateOf<String?>(null) }
    var activeTournamentId by remember { mutableStateOf<String>("tour_premier") }

    // Pending match creation details for toss flow
    var pendingTeamA by remember { mutableStateOf("Mumbai Warriors CC") }
    var pendingTeamB by remember { mutableStateOf("Delhi Challengers") }
    var pendingOvers by remember { mutableStateOf(20) }
    var pendingFormat by remember { mutableStateOf("T20") }
    var pendingVenue by remember { mutableStateOf("Wankhede Stadium, Mumbai") }

    val isTopLevelScreen = currentScreen in listOf(
        Routes.HOME, Routes.LIVE, Routes.SCORE, Routes.TOURNAMENTS, Routes.PROFILE
    )

    // Back handling
    if (!isTopLevelScreen) {
        BackHandler {
            currentScreen = Routes.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            if (isTopLevelScreen) {
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBar(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        tonalElevation = 0.dp,
                        windowInsets = androidx.compose.material3.NavigationBarDefaults.windowInsets
                    ) {
                        Screen.bottomNavItems.forEach { screen ->
                            val selected = currentScreen == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (screen.route == Routes.SCORE) {
                                        currentScreen = Routes.SCORER_DASHBOARD
                                    } else {
                                        currentScreen = screen.route
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_${screen.route}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier.padding(innerPadding),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                Routes.HOME -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onCreateMatchClick = { currentScreen = Routes.MATCH_CREATE },
                        onMatchClick = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORECARD
                        },
                        onScoreMatchClick = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORER
                        },
                        onTeamsClick = { currentScreen = Routes.TEAMS },
                        onTournamentsClick = { currentScreen = Routes.TOURNAMENTS },
                        onCommunityClick = { currentScreen = Routes.COMMUNITY },
                        onSearchClick = { currentScreen = Routes.SEARCH }
                    )
                }

                Routes.LIVE -> {
                    LiveMatchesScreen(
                        viewModel = homeViewModel,
                        onViewScorecard = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORECARD
                        },
                        onScoreLive = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORER
                        },
                        onSearchClick = { currentScreen = Routes.SEARCH }
                    )
                }

                Routes.SCORER_DASHBOARD -> {
                    ScorerDashboardScreen(
                        authViewModel = scorerAuthViewModel,
                        tournamentsViewModel = tournamentsViewModel,
                        homeViewModel = homeViewModel,
                        onNavigateBack = { currentScreen = Routes.HOME },
                        onCreateTournamentClick = { currentScreen = Routes.CREATE_TOURNAMENT },
                        onCreateMatchClick = { currentScreen = Routes.MATCH_CREATE },
                        onStartScoringClick = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORER
                        },
                        onManageTournamentClick = { tournamentId ->
                            activeTournamentId = tournamentId
                            currentScreen = Routes.TOURNAMENT_MANAGE
                        },
                        onViewScorecardClick = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORECARD
                        },
                        onPhoneAuthClick = { currentScreen = Routes.PHONE_AUTH }
                    )
                }

                Routes.PHONE_AUTH -> {
                    PhoneAuthScreen(
                        viewModel = scorerAuthViewModel,
                        onNavigateBack = { currentScreen = Routes.PROFILE },
                        onAuthSuccess = { currentScreen = Routes.SCORER_DASHBOARD }
                    )
                }

                Routes.CREATE_TOURNAMENT -> {
                    CreateTournamentScreen(
                        viewModel = tournamentsViewModel,
                        onNavigateBack = { currentScreen = Routes.SCORER_DASHBOARD },
                        onTournamentCreated = { tournamentId ->
                            activeTournamentId = tournamentId
                            currentScreen = Routes.TOURNAMENT_MANAGE
                        }
                    )
                }

                Routes.TOURNAMENT_MANAGE -> {
                    TournamentManageScreen(
                        tournamentId = activeTournamentId,
                        viewModel = tournamentsViewModel,
                        onNavigateBack = { currentScreen = Routes.TOURNAMENTS },
                        onScoreFixtureClick = { fixtureId ->
                            // Use live demo or load fixture match
                            activeMatchId = "match_live_demo"
                            currentScreen = Routes.SCORER
                        }
                    )
                }

                Routes.MATCH_CREATE -> {
                    MatchCreateScreen(
                        onNavigateBack = { currentScreen = Routes.HOME },
                        onProceedToToss = { teamA, teamB, overs, format, venue ->
                            pendingTeamA = teamA
                            pendingTeamB = teamB
                            pendingOvers = overs
                            pendingFormat = format
                            pendingVenue = venue
                            currentScreen = Routes.TOSS
                        }
                    )
                }

                Routes.TOSS -> {
                    TossScreen(
                        teamAName = pendingTeamA,
                        teamBName = pendingTeamB,
                        overs = pendingOvers,
                        format = pendingFormat,
                        venue = pendingVenue,
                        viewModel = scoringViewModel,
                        onNavigateBack = { currentScreen = Routes.MATCH_CREATE },
                        onMatchStarted = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORER
                        }
                    )
                }

                Routes.SCORER -> {
                    val matchIdToLoad = activeMatchId ?: "match_live_demo"
                    ScorerScreen(
                        matchId = matchIdToLoad,
                        viewModel = scoringViewModel,
                        onNavigateBack = { currentScreen = Routes.HOME },
                        onViewScorecard = { id ->
                            activeMatchId = id
                            currentScreen = Routes.SCORECARD
                        }
                    )
                }

                Routes.SCORECARD -> {
                    val matchIdToLoad = activeMatchId ?: "match_live_demo"
                    ScorecardScreen(
                        matchId = matchIdToLoad,
                        viewModel = scoringViewModel,
                        onNavigateBack = { currentScreen = Routes.HOME }
                    )
                }

                Routes.TOURNAMENTS -> {
                    TournamentsScreen(
                        viewModel = tournamentsViewModel,
                        onSearchClick = { currentScreen = Routes.SEARCH },
                        onTournamentClick = { id ->
                            activeTournamentId = id
                            currentScreen = Routes.TOURNAMENT_MANAGE
                        }
                    )
                }

                Routes.TEAMS -> {
                    TeamsScreen(
                        viewModel = teamsViewModel,
                        onNavigateBack = { currentScreen = Routes.HOME },
                        showBack = true,
                        onSearchClick = { currentScreen = Routes.SEARCH }
                    )
                }

                Routes.PROFILE -> {
                    ProfileScreen(
                        authManager = authManager,
                        scorerAuthViewModel = scorerAuthViewModel,
                        onSearchClick = { currentScreen = Routes.SEARCH },
                        onScorerDashboardClick = { currentScreen = Routes.SCORER_DASHBOARD },
                        onPhoneAuthClick = { currentScreen = Routes.PHONE_AUTH }
                    )
                }

                Routes.SEARCH -> {
                    SearchScreen(
                        viewModel = homeViewModel,
                        onNavigateBack = { currentScreen = Routes.HOME },
                        onMatchClick = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORECARD
                        }
                    )
                }

                Routes.COMMUNITY -> {
                    CommunityScreen(
                        onNavigateBack = { currentScreen = Routes.HOME },
                        onSearchClick = { currentScreen = Routes.SEARCH }
                    )
                }

                else -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onCreateMatchClick = { currentScreen = Routes.MATCH_CREATE },
                        onMatchClick = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORECARD
                        },
                        onScoreMatchClick = { matchId ->
                            activeMatchId = matchId
                            currentScreen = Routes.SCORER
                        },
                        onTeamsClick = { currentScreen = Routes.TEAMS },
                        onTournamentsClick = { currentScreen = Routes.TOURNAMENTS },
                        onCommunityClick = { currentScreen = Routes.COMMUNITY },
                        onSearchClick = { currentScreen = Routes.SEARCH }
                    )
                }
            }
        }
    }
}
