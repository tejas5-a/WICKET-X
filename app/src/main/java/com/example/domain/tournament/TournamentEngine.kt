package com.example.domain.tournament

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.roundToInt

data class TournamentFixture(
    val id: String,
    val tournamentId: String,
    val roundNumber: Int,
    val roundName: String,
    val teamAName: String,
    val teamBName: String,
    val status: String = "scheduled", // scheduled, live, completed
    val winnerName: String? = null,
    val matchId: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("tournamentId", tournamentId)
        put("roundNumber", roundNumber)
        put("roundName", roundName)
        put("teamAName", teamAName)
        put("teamBName", teamBName)
        put("status", status)
        put("winnerName", winnerName ?: "")
        put("matchId", matchId ?: "")
    }

    companion object {
        fun fromJson(json: JSONObject): TournamentFixture = TournamentFixture(
            id = json.optString("id", UUID.randomUUID().toString()),
            tournamentId = json.optString("tournamentId", ""),
            roundNumber = json.optInt("roundNumber", 1),
            roundName = json.optString("roundName", "Round 1"),
            teamAName = json.optString("teamAName", ""),
            teamBName = json.optString("teamBName", ""),
            status = json.optString("status", "scheduled"),
            winnerName = json.optString("winnerName").takeIf { it.isNotBlank() },
            matchId = json.optString("matchId").takeIf { it.isNotBlank() }
        )
    }
}

data class TournamentPointsItem(
    val teamName: String,
    val played: Int = 0,
    val won: Int = 0,
    val lost: Int = 0,
    val tied: Int = 0,
    val points: Int = 0,
    val nrr: Double = 0.0,
    val runsScored: Int = 0,
    val oversFaced: Double = 0.0,
    val runsConceded: Int = 0,
    val oversBowled: Double = 0.0
) {
    val nrrFormatted: String
        get() = String.format("%+.3f", nrr)

    fun toJson(): JSONObject = JSONObject().apply {
        put("teamName", teamName)
        put("played", played)
        put("won", won)
        put("lost", lost)
        put("tied", tied)
        put("points", points)
        put("nrr", nrr)
        put("runsScored", runsScored)
        put("oversFaced", oversFaced)
        put("runsConceded", runsConceded)
        put("oversBowled", oversBowled)
    }

    companion object {
        fun fromJson(json: JSONObject): TournamentPointsItem = TournamentPointsItem(
            teamName = json.optString("teamName", ""),
            played = json.optInt("played", 0),
            won = json.optInt("won", 0),
            lost = json.optInt("lost", 0),
            tied = json.optInt("tied", 0),
            points = json.optInt("points", 0),
            nrr = json.optDouble("nrr", 0.0),
            runsScored = json.optInt("runsScored", 0),
            oversFaced = json.optDouble("oversFaced", 0.0),
            runsConceded = json.optInt("runsConceded", 0),
            oversBowled = json.optDouble("oversBowled", 0.0)
        )
    }
}

object TournamentEngine {

    /**
     * Generates standard Round Robin tournament fixtures for a list of participating teams.
     */
    fun generateRoundRobinFixtures(
        tournamentId: String,
        teams: List<String>
    ): List<TournamentFixture> {
        val cleanTeams = teams.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (cleanTeams.size < 2) return emptyList()

        val teamList = cleanTeams.toMutableList()
        // If odd number of teams, add dummy BYE
        val hasBye = teamList.size % 2 != 0
        if (hasBye) {
            teamList.add("BYE")
        }

        val totalTeams = teamList.size
        val rounds = totalTeams - 1
        val matchesPerRound = totalTeams / 2
        val fixtures = mutableListOf<TournamentFixture>()

        for (round in 0 until rounds) {
            val roundNumber = round + 1
            for (matchIdx in 0 until matchesPerRound) {
                val teamA = teamList[matchIdx]
                val teamB = teamList[totalTeams - 1 - matchIdx]

                if (teamA != "BYE" && teamB != "BYE") {
                    fixtures.add(
                        TournamentFixture(
                            id = "fix_${tournamentId}_${roundNumber}_${matchIdx + 1}",
                            tournamentId = tournamentId,
                            roundNumber = roundNumber,
                            roundName = "Round $roundNumber - Match ${matchIdx + 1}",
                            teamAName = teamA,
                            teamBName = teamB,
                            status = "scheduled"
                        )
                    )
                }
            }

            // Rotate teams (keep first team fixed, rotate rest)
            val last = teamList.removeAt(teamList.size - 1)
            teamList.add(1, last)
        }

        return fixtures
    }

    /**
     * Creates initialized points table for the list of teams.
     */
    fun createInitialPointsTable(teams: List<String>): List<TournamentPointsItem> {
        return teams.map { it.trim() }.filter { it.isNotBlank() }.distinct().map {
            TournamentPointsItem(teamName = it)
        }
    }

    /**
     * Serializes fixture list to JSON string for local Room and cloud persistence.
     */
    fun fixturesToJson(fixtures: List<TournamentFixture>): String {
        val arr = JSONArray()
        fixtures.forEach { arr.put(it.toJson()) }
        return arr.toString()
    }

    /**
     * Parses JSON string into a list of TournamentFixture.
     */
    fun parseFixturesJson(jsonStr: String): List<TournamentFixture> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<TournamentFixture>()
            for (i in 0 until arr.length()) {
                list.add(TournamentFixture.fromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Serializes points table list to JSON string.
     */
    fun pointsTableToJson(items: List<TournamentPointsItem>): String {
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        return arr.toString()
    }

    /**
     * Parses points table JSON string.
     */
    fun parsePointsTableJson(jsonStr: String): List<TournamentPointsItem> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<TournamentPointsItem>()
            for (i in 0 until arr.length()) {
                list.add(TournamentPointsItem.fromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Updates points table with a match result.
     */
    fun recordMatchResultInPointsTable(
        currentTable: List<TournamentPointsItem>,
        teamA: String,
        teamB: String,
        winner: String?, // null if tied / no result
        teamAScore: Int,
        teamAOvers: Double,
        teamBScore: Int,
        teamBOvers: Double
    ): List<TournamentPointsItem> {
        val map = currentTable.associateBy { it.teamName }.toMutableMap()

        val itemA = map[teamA] ?: TournamentPointsItem(teamName = teamA)
        val itemB = map[teamB] ?: TournamentPointsItem(teamName = teamB)

        val aWon = winner == teamA
        val bWon = winner == teamB
        val isTied = winner == null

        val newRunsScoredA = itemA.runsScored + teamAScore
        val newOversFacedA = itemA.oversFaced + teamAOvers
        val newRunsConcededA = itemA.runsConceded + teamBScore
        val newOversBowledA = itemA.oversBowled + teamBOvers

        val forRateA = if (newOversFacedA > 0) newRunsScoredA / newOversFacedA else 0.0
        val againstRateA = if (newOversBowledA > 0) newRunsConcededA / newOversBowledA else 0.0
        val nrrA = ((forRateA - againstRateA) * 1000.0).roundToInt() / 1000.0

        val newRunsScoredB = itemB.runsScored + teamBScore
        val newOversFacedB = itemB.oversFaced + teamBOvers
        val newRunsConcededB = itemB.runsConceded + teamAScore
        val newOversBowledB = itemB.oversBowled + teamAOvers

        val forRateB = if (newOversFacedB > 0) newRunsScoredB / newOversFacedB else 0.0
        val againstRateB = if (newOversBowledB > 0) newRunsConcededB / newOversBowledB else 0.0
        val nrrB = ((forRateB - againstRateB) * 1000.0).roundToInt() / 1000.0

        map[teamA] = itemA.copy(
            played = itemA.played + 1,
            won = itemA.won + if (aWon) 1 else 0,
            lost = itemA.lost + if (bWon) 1 else 0,
            tied = itemA.tied + if (isTied) 1 else 0,
            points = itemA.points + (if (aWon) 2 else if (isTied) 1 else 0),
            runsScored = newRunsScoredA,
            oversFaced = newOversFacedA,
            runsConceded = newRunsConcededA,
            oversBowled = newOversBowledA,
            nrr = nrrA
        )

        map[teamB] = itemB.copy(
            played = itemB.played + 1,
            won = itemB.won + if (bWon) 1 else 0,
            lost = itemB.lost + if (aWon) 1 else 0,
            tied = itemB.tied + if (isTied) 1 else 0,
            points = itemB.points + (if (bWon) 2 else if (isTied) 1 else 0),
            runsScored = newRunsScoredB,
            oversFaced = newOversFacedB,
            runsConceded = newRunsConcededB,
            oversBowled = newOversBowledB,
            nrr = nrrB
        )

        // Sort by Points DESC, then NRR DESC, then Won DESC
        return map.values.sortedWith(
            compareByDescending<TournamentPointsItem> { it.points }
                .thenByDescending { it.nrr }
                .thenByDescending { it.won }
        )
    }
}
