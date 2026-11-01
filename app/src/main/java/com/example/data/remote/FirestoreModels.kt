package com.example.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val playingRole: String = "All-Rounder",
    val battingStyle: String = "Right-hand bat",
    val bowlingStyle: String = "Right-arm medium",
    val matchesPlayed: Long = 0L,
    val totalRuns: Long = 0L,
    val totalWickets: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreMatch(
    @DocumentId val id: String = "",
    val creatorId: String = "",
    val teamAName: String = "",
    val teamBName: String = "",
    val matchFormat: String = "T20",
    val totalOvers: Long = 20L,
    val venue: String = "",
    val matchDate: String = "",
    val status: String = "live",
    val tossWinner: String = "",
    val tossDecision: String = "bat",
    val currentInnings: Long = 1L,
    val teamAScore: Long = 0L,
    val teamAWickets: Long = 0L,
    val teamABalls: Long = 0L,
    val teamBScore: Long = 0L,
    val teamBWickets: Long = 0L,
    val teamBBalls: Long = 0L,
    val targetRuns: Long = 0L,
    val resultSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreDelivery(
    @DocumentId val id: String = "",
    val matchId: String = "",
    val creatorId: String = "",
    val innings: Long = 1L,
    val overNumber: Long = 0L,
    val ballInOver: Long = 1L,
    val strikerName: String = "",
    val nonStrikerName: String = "",
    val bowlerName: String = "",
    val runsBat: Long = 0L,
    val extrasType: String = "none",
    val extrasRuns: Long = 0L,
    val isWicket: Boolean = false,
    val dismissalType: String = "",
    val dismissedPlayer: String = "",
    val isLegal: Boolean = true,
    val commentary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreTeam(
    @DocumentId val id: String = "",
    val name: String = "",
    val shortName: String = "",
    val city: String = "",
    val createdBy: String = "",
    val captainName: String = "",
    val playerCount: Long = 11L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreTournament(
    @DocumentId val id: String = "",
    val organizerId: String = "",
    val name: String = "",
    val format: String = "T20",
    val location: String = "",
    val status: String = "active",
    val teamCount: Long = 4L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
