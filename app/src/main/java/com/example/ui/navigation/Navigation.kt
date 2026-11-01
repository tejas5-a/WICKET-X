package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SportsCricket
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Live : Screen(
        route = "live",
        title = "Live",
        selectedIcon = Icons.Filled.Sensors,
        unselectedIcon = Icons.Outlined.Sensors
    )

    object Score : Screen(
        route = "score",
        title = "Score",
        selectedIcon = Icons.Filled.SportsCricket,
        unselectedIcon = Icons.Outlined.SportsCricket
    )

    object Tournaments : Screen(
        route = "tournaments",
        title = "Leagues",
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents
    )

    object Profile : Screen(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val bottomNavItems = listOf(Home, Live, Score, Tournaments, Profile)
    }
}

object Routes {
    const val HOME = "home"
    const val LIVE = "live"
    const val SCORE = "score"
    const val TOURNAMENTS = "tournaments"
    const val PROFILE = "profile"

    const val MATCH_CREATE = "match_create"
    const val TOSS = "toss/{matchId}"
    const val SCORER = "scorer/{matchId}"
    const val SCORECARD = "scorecard/{matchId}"
    const val TEAMS = "teams"
    const val CREATE_TEAM = "create_team"
    const val CREATE_TOURNAMENT = "create_tournament"
    const val TOURNAMENT_DETAIL = "tournament_detail/{tournamentId}"
    const val TOURNAMENT_MANAGE = "tournament_manage"
    const val PHONE_AUTH = "phone_auth"
    const val SCORER_DASHBOARD = "scorer_dashboard"
    const val SEARCH = "search"
    const val COMMUNITY = "community"

    fun tossRoute(matchId: String) = "toss/$matchId"
    fun scorerRoute(matchId: String) = "scorer/$matchId"
    fun scorecardRoute(matchId: String) = "scorecard/$matchId"
    fun tournamentDetailRoute(tournamentId: String) = "tournament_detail/$tournamentId"
    fun tournamentManageRoute(tournamentId: String) = "tournament_manage"
}
