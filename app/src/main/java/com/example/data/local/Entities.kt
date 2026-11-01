package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val creatorId: String,
    val teamAName: String,
    val teamBName: String,
    val matchFormat: String = "T20",
    val totalOvers: Int = 20,
    val venue: String = "Grassroots Cricket Ground",
    val status: String = "live", // "scheduled", "live", "completed", "tied"
    val tossWinner: String = "",
    val tossDecision: String = "bat",
    val currentInnings: Int = 1,
    val teamAScore: Int = 0,
    val teamAWickets: Int = 0,
    val teamAOvers: String = "0.0",
    val teamBScore: Int = 0,
    val teamBWickets: Int = 0,
    val teamBOvers: String = "0.0",
    val targetRuns: Int? = null,
    val resultSummary: String = "Match in progress",
    val scorerId: String = "",
    val scorerPin: String = "",
    val tournamentId: String? = null,
    val isPublished: Boolean = true,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "deliveries")
data class DeliveryEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val inningsNumber: Int,
    val overNumber: Int,
    val ballInOver: Int,
    val legalBallInOver: Int,
    val strikerName: String,
    val nonStrikerName: String,
    val bowlerName: String,
    val runsBat: Int,
    val extraType: String = "NONE",
    val extraRuns: Int = 0,
    val isWicket: Boolean = false,
    val dismissalType: String? = null,
    val dismissedPlayerName: String? = null,
    val fielderName: String? = null,
    val isLegal: Boolean = true,
    val commentary: String = "",
    val isSynced: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_audits")
data class DeliveryAuditEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val deliveryId: String,
    val action: String, // "RECORDED", "UNDONE", "CORRECTED"
    val scorerId: String,
    val scorerName: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val city: String = "",
    val captainName: String = "",
    val playerCount: Int = 11,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val teamName: String,
    val role: String = "All-Rounder", // Batsman, Bowler, All-Rounder, Wicketkeeper
    val battingStyle: String = "Right-hand bat",
    val bowlingStyle: String = "Right-arm medium",
    val matches: Int = 0,
    val runs: Int = 0,
    val wickets: Int = 0,
    val highestScore: Int = 0
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val organizerId: String = "",
    val name: String,
    val format: String = "T20",
    val location: String = "",
    val status: String = "active", // "upcoming", "active", "completed"
    val teamCount: Int = 4,
    val teamsList: String = "", // Comma-separated or serialized list of team names
    val fixturesJson: String = "[]",
    val pointsTableJson: String = "[]",
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String,
    val displayName: String = "",
    val clubName: String = "",
    val verificationStatus: String = "phone_unverified", // "phone_unverified", "phone_verified"
    val role: String = "viewer", // "viewer", "scorer_pending", "scorer", "admin"
    val scorerBadge: String = "Grassroots Viewer",
    val matchesScoredCount: Int = 0,
    val tournamentsManagedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
