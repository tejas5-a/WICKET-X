package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DeliveryAuditEntity
import com.example.data.local.DeliveryEntity
import com.example.data.local.MatchEntity
import com.example.data.repository.CricketRepository
import com.example.domain.engine.CricketScoringEngine
import com.example.domain.engine.DismissalType
import com.example.domain.engine.ExtraType
import com.example.domain.engine.MatchEngineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MatchScoringViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CricketRepository(application)

    private val _engineState = MutableStateFlow<MatchEngineState?>(null)
    val engineState: StateFlow<MatchEngineState?> = _engineState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _deliveryAudits = MutableStateFlow<List<DeliveryAuditEntity>>(emptyList())
    val deliveryAudits: StateFlow<List<DeliveryAuditEntity>> = _deliveryAudits.asStateFlow()

    private val _isAuthorizedScorer = MutableStateFlow(true)
    val isAuthorizedScorer: StateFlow<Boolean> = _isAuthorizedScorer.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    /**
     * Initializes a new match and saves initial state.
     */
    fun startNewMatch(
        teamAName: String,
        teamBName: String,
        totalOvers: Int,
        matchFormat: String,
        venue: String,
        tossWinner: String,
        tossDecision: String,
        team1Batters: List<String>,
        team2Bowlers: List<String>,
        tournamentId: String? = null,
        onMatchCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val matchId = "match_" + UUID.randomUUID().toString().take(8)
            val currentUserId = repository.getCurrentUserId()

            val initialEngineState = CricketScoringEngine.createInitialMatch(
                matchId = matchId,
                teamAName = teamAName,
                teamBName = teamBName,
                totalOvers = totalOvers,
                matchFormat = matchFormat,
                venue = venue,
                tossWinner = tossWinner,
                tossDecision = tossDecision,
                team1Batters = team1Batters,
                team2Bowlers = team2Bowlers
            )

            val matchEntity = MatchEntity(
                id = matchId,
                creatorId = currentUserId,
                scorerId = currentUserId,
                teamAName = teamAName,
                teamBName = teamBName,
                matchFormat = matchFormat,
                totalOvers = totalOvers,
                venue = venue,
                status = "live",
                tossWinner = tossWinner,
                tossDecision = tossDecision,
                currentInnings = 1,
                teamAScore = 0,
                teamAWickets = 0,
                teamAOvers = "0.0",
                tournamentId = tournamentId,
                resultSummary = "$tossWinner elected to $tossDecision first"
            )

            repository.saveMatch(matchEntity)
            _engineState.value = initialEngineState
            _isAuthorizedScorer.value = true
            _isLoading.value = false
            onMatchCreated(matchId)
        }
    }

    /**
     * Loads existing match state and replays ball deliveries to recover exact engine state.
     */
    fun loadMatch(matchId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val match = repository.getMatch(matchId)
            val currentUserId = repository.getCurrentUserId()
            _isAuthorizedScorer.value = repository.canUserScoreMatch(currentUserId, matchId)

            // Collect audits
            repository.observeAudits(matchId).collect {
                _deliveryAudits.value = it
            }

            if (match != null) {
                val deliveries = repository.getDeliveries(matchId)

                // Reconstruct match state
                var state = CricketScoringEngine.createInitialMatch(
                    matchId = match.id,
                    teamAName = match.teamAName,
                    teamBName = match.teamBName,
                    totalOvers = match.totalOvers,
                    matchFormat = match.matchFormat,
                    venue = match.venue,
                    tossWinner = if (match.tossWinner.isNotBlank()) match.tossWinner else match.teamAName,
                    tossDecision = if (match.tossDecision.isNotBlank()) match.tossDecision else "bat",
                    team1Batters = listOf("Batter 1", "Batter 2"),
                    team2Bowlers = listOf("Bowler 1")
                )

                // Replay innings 1 deliveries
                val inn1Deliveries = deliveries.filter { it.inningsNumber == 1 }
                for (d in inn1Deliveries) {
                    val event = com.example.domain.engine.DeliveryEvent(
                        id = d.id,
                        matchId = d.matchId,
                        inningsNumber = d.inningsNumber,
                        overNumber = d.overNumber,
                        ballInOver = d.ballInOver,
                        legalBallInOver = d.legalBallInOver,
                        strikerId = d.strikerName,
                        strikerName = d.strikerName,
                        nonStrikerId = d.nonStrikerName,
                        nonStrikerName = d.nonStrikerName,
                        bowlerId = d.bowlerName,
                        bowlerName = d.bowlerName,
                        runsBat = d.runsBat,
                        extraType = ExtraType.valueOf(d.extraType),
                        extraRuns = d.extraRuns,
                        isWicket = d.isWicket,
                        dismissalType = d.dismissalType?.let { DismissalType.valueOf(it) },
                        dismissedPlayerName = d.dismissedPlayerName,
                        fielderName = d.fielderName,
                        isLegal = d.isLegal,
                        commentary = d.commentary,
                        timestamp = d.timestamp
                    )
                    state = CricketScoringEngine.processDelivery(state, event, nextBatterName = d.dismissedPlayerName)
                }

                // If match is on innings 2, start 2nd innings and replay
                if (match.currentInnings == 2) {
                    state = CricketScoringEngine.startSecondInnings(
                        state = state,
                        team2Batters = listOf("Batter 1", "Batter 2"),
                        team1Bowlers = listOf("Bowler 1")
                    )
                    val inn2Deliveries = deliveries.filter { it.inningsNumber == 2 }
                    for (d in inn2Deliveries) {
                        val event = com.example.domain.engine.DeliveryEvent(
                            id = d.id,
                            matchId = d.matchId,
                            inningsNumber = d.inningsNumber,
                            overNumber = d.overNumber,
                            ballInOver = d.ballInOver,
                            legalBallInOver = d.legalBallInOver,
                            strikerId = d.strikerName,
                            strikerName = d.strikerName,
                            nonStrikerId = d.nonStrikerName,
                            nonStrikerName = d.nonStrikerName,
                            bowlerId = d.bowlerName,
                            bowlerName = d.bowlerName,
                            runsBat = d.runsBat,
                            extraType = ExtraType.valueOf(d.extraType),
                            extraRuns = d.extraRuns,
                            isWicket = d.isWicket,
                            dismissalType = d.dismissalType?.let { DismissalType.valueOf(it) },
                            dismissedPlayerName = d.dismissedPlayerName,
                            fielderName = d.fielderName,
                            isLegal = d.isLegal,
                            commentary = d.commentary,
                            timestamp = d.timestamp
                        )
                        state = CricketScoringEngine.processDelivery(state, event, nextBatterName = d.dismissedPlayerName)
                    }
                }

                _engineState.value = state
            }
            _isLoading.value = false
        }
    }

    /**
     * Scores a ball delivery with all options: runs (0, 1, 2, 3, 4, 5, 6), extras, wickets.
     * Enforces backend/repository permission check before writing.
     */
    fun scoreBall(
        runsBat: Int,
        extraType: ExtraType = ExtraType.NONE,
        extraRuns: Int = 0,
        isWicket: Boolean = false,
        dismissalType: DismissalType? = null,
        dismissedPlayer: String? = null,
        fielder: String? = null,
        nextBatter: String? = null,
        nextBowler: String? = null
    ) {
        val current = _engineState.value ?: return
        val currentInn = current.currentInnings
        if (currentInn.isInningsComplete || current.status == "completed") {
            _statusMessage.value = "Innings is completed!"
            return
        }

        val currentUserId = repository.getCurrentUserId()

        viewModelScope.launch {
            // Permission check: Viewers cannot score
            val isAuthorized = repository.canUserScoreMatch(currentUserId, current.matchId)
            if (!isAuthorized) {
                _statusMessage.value = "Unauthorized: Only certified scorers can record deliveries."
                return@launch
            }

            val deliveryId = "del_" + UUID.randomUUID().toString().take(10)
            val isLegal = extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL

            val ballNumber = currentInn.ballsInCurrentOver + 1
            val commentary = buildCommentary(runsBat, extraType, extraRuns, isWicket, dismissalType, currentInn.strikerName, currentInn.currentBowlerName)

            val deliveryEvent = com.example.domain.engine.DeliveryEvent(
                id = deliveryId,
                matchId = current.matchId,
                inningsNumber = current.currentInningsIndex,
                overNumber = currentInn.completedOvers,
                ballInOver = ballNumber,
                legalBallInOver = if (isLegal) ballNumber else 0,
                strikerId = currentInn.strikerName,
                strikerName = currentInn.strikerName,
                nonStrikerId = currentInn.nonStrikerName,
                nonStrikerName = currentInn.nonStrikerName,
                bowlerId = currentInn.currentBowlerName,
                bowlerName = currentInn.currentBowlerName,
                runsBat = runsBat,
                extraType = extraType,
                extraRuns = extraRuns,
                isWicket = isWicket,
                dismissalType = dismissalType,
                dismissedPlayerName = dismissedPlayer ?: if (isWicket) currentInn.strikerName else null,
                fielderName = fielder,
                isLegal = isLegal,
                commentary = commentary,
                timestamp = System.currentTimeMillis()
            )

            // Process through pure scoring engine
            val updatedState = CricketScoringEngine.processDelivery(
                state = current,
                delivery = deliveryEvent,
                nextBatterName = nextBatter,
                nextBowlerName = nextBowler
            )
            _engineState.value = updatedState

            val deliveryEntity = DeliveryEntity(
                id = deliveryId,
                matchId = current.matchId,
                inningsNumber = current.currentInningsIndex,
                overNumber = currentInn.completedOvers,
                ballInOver = ballNumber,
                legalBallInOver = if (isLegal) ballNumber else 0,
                strikerName = currentInn.strikerName,
                nonStrikerName = currentInn.nonStrikerName,
                bowlerName = currentInn.currentBowlerName,
                runsBat = runsBat,
                extraType = extraType.name,
                extraRuns = extraRuns,
                isWicket = isWicket,
                dismissalType = dismissalType?.name,
                dismissedPlayerName = dismissedPlayer ?: if (isWicket) currentInn.strikerName else null,
                fielderName = fielder,
                isLegal = isLegal,
                commentary = commentary,
                isSynced = false,
                timestamp = System.currentTimeMillis()
            )

            // Record delivery with audit trail
            repository.recordDelivery(deliveryEntity, scorerId = currentUserId, scorerName = "Official Scorer")

            // Update match entity
            val inn1 = updatedState.innings1
            val inn2 = updatedState.innings2
            val updatedMatchEntity = MatchEntity(
                id = updatedState.matchId,
                creatorId = currentUserId,
                scorerId = currentUserId,
                teamAName = updatedState.teamAName,
                teamBName = updatedState.teamBName,
                matchFormat = updatedState.matchFormat,
                totalOvers = updatedState.totalOvers,
                venue = updatedState.venue,
                status = updatedState.status,
                tossWinner = updatedState.tossWinner,
                tossDecision = updatedState.tossDecision,
                currentInnings = updatedState.currentInningsIndex,
                teamAScore = inn1.totalRuns,
                teamAWickets = inn1.wickets,
                teamAOvers = inn1.oversFormatted,
                teamBScore = inn2?.totalRuns ?: 0,
                teamBWickets = inn2?.wickets ?: 0,
                teamBOvers = inn2?.oversFormatted ?: "0.0",
                targetRuns = updatedState.target,
                resultSummary = updatedState.resultSummary
            )
            repository.saveMatch(updatedMatchEntity)
        }
    }

    /**
     * Undoes the last recorded ball delivery with audit trail.
     */
    fun undoLastDelivery() {
        val current = _engineState.value ?: return
        if (current.deliveryHistory.isEmpty()) {
            _statusMessage.value = "No deliveries to undo"
            return
        }

        val currentUserId = repository.getCurrentUserId()
        val matchId = current.matchId

        viewModelScope.launch {
            val isAuthorized = repository.canUserScoreMatch(currentUserId, matchId)
            if (!isAuthorized) {
                _statusMessage.value = "Unauthorized: Only certified scorers can undo deliveries."
                return@launch
            }

            repository.undoLastDelivery(matchId, scorerId = currentUserId, scorerName = "Official Scorer")
            loadMatch(matchId)
            _statusMessage.value = "Last ball delivery undone (Audited)"
        }
    }

    /**
     * Starts the second innings of the match.
     */
    fun startSecondInnings(
        team2Batters: List<String>,
        team1Bowlers: List<String>
    ) {
        val current = _engineState.value ?: return
        val updated = CricketScoringEngine.startSecondInnings(current, team2Batters, team1Bowlers)
        _engineState.value = updated

        viewModelScope.launch {
            val match = repository.getMatch(current.matchId)
            if (match != null) {
                val updatedMatch = match.copy(
                    currentInnings = 2,
                    targetRuns = updated.target,
                    resultSummary = updated.resultSummary
                )
                repository.saveMatch(updatedMatch)
            }
        }
    }

    fun switchStrike() {
        val current = _engineState.value ?: return
        val currentInn = current.currentInnings
        val temp = currentInn.strikerName
        val updatedInnings = currentInn.copy(
            strikerName = currentInn.nonStrikerName,
            nonStrikerName = temp
        )
        _engineState.value = if (current.currentInningsIndex == 1) {
            current.copy(innings1 = updatedInnings)
        } else {
            current.copy(innings2 = updatedInnings)
        }
    }

    fun changeBowler(newBowlerName: String) {
        val current = _engineState.value ?: return
        val currentInn = current.currentInnings
        var bowlers = currentInn.bowlers.toMutableMap()
        if (!bowlers.containsKey(newBowlerName)) {
            bowlers[newBowlerName] = com.example.domain.engine.BowlerScorecardItem(
                playerId = newBowlerName,
                playerName = newBowlerName
            )
        }
        val updatedInnings = currentInn.copy(
            currentBowlerName = newBowlerName,
            bowlers = bowlers
        )
        _engineState.value = if (current.currentInningsIndex == 1) {
            current.copy(innings1 = updatedInnings)
        } else {
            current.copy(innings2 = updatedInnings)
        }
    }

    private fun buildCommentary(
        runs: Int,
        extraType: ExtraType,
        extraRuns: Int,
        isWicket: Boolean,
        dismissal: DismissalType?,
        striker: String,
        bowler: String
    ): String {
        return when {
            isWicket -> "OUT! $striker is dismissed (${dismissal?.displayName ?: "Wicket"}) off $bowler's bowling!"
            runs == 6 -> "SIX! $striker launches it high into the stands!"
            runs == 5 -> "5 RUNS! Overthrow boundary conceded to $striker!"
            runs == 4 -> "FOUR! Beautiful boundary driven through the gap by $striker!"
            extraType == ExtraType.WIDE -> "Wide ball called by umpire. Extra run conceded."
            extraType == ExtraType.NO_BALL -> "NO BALL! Overstepping by $bowler! Free Hit coming up!"
            runs == 0 -> "Dot ball. Solid defensive stroke by $striker."
            else -> "$runs run(s) taken by $striker and rotated."
        }
    }
}
