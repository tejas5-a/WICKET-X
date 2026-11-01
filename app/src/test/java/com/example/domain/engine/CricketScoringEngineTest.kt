package com.example.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CricketScoringEngineTest {

    private fun sampleMatch(): MatchEngineState {
        return CricketScoringEngine.createInitialMatch(
            matchId = "test_match_1",
            teamAName = "Mumbai Warriors",
            teamBName = "Delhi Challengers",
            totalOvers = 20,
            matchFormat = "T20",
            venue = "Wankhede Stadium",
            tossWinner = "Mumbai Warriors",
            tossDecision = "bat",
            team1Batters = listOf("Rohit", "Ishan", "Surya", "Hardik"),
            team2Bowlers = listOf("Bumrah", "Shami", "Siraj")
        )
    }

    @Test
    fun testInitialMatchSetup() {
        val match = sampleMatch()
        assertEquals("Mumbai Warriors", match.currentInnings.battingTeamName)
        assertEquals("Delhi Challengers", match.currentInnings.bowlingTeamName)
        assertEquals("Rohit", match.currentInnings.strikerName)
        assertEquals("Ishan", match.currentInnings.nonStrikerName)
        assertEquals("Bumrah", match.currentInnings.currentBowlerName)
        assertEquals(0, match.currentInnings.totalRuns)
        assertEquals(0, match.currentInnings.wickets)
        assertEquals(0, match.currentInnings.legalBalls)
        assertFalse(match.currentInnings.isInningsComplete)
    }

    @Test
    fun testSingleRunRotatesStrike() {
        val initial = sampleMatch()
        val delivery = DeliveryEvent(
            id = "d1",
            matchId = initial.matchId,
            inningsNumber = 1,
            overNumber = 0,
            ballInOver = 1,
            legalBallInOver = 1,
            strikerId = "Rohit",
            strikerName = "Rohit",
            nonStrikerId = "Ishan",
            nonStrikerName = "Ishan",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 1,
            isLegal = true
        )

        val updated = CricketScoringEngine.processDelivery(initial, delivery)
        assertEquals(1, updated.currentInnings.totalRuns)
        assertEquals(1, updated.currentInnings.legalBalls)
        // Strike rotated: Ishan is now striker, Rohit is non-striker
        assertEquals("Ishan", updated.currentInnings.strikerName)
        assertEquals("Rohit", updated.currentInnings.nonStrikerName)

        val rohitStats = updated.currentInnings.batters["Rohit"]
        assertNotNull(rohitStats)
        assertEquals(1, rohitStats?.runs)
        assertEquals(1, rohitStats?.ballsFaced)
    }

    @Test
    fun testBoundaryFourDoesNotRotateStrike() {
        val initial = sampleMatch()
        val delivery = DeliveryEvent(
            id = "d1",
            matchId = initial.matchId,
            inningsNumber = 1,
            overNumber = 0,
            ballInOver = 1,
            legalBallInOver = 1,
            strikerId = "Rohit",
            strikerName = "Rohit",
            nonStrikerId = "Ishan",
            nonStrikerName = "Ishan",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 4,
            isLegal = true
        )

        val updated = CricketScoringEngine.processDelivery(initial, delivery)
        assertEquals(4, updated.currentInnings.totalRuns)
        // Even runs: Rohit stays on strike
        assertEquals("Rohit", updated.currentInnings.strikerName)
        assertEquals("Ishan", updated.currentInnings.nonStrikerName)

        val rohit = updated.currentInnings.batters["Rohit"]
        assertEquals(4, rohit?.runs)
        assertEquals(1, rohit?.fours)
        assertEquals(0, rohit?.sixes)
        assertEquals(400.0, rohit?.strikeRate ?: 0.0, 0.01)
    }

    @Test
    fun testWideBallDoesNotIncrementLegalBalls() {
        val initial = sampleMatch()
        val wideDelivery = DeliveryEvent(
            id = "d_wide",
            matchId = initial.matchId,
            inningsNumber = 1,
            overNumber = 0,
            ballInOver = 1,
            legalBallInOver = 0,
            strikerId = "Rohit",
            strikerName = "Rohit",
            nonStrikerId = "Ishan",
            nonStrikerName = "Ishan",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 0,
            extraType = ExtraType.WIDE,
            extraRuns = 1,
            isLegal = false
        )

        val updated = CricketScoringEngine.processDelivery(initial, wideDelivery)
        assertEquals(1, updated.currentInnings.totalRuns)
        // Wide ball: legal balls remain 0
        assertEquals(0, updated.currentInnings.legalBalls)
        assertEquals(1, updated.currentInnings.extras.wides)
        assertEquals(1, updated.currentInnings.bowlers["Bumrah"]?.wides)
        assertEquals(1, updated.currentInnings.bowlers["Bumrah"]?.runsConceded)
        // Strike not rotated
        assertEquals("Rohit", updated.currentInnings.strikerName)
    }

    @Test
    fun testNoBallGivesFreeHit() {
        val initial = sampleMatch()
        val noBallDelivery = DeliveryEvent(
            id = "d_nb",
            matchId = initial.matchId,
            inningsNumber = 1,
            overNumber = 0,
            ballInOver = 1,
            legalBallInOver = 0,
            strikerId = "Rohit",
            strikerName = "Rohit",
            nonStrikerId = "Ishan",
            nonStrikerName = "Ishan",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 1, // ran 1 run off no-ball
            extraType = ExtraType.NO_BALL,
            extraRuns = 1,
            isLegal = false
        )

        val updated = CricketScoringEngine.processDelivery(initial, noBallDelivery)
        assertEquals(2, updated.currentInnings.totalRuns) // 1 bat run + 1 nb extra
        assertEquals(0, updated.currentInnings.legalBalls)
        assertTrue(updated.currentInnings.isFreeHit)
        // 1 run scored so strike rotated
        assertEquals("Ishan", updated.currentInnings.strikerName)
    }

    @Test
    fun testWicketIncrementsWicketsAndBringsNewBatter() {
        val initial = sampleMatch()
        val wicketDelivery = DeliveryEvent(
            id = "d_wkt",
            matchId = initial.matchId,
            inningsNumber = 1,
            overNumber = 0,
            ballInOver = 1,
            legalBallInOver = 1,
            strikerId = "Rohit",
            strikerName = "Rohit",
            nonStrikerId = "Ishan",
            nonStrikerName = "Ishan",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 0,
            isWicket = true,
            dismissalType = DismissalType.BOWLED,
            dismissedPlayerName = "Rohit",
            isLegal = true
        )

        val updated = CricketScoringEngine.processDelivery(
            state = initial,
            delivery = wicketDelivery,
            nextBatterName = "Surya"
        )
        assertEquals(1, updated.currentInnings.wickets)
        assertEquals("Surya", updated.currentInnings.strikerName)
        assertEquals("Ishan", updated.currentInnings.nonStrikerName)

        val rohit = updated.currentInnings.batters["Rohit"]
        assertTrue(rohit?.isOut == true)
        assertEquals("b Bumrah", rohit?.dismissalText)

        val bumrah = updated.currentInnings.bowlers["Bumrah"]
        assertEquals(1, bumrah?.wickets)
        assertEquals(1, updated.currentInnings.fallOfWickets.size)
    }

    @Test
    fun testOverCompletionRotatesStrike() {
        var state = sampleMatch()
        // Bowl 6 dot balls (0 runs)
        for (i in 1..6) {
            val d = DeliveryEvent(
                id = "d$i",
                matchId = state.matchId,
                inningsNumber = 1,
                overNumber = 0,
                ballInOver = i,
                legalBallInOver = i,
                strikerId = state.currentInnings.strikerName,
                strikerName = state.currentInnings.strikerName,
                nonStrikerId = state.currentInnings.nonStrikerName,
                nonStrikerName = state.currentInnings.nonStrikerName,
                bowlerId = state.currentInnings.currentBowlerName,
                bowlerName = state.currentInnings.currentBowlerName,
                runsBat = 0,
                isLegal = true
            )
            state = CricketScoringEngine.processDelivery(state, d, nextBowlerName = if (i == 6) "Shami" else null)
        }

        assertEquals(6, state.currentInnings.legalBalls)
        assertEquals(1, state.currentInnings.completedOvers)
        assertEquals("1.0", state.currentInnings.oversFormatted)
        // Over ended on dot ball: strike rotates to Ishan!
        assertEquals("Ishan", state.currentInnings.strikerName)
        assertEquals("Rohit", state.currentInnings.nonStrikerName)
        // New bowler for over 2
        assertEquals("Shami", state.currentInnings.currentBowlerName)
    }

    @Test
    fun testUndoLastDeliveryRestoresExactState() {
        val initial = sampleMatch()
        val d1 = DeliveryEvent(
            id = "d1",
            matchId = initial.matchId,
            inningsNumber = 1,
            overNumber = 0,
            ballInOver = 1,
            legalBallInOver = 1,
            strikerId = "Rohit",
            strikerName = "Rohit",
            nonStrikerId = "Ishan",
            nonStrikerName = "Ishan",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 6,
            isLegal = true
        )
        val d2 = DeliveryEvent(
            id = "d2",
            matchId = initial.matchId,
            inningsNumber = 1,
            overNumber = 0,
            ballInOver = 2,
            legalBallInOver = 2,
            strikerId = "Rohit",
            strikerName = "Rohit",
            nonStrikerId = "Ishan",
            nonStrikerName = "Ishan",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 1,
            isLegal = true
        )

        val stateAfter1 = CricketScoringEngine.processDelivery(initial, d1)
        val stateAfter2 = CricketScoringEngine.processDelivery(stateAfter1, d2)
        assertEquals(7, stateAfter2.currentInnings.totalRuns)

        // Undo delivery 2
        val undoneState = CricketScoringEngine.undoLastDelivery(
            initialState = initial,
            events = stateAfter2.deliveryHistory
        )

        assertEquals(6, undoneState.currentInnings.totalRuns)
        assertEquals(1, undoneState.currentInnings.legalBalls)
        assertEquals("Rohit", undoneState.currentInnings.strikerName)
        assertEquals(1, undoneState.deliveryHistory.size)
    }

    @Test
    fun testSecondInningsTargetChaseVictory() {
        val initial = sampleMatch()
        // 1st innings finishes with 10 runs in a super short match
        val inn1Finished = initial.currentInnings.copy(
            totalRuns = 10,
            wickets = 2,
            legalBalls = 12,
            maxOvers = 2,
            isInningsComplete = true
        )
        val stateWithInn1 = initial.copy(
            totalOvers = 2,
            innings1 = inn1Finished
        )

        // Start 2nd innings
        val stateInn2 = CricketScoringEngine.startSecondInnings(
            state = stateWithInn1,
            team2Batters = listOf("Warner", "Pant"),
            team1Bowlers = listOf("Bumrah")
        )

        assertEquals(2, stateInn2.currentInningsIndex)
        assertEquals(11, stateInn2.target)
        assertEquals(11, stateInn2.requiredRuns)

        // Pant hits a six and another six -> 12 runs, target reached!
        val d1 = DeliveryEvent(
            id = "d_inn2_1",
            matchId = stateInn2.matchId,
            inningsNumber = 2,
            overNumber = 0,
            ballInOver = 1,
            legalBallInOver = 1,
            strikerId = "Warner",
            strikerName = "Warner",
            nonStrikerId = "Pant",
            nonStrikerName = "Pant",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 6,
            isLegal = true
        )
        val d2 = DeliveryEvent(
            id = "d_inn2_2",
            matchId = stateInn2.matchId,
            inningsNumber = 2,
            overNumber = 0,
            ballInOver = 2,
            legalBallInOver = 2,
            strikerId = "Warner",
            strikerName = "Warner",
            nonStrikerId = "Pant",
            nonStrikerName = "Pant",
            bowlerId = "Bumrah",
            bowlerName = "Bumrah",
            runsBat = 6,
            isLegal = true
        )

        val s1 = CricketScoringEngine.processDelivery(stateInn2, d1)
        val s2 = CricketScoringEngine.processDelivery(s1, d2)

        assertEquals("completed", s2.status)
        assertTrue(s2.currentInnings.isInningsComplete)
        assertTrue(s2.resultSummary.contains("Delhi Challengers won by 10 wickets"))
    }
}
