package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TournamentEntity
import com.example.data.repository.CricketRepository
import com.example.domain.tournament.TournamentEngine
import com.example.domain.tournament.TournamentFixture
import com.example.domain.tournament.TournamentPointsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TeamStanding(
    val teamName: String,
    val played: Int,
    val won: Int,
    val lost: Int,
    val tied: Int,
    val points: Int,
    val netRunRate: Double
)

class TournamentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CricketRepository(application)

    val tournaments: StateFlow<List<TournamentEntity>> = repository.observeTournaments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _selectedTournament = MutableStateFlow<TournamentEntity?>(null)
    val selectedTournament: StateFlow<TournamentEntity?> = _selectedTournament.asStateFlow()

    private val _fixtures = MutableStateFlow<List<TournamentFixture>>(emptyList())
    val fixtures: StateFlow<List<TournamentFixture>> = _fixtures.asStateFlow()

    private val _pointsTable = MutableStateFlow<List<TournamentPointsItem>>(emptyList())
    val pointsTable: StateFlow<List<TournamentPointsItem>> = _pointsTable.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun loadTournament(tournamentId: String) {
        viewModelScope.launch {
            val t = repository.getTournament(tournamentId)
            _selectedTournament.value = t
            if (t != null) {
                _fixtures.value = TournamentEngine.parseFixturesJson(t.fixturesJson)
                _pointsTable.value = TournamentEngine.parsePointsTableJson(t.pointsTableJson)
            }
        }
    }

    fun createTournament(
        name: String,
        format: String,
        location: String,
        teams: List<String>,
        onCreated: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createTournamentWithFixtures(
                name = name.trim(),
                format = format,
                location = location.trim(),
                teams = teams
            )
            if (result.isSuccess) {
                val created = result.getOrThrow()
                _selectedTournament.value = created
                _fixtures.value = TournamentEngine.parseFixturesJson(created.fixturesJson)
                _pointsTable.value = TournamentEngine.parsePointsTableJson(created.pointsTableJson)
                _statusMessage.value = "Tournament created with Round Robin fixtures!"
                onCreated(created.id)
            } else {
                _statusMessage.value = result.exceptionOrNull()?.message ?: "Failed to create tournament"
            }
        }
    }

    fun updateTournamentStatus(tournamentId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateTournamentStatus(tournamentId, newStatus)
            loadTournament(tournamentId)
            _statusMessage.value = "Tournament status updated to $newStatus"
        }
    }

    fun recordMatchResult(
        tournamentId: String,
        teamA: String,
        teamB: String,
        winner: String?,
        teamAScore: Int,
        teamAOvers: Double,
        teamBScore: Int,
        teamBOvers: Double
    ) {
        viewModelScope.launch {
            repository.updateTournamentPoints(
                tournamentId = tournamentId,
                teamA = teamA,
                teamB = teamB,
                winner = winner,
                teamAScore = teamAScore,
                teamAOvers = teamAOvers,
                teamBScore = teamBScore,
                teamBOvers = teamBOvers
            )
            loadTournament(tournamentId)
            _statusMessage.value = "Points table updated with match result!"
        }
    }

    fun getStandingsForTournament(tournamentId: String): List<TeamStanding> {
        val t = tournaments.value.find { it.id == tournamentId }
        if (t != null && t.pointsTableJson.isNotBlank()) {
            val table = TournamentEngine.parsePointsTableJson(t.pointsTableJson)
            if (table.isNotEmpty()) {
                return table.map {
                    TeamStanding(
                        teamName = it.teamName,
                        played = it.played,
                        won = it.won,
                        lost = it.lost,
                        tied = it.tied,
                        points = it.points,
                        netRunRate = it.nrr
                    )
                }
            }
        }
        return listOf(
            TeamStanding("Mumbai Warriors CC", played = 4, won = 3, lost = 1, tied = 0, points = 6, netRunRate = +1.240),
            TeamStanding("Gujarat Super Titans", played = 4, won = 3, lost = 1, tied = 0, points = 6, netRunRate = +0.825),
            TeamStanding("Delhi Challengers", played = 4, won = 2, lost = 2, tied = 0, points = 4, netRunRate = +0.120),
            TeamStanding("Rajasthan Strikers", played = 4, won = 0, lost = 4, tied = 0, points = 0, netRunRate = -2.185)
        )
    }
}
