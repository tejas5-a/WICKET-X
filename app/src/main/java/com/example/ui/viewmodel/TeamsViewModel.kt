package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PlayerEntity
import com.example.data.local.TeamEntity
import com.example.data.repository.CricketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TeamsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CricketRepository(application)

    val teams: StateFlow<List<TeamEntity>> = repository.observeTeams()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allPlayers: StateFlow<List<PlayerEntity>> = repository.observePlayers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _selectedTeam = MutableStateFlow<TeamEntity?>(null)
    val selectedTeam: StateFlow<TeamEntity?> = _selectedTeam.asStateFlow()

    fun selectTeam(team: TeamEntity) {
        _selectedTeam.value = team
    }

    fun createTeam(name: String, shortName: String, city: String, captainName: String) {
        viewModelScope.launch {
            val team = TeamEntity(
                id = "team_" + UUID.randomUUID().toString().take(8),
                name = name.trim(),
                shortName = shortName.trim().uppercase(),
                city = city.trim(),
                captainName = captainName.trim(),
                playerCount = 11,
                matchesPlayed = 0,
                matchesWon = 0
            )
            repository.saveTeam(team)
        }
    }
}
