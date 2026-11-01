package com.example.domain.engine

/**
 * Cricket scoring domain models and pure computation engine.
 * Calculates all cricket rules independently of UI.
 */

enum class ExtraType {
    NONE, WIDE, NO_BALL, BYE, LEG_BYE, PENALTY
}

enum class DismissalType(val displayName: String) {
    BOWLED("Bowled"),
    CAUGHT("Caught"),
    CAUGHT_AND_BOWLED("Caught & Bowled"),
    LBW("LBW"),
    RUN_OUT("Run Out"),
    STUMPED("Stumped"),
    HIT_WICKET("Hit Wicket"),
    RETIRED_HURT("Retired Hurt"),
    RETIRED_OUT("Retired Out"),
    HANDLED_BALL("Handled Ball"),
    OBSTRUCTING_FIELD("Obstructing Field"),
    TIMED_OUT("Timed Out")
}

data class BatterScorecardItem(
    val playerId: String,
    val playerName: String,
    val runs: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false,
    val dismissalText: String = "not out",
    val dismissedByBowler: String? = null,
    val fielderName: String? = null
) {
    val strikeRate: Double
        get() = if (ballsFaced > 0) (runs.toDouble() * 100.0) / ballsFaced else 0.0
}

data class BowlerScorecardItem(
    val playerId: String,
    val playerName: String,
    val legalBallsBowled: Int = 0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0
) {
    val oversString: String
        get() {
            val overs = legalBallsBowled / 6
            val balls = legalBallsBowled % 6
            return "$overs.$balls"
        }

    val oversDecimal: Double
        get() = legalBallsBowled / 6.0

    val economy: Double
        get() = if (legalBallsBowled > 0) runsConceded / oversDecimal else 0.0
}

data class FallOfWicket(
    val wicketNumber: Int,
    val teamScore: Int,
    val overs: String,
    val batterName: String
)

data class ExtrasBreakdown(
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0,
    val penalty: Int = 0
) {
    val total: Int
        get() = wides + noBalls + byes + legByes + penalty
}

data class DeliveryEvent(
    val id: String,
    val matchId: String,
    val inningsNumber: Int,
    val overNumber: Int, // 0-indexed over count
    val ballInOver: Int, // ball count inside the over
    val legalBallInOver: Int, // 1 to 6
    val strikerId: String,
    val strikerName: String,
    val nonStrikerId: String,
    val nonStrikerName: String,
    val bowlerId: String,
    val bowlerName: String,
    val runsBat: Int,
    val extraType: ExtraType = ExtraType.NONE,
    val extraRuns: Int = 0,
    val isWicket: Boolean = false,
    val dismissalType: DismissalType? = null,
    val dismissedPlayerName: String? = null,
    val fielderName: String? = null,
    val isLegal: Boolean = true,
    val commentary: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalRunsFromDelivery: Int
        get() = runsBat + extraRuns
}

data class InningsState(
    val inningsNumber: Int,
    val battingTeamName: String,
    val bowlingTeamName: String,
    val totalRuns: Int = 0,
    val wickets: Int = 0,
    val legalBalls: Int = 0,
    val maxOvers: Int = 20,
    val strikerId: String = "",
    val strikerName: String = "",
    val nonStrikerId: String = "",
    val nonStrikerName: String = "",
    val currentBowlerId: String = "",
    val currentBowlerName: String = "",
    val isFreeHit: Boolean = false,
    val isInningsComplete: Boolean = false,
    val batters: Map<String, BatterScorecardItem> = emptyMap(),
    val bowlers: Map<String, BowlerScorecardItem> = emptyMap(),
    val extras: ExtrasBreakdown = ExtrasBreakdown(),
    val fallOfWickets: List<FallOfWicket> = emptyList(),
    val recentDeliveries: List<DeliveryEvent> = emptyList() // last few balls
) {
    val completedOvers: Int
        get() = legalBalls / 6

    val ballsInCurrentOver: Int
        get() = legalBalls % 6

    val oversFormatted: String
        get() = "$completedOvers.$ballsInCurrentOver"

    val currentRunRate: Double
        get() = if (legalBalls > 0) (totalRuns.toDouble() * 6.0) / legalBalls else 0.0
}

data class MatchEngineState(
    val matchId: String,
    val teamAName: String,
    val teamBName: String,
    val matchFormat: String = "T20",
    val totalOvers: Int = 20,
    val venue: String = "Grassroots Cricket Ground",
    val tossWinner: String = "",
    val tossDecision: String = "bat",
    val status: String = "live", // "scheduled", "live", "completed", "tied"
    val currentInningsIndex: Int = 1,
    val innings1: InningsState,
    val innings2: InningsState? = null,
    val deliveryHistory: List<DeliveryEvent> = emptyList(),
    val resultSummary: String = "Match in progress"
) {
    val currentInnings: InningsState
        get() = if (currentInningsIndex == 1 || innings2 == null) innings1 else innings2

    val target: Int?
        get() = if (currentInningsIndex == 2) innings1.totalRuns + 1 else null

    val requiredRuns: Int?
        get() = target?.let { maxOf(0, it - (innings2?.totalRuns ?: 0)) }

    val remainingBalls: Int?
        get() = innings2?.let { maxOf(0, (totalOvers * 6) - it.legalBalls) }

    val requiredRunRate: Double?
        get() {
            val needed = requiredRuns ?: return null
            val balls = remainingBalls ?: return null
            return if (balls > 0) (needed.toDouble() * 6.0) / balls else if (needed == 0) 0.0 else 99.99
        }
}

/**
 * Pure cricket calculations.
 */
object CricketScoringEngine {

    /**
     * Creates an initial match state from teams, overs, and toss outcome.
     */
    fun createInitialMatch(
        matchId: String,
        teamAName: String,
        teamBName: String,
        totalOvers: Int,
        matchFormat: String,
        venue: String,
        tossWinner: String,
        tossDecision: String,
        team1Batters: List<String>,
        team2Bowlers: List<String>
    ): MatchEngineState {
        val battingFirstTeam = if (tossWinner == teamAName) {
            if (tossDecision == "bat") teamAName else teamBName
        } else {
            if (tossDecision == "bat") teamBName else teamAName
        }
        val bowlingFirstTeam = if (battingFirstTeam == teamAName) teamBName else teamAName

        val striker = team1Batters.getOrNull(0) ?: "Batter 1"
        val nonStriker = team1Batters.getOrNull(1) ?: "Batter 2"
        val bowler = team2Bowlers.getOrNull(0) ?: "Bowler 1"

        val initialBatters = mapOf(
            striker to BatterScorecardItem(playerId = striker, playerName = striker),
            nonStriker to BatterScorecardItem(playerId = nonStriker, playerName = nonStriker)
        )
        val initialBowlers = mapOf(
            bowler to BowlerScorecardItem(playerId = bowler, playerName = bowler)
        )

        val innings1 = InningsState(
            inningsNumber = 1,
            battingTeamName = battingFirstTeam,
            bowlingTeamName = bowlingFirstTeam,
            maxOvers = totalOvers,
            strikerId = striker,
            strikerName = striker,
            nonStrikerId = nonStriker,
            nonStrikerName = nonStriker,
            currentBowlerId = bowler,
            currentBowlerName = bowler,
            batters = initialBatters,
            bowlers = initialBowlers
        )

        return MatchEngineState(
            matchId = matchId,
            teamAName = teamAName,
            teamBName = teamBName,
            matchFormat = matchFormat,
            totalOvers = totalOvers,
            venue = venue,
            tossWinner = tossWinner,
            tossDecision = tossDecision,
            status = "live",
            currentInningsIndex = 1,
            innings1 = innings1,
            resultSummary = "$battingFirstTeam elected to $tossDecision first"
        )
    }

    /**
     * Applies a ball delivery to the current match state, returning an updated state.
     */
    fun processDelivery(
        state: MatchEngineState,
        delivery: DeliveryEvent,
        nextBatterName: String? = null,
        nextBowlerName: String? = null
    ): MatchEngineState {
        val currentInn = state.currentInnings
        if (currentInn.isInningsComplete || state.status == "completed") {
            return state
        }

        val isLegal = delivery.extraType != ExtraType.WIDE && delivery.extraType != ExtraType.NO_BALL
        val legalBallsAfter = if (isLegal) currentInn.legalBalls + 1 else currentInn.legalBalls

        // Batter update
        var updatedBatters = currentInn.batters.toMutableMap()
        val strikerItem = updatedBatters[currentInn.strikerName] ?: BatterScorecardItem(
            playerId = currentInn.strikerName,
            playerName = currentInn.strikerName
        )

        val runsFromBat = delivery.runsBat
        val newBallsFaced = if (delivery.extraType != ExtraType.WIDE) strikerItem.ballsFaced + 1 else strikerItem.ballsFaced
        val newFours = if (runsFromBat == 4) strikerItem.fours + 1 else strikerItem.fours
        val newSixes = if (runsFromBat == 6) strikerItem.sixes + 1 else strikerItem.sixes

        val updatedStriker = if (delivery.isWicket && (delivery.dismissedPlayerName == null || delivery.dismissedPlayerName == currentInn.strikerName)) {
            strikerItem.copy(
                runs = strikerItem.runs + runsFromBat,
                ballsFaced = newBallsFaced,
                fours = newFours,
                sixes = newSixes,
                isOut = true,
                dismissalText = formatDismissalText(delivery),
                dismissedByBowler = currentInn.currentBowlerName,
                fielderName = delivery.fielderName
            )
        } else {
            strikerItem.copy(
                runs = strikerItem.runs + runsFromBat,
                ballsFaced = newBallsFaced,
                fours = newFours,
                sixes = newSixes
            )
        }
        updatedBatters[currentInn.strikerName] = updatedStriker

        // Bowler update
        var updatedBowlers = currentInn.bowlers.toMutableMap()
        val bowlerItem = updatedBowlers[currentInn.currentBowlerName] ?: BowlerScorecardItem(
            playerId = currentInn.currentBowlerName,
            playerName = currentInn.currentBowlerName
        )

        // Bowler runs conceded: bat runs + wide runs + no ball runs (byes/leg-byes not credited against bowler)
        val bowlerConcededRuns = when (delivery.extraType) {
            ExtraType.WIDE -> delivery.extraRuns + runsFromBat
            ExtraType.NO_BALL -> delivery.extraRuns + runsFromBat
            ExtraType.NONE -> runsFromBat
            ExtraType.BYE, ExtraType.LEG_BYE, ExtraType.PENALTY -> 0
        }

        // Wickets credited to bowler: bowled, caught, lbw, stumped, hit wicket
        val bowlerWicket = delivery.isWicket && delivery.dismissalType in listOf(
            DismissalType.BOWLED,
            DismissalType.CAUGHT,
            DismissalType.CAUGHT_AND_BOWLED,
            DismissalType.LBW,
            DismissalType.STUMPED,
            DismissalType.HIT_WICKET
        )

        val updatedBowler = bowlerItem.copy(
            legalBallsBowled = if (isLegal) bowlerItem.legalBallsBowled + 1 else bowlerItem.legalBallsBowled,
            runsConceded = bowlerItem.runsConceded + bowlerConcededRuns,
            wickets = if (bowlerWicket) bowlerItem.wickets + 1 else bowlerItem.wickets,
            wides = if (delivery.extraType == ExtraType.WIDE) bowlerItem.wides + 1 else bowlerItem.wides,
            noBalls = if (delivery.extraType == ExtraType.NO_BALL) bowlerItem.noBalls + 1 else bowlerItem.noBalls
        )
        updatedBowlers[currentInn.currentBowlerName] = updatedBowler

        // Extras breakdown
        val currentExtras = currentInn.extras
        val updatedExtras = when (delivery.extraType) {
            ExtraType.WIDE -> currentExtras.copy(wides = currentExtras.wides + delivery.extraRuns)
            ExtraType.NO_BALL -> currentExtras.copy(noBalls = currentExtras.noBalls + delivery.extraRuns)
            ExtraType.BYE -> currentExtras.copy(byes = currentExtras.byes + delivery.extraRuns)
            ExtraType.LEG_BYE -> currentExtras.copy(legByes = currentExtras.legByes + delivery.extraRuns)
            ExtraType.PENALTY -> currentExtras.copy(penalty = currentExtras.penalty + delivery.extraRuns)
            ExtraType.NONE -> currentExtras
        }

        val totalDeliveryRuns = delivery.totalRunsFromDelivery
        val newTotalRuns = currentInn.totalRuns + totalDeliveryRuns
        val newWickets = if (delivery.isWicket) currentInn.wickets + 1 else currentInn.wickets

        // Fall of wickets
        val fallOfWickets = currentInn.fallOfWickets.toMutableList()
        if (delivery.isWicket) {
            val dismissed = delivery.dismissedPlayerName ?: currentInn.strikerName
            val oversString = "${legalBallsAfter / 6}.${legalBallsAfter % 6}"
            fallOfWickets.add(
                FallOfWicket(
                    wicketNumber = newWickets,
                    teamScore = newTotalRuns,
                    overs = oversString,
                    batterName = dismissed
                )
            )
        }

        // Strike rotation logic
        var newStriker = currentInn.strikerName
        var newNonStriker = currentInn.nonStrikerName

        if (delivery.isWicket) {
            val incomingBatter = nextBatterName ?: "Batter ${newWickets + 2}"
            if (delivery.dismissedPlayerName == newNonStriker) {
                newNonStriker = incomingBatter
            } else {
                newStriker = incomingBatter
            }
            if (!updatedBatters.containsKey(incomingBatter)) {
                updatedBatters[incomingBatter] = BatterScorecardItem(
                    playerId = incomingBatter,
                    playerName = incomingBatter
                )
            }
        }

        // Odd runs rotate strike (1, 3, 5 runs scored off bat or byes/legbyes/running on extras)
        val shouldRotateForRuns = (runsFromBat + (if (delivery.extraType in listOf(ExtraType.BYE, ExtraType.LEG_BYE)) delivery.extraRuns else 0)) % 2 != 0
        if (shouldRotateForRuns) {
            val temp = newStriker
            newStriker = newNonStriker
            newNonStriker = temp
        }

        // Over completion check
        val isOverComplete = isLegal && (legalBallsAfter % 6 == 0)
        var newBowler = currentInn.currentBowlerName

        if (isOverComplete) {
            // End of over: Rotate strike between batters
            val temp = newStriker
            newStriker = newNonStriker
            newNonStriker = temp

            if (nextBowlerName != null && nextBowlerName.isNotBlank() && nextBowlerName != currentInn.currentBowlerName) {
                newBowler = nextBowlerName
                if (!updatedBowlers.containsKey(newBowler)) {
                    updatedBowlers[newBowler] = BowlerScorecardItem(
                        playerId = newBowler,
                        playerName = newBowler
                    )
                }
            }
        }

        // Free hit rule: If No-Ball, next delivery is Free Hit
        val nextFreeHit = delivery.extraType == ExtraType.NO_BALL

        // Innings completion conditions
        val maxBalls = currentInn.maxOvers * 6
        val allOut = newWickets >= 10
        val oversDone = legalBallsAfter >= maxBalls

        // 2nd innings target reached?
        val targetReached = state.currentInningsIndex == 2 && state.target != null && newTotalRuns >= state.target!!

        val inningsFinished = allOut || oversDone || targetReached

        val updatedInnings = currentInn.copy(
            totalRuns = newTotalRuns,
            wickets = newWickets,
            legalBalls = legalBallsAfter,
            strikerName = newStriker,
            nonStrikerName = newNonStriker,
            currentBowlerName = newBowler,
            isFreeHit = nextFreeHit,
            isInningsComplete = inningsFinished,
            batters = updatedBatters,
            bowlers = updatedBowlers,
            extras = updatedExtras,
            fallOfWickets = fallOfWickets,
            recentDeliveries = (listOf(delivery) + currentInn.recentDeliveries).take(12)
        )

        val updatedHistory = state.deliveryHistory + delivery

        // Handle match state transitions
        if (state.currentInningsIndex == 1) {
            if (inningsFinished) {
                val target = newTotalRuns + 1
                return state.copy(
                    innings1 = updatedInnings,
                    deliveryHistory = updatedHistory,
                    resultSummary = "1st Innings: ${updatedInnings.battingTeamName} scored $newTotalRuns/$newWickets. Target: $target"
                )
            } else {
                return state.copy(
                    innings1 = updatedInnings,
                    deliveryHistory = updatedHistory
                )
            }
        } else {
            // 2nd Innings
            val inn1 = state.innings1
            var matchStatus = state.status
            var summary = state.resultSummary

            if (inningsFinished) {
                matchStatus = "completed"
                val target = inn1.totalRuns + 1
                if (newTotalRuns >= target) {
                    val wicketsLeft = 10 - newWickets
                    val ballsLeft = maxBalls - legalBallsAfter
                    summary = "${updatedInnings.battingTeamName} won by $wicketsLeft wickets ($ballsLeft balls left)"
                } else if (newTotalRuns == inn1.totalRuns && (allOut || oversDone)) {
                    matchStatus = "tied"
                    summary = "Match Tied! Both teams scored $newTotalRuns runs"
                } else {
                    val runsDiff = inn1.totalRuns - newTotalRuns
                    summary = "${inn1.battingTeamName} won by $runsDiff runs"
                }
            }

            return state.copy(
                innings2 = updatedInnings,
                deliveryHistory = updatedHistory,
                status = matchStatus,
                resultSummary = summary
            )
        }
    }

    /**
     * Starts the 2nd innings of the match.
     */
    fun startSecondInnings(
        state: MatchEngineState,
        team2Batters: List<String>,
        team1Bowlers: List<String>
    ): MatchEngineState {
        val inn1 = state.innings1
        val battingTeam = inn1.bowlingTeamName
        val bowlingTeam = inn1.battingTeamName

        val striker = team2Batters.getOrNull(0) ?: "Batter 1"
        val nonStriker = team2Batters.getOrNull(1) ?: "Batter 2"
        val bowler = team1Bowlers.getOrNull(0) ?: "Bowler 1"

        val initialBatters = mapOf(
            striker to BatterScorecardItem(playerId = striker, playerName = striker),
            nonStriker to BatterScorecardItem(playerId = nonStriker, playerName = nonStriker)
        )
        val initialBowlers = mapOf(
            bowler to BowlerScorecardItem(playerId = bowler, playerName = bowler)
        )

        val target = inn1.totalRuns + 1
        val innings2 = InningsState(
            inningsNumber = 2,
            battingTeamName = battingTeam,
            bowlingTeamName = bowlingTeam,
            maxOvers = state.totalOvers,
            strikerId = striker,
            strikerName = striker,
            nonStrikerId = nonStriker,
            nonStrikerName = nonStriker,
            currentBowlerId = bowler,
            currentBowlerName = bowler,
            batters = initialBatters,
            bowlers = initialBowlers
        )

        return state.copy(
            currentInningsIndex = 2,
            innings2 = innings2,
            resultSummary = "$battingTeam need $target runs to win in ${state.totalOvers} overs"
        )
    }

    /**
     * Undoes the last delivery by replaying all previous deliveries.
     * Guaranteed to restore exact state without discrepancies.
     */
    fun undoLastDelivery(
        initialState: MatchEngineState,
        events: List<DeliveryEvent>
    ): MatchEngineState {
        if (events.isEmpty()) return initialState

        val remainingEvents = events.dropLast(1)
        var replayState = initialState
        for (event in remainingEvents) {
            replayState = processDelivery(
                state = replayState,
                delivery = event,
                nextBatterName = event.dismissedPlayerName,
                nextBowlerName = null
            )
        }
        return replayState
    }

    private fun formatDismissalText(delivery: DeliveryEvent): String {
        return when (delivery.dismissalType) {
            DismissalType.BOWLED -> "b ${delivery.bowlerName}"
            DismissalType.CAUGHT -> "c ${delivery.fielderName ?: "Fielder"} b ${delivery.bowlerName}"
            DismissalType.CAUGHT_AND_BOWLED -> "c & b ${delivery.bowlerName}"
            DismissalType.LBW -> "lbw b ${delivery.bowlerName}"
            DismissalType.RUN_OUT -> "run out (${delivery.fielderName ?: "Fielder"})"
            DismissalType.STUMPED -> "st ${delivery.fielderName ?: "Keeper"} b ${delivery.bowlerName}"
            DismissalType.HIT_WICKET -> "hit wicket b ${delivery.bowlerName}"
            DismissalType.RETIRED_HURT -> "retired hurt"
            DismissalType.RETIRED_OUT -> "retired out"
            DismissalType.HANDLED_BALL -> "handled the ball"
            DismissalType.OBSTRUCTING_FIELD -> "obstructing the field"
            DismissalType.TIMED_OUT -> "timed out"
            null -> "out"
        }
    }
}
