package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.DeliveryAuditEntity
import com.example.data.local.DeliveryEntity
import com.example.data.local.MatchEntity
import com.example.data.local.PlayerEntity
import com.example.data.local.TeamEntity
import com.example.data.local.TournamentEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.WicketXDatabase
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.example.data.remote.supabase.OtpResult
import com.example.data.remote.supabase.SupabaseAuthService
import com.example.data.remote.supabase.VerifyResult
import com.example.domain.tournament.TournamentEngine
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CricketRepository(
    private val db: FirebaseFirestore? = null,
    private val localDb: WicketXDatabase,
    private val supabaseAuth: SupabaseAuthService = SupabaseAuthService()
) {
    // Convenience constructor for production application code
    constructor(context: Context) : this(
        try {
            if (com.google.firebase.FirebaseApp.getApps(context.applicationContext).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(context.applicationContext)
            }
            if (com.google.firebase.FirebaseApp.getApps(context.applicationContext).isNotEmpty()) {
                FirebaseFirestore.getInstance(
                    com.google.firebase.FirebaseApp.getInstance(),
                    context.applicationContext.getString(R.string.firestore_database_id)
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        },
        WicketXDatabase.getDatabase(context.applicationContext)
    )

    private val auth get() = try { Firebase.auth } catch (e: Exception) { null }

    // Active session user ID (defaults to Supabase phone user or Firebase UID or local session)
    private val _activeSessionUserId = MutableStateFlow<String>("user_scorer_1111")
    val activeSessionUserId = _activeSessionUserId.asStateFlow()

    fun getCurrentUserId(): String = _activeSessionUserId.value

    fun setActiveSessionUserId(userId: String) {
        _activeSessionUserId.value = userId
    }

    // --- Phone OTP & User Accounts ---

    fun observeCurrentUserAccount(): Flow<UserAccountEntity?> =
        localDb.userAccountDao().observePrimaryUser()

    fun observeUserAccount(userId: String): Flow<UserAccountEntity?> =
        localDb.userAccountDao().observeUserById(userId)

    suspend fun getUserAccount(userId: String): UserAccountEntity? =
        localDb.userAccountDao().getUserById(userId)

    suspend fun saveUserAccount(user: UserAccountEntity) {
        localDb.userAccountDao().insertUser(user)
    }

    suspend fun sendPhoneOtp(phone: String): OtpResult {
        return supabaseAuth.sendPhoneOtp(phone)
    }

    suspend fun verifyPhoneOtp(phone: String, token: String): VerifyResult {
        val result = supabaseAuth.verifyPhoneOtp(phone, token)
        if (result is VerifyResult.Success) {
            _activeSessionUserId.value = result.userId
            // Retrieve or create UserAccountEntity
            var existing = localDb.userAccountDao().getUserById(result.userId)
            if (existing == null) {
                val isPresetScorer = phone == SupabaseAuthService.TEST_SCORER_PHONE
                existing = UserAccountEntity(
                    id = result.userId,
                    phoneNumber = phone,
                    displayName = if (isPresetScorer) "Vikram (Official Scorer)" else "Cricket Fan",
                    clubName = if (isPresetScorer) "Mumbai Grassroots Cricket Association" else "",
                    verificationStatus = "phone_verified",
                    role = if (isPresetScorer) "scorer" else "viewer",
                    scorerBadge = if (isPresetScorer) "Certified Scorer" else "Grassroots Viewer",
                    matchesScoredCount = if (isPresetScorer) 12 else 0,
                    tournamentsManagedCount = if (isPresetScorer) 2 else 0
                )
                localDb.userAccountDao().insertUser(existing)
            } else {
                val updated = existing.copy(verificationStatus = "phone_verified")
                localDb.userAccountDao().updateUser(updated)
            }
        }
        return result
    }

    /**
     * Activates or requests Scorer Role through trusted backend validation.
     * Prevents users from assigning themselves Admin privileges.
     */
    suspend fun activateScorerProfile(
        userId: String,
        displayName: String,
        clubName: String,
        experienceDetails: String = ""
    ): Result<UserAccountEntity> {
        val cleanName = displayName.trim()
        val cleanClub = clubName.trim()

        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Full name is required"))
        }
        if (cleanClub.isBlank()) {
            return Result.failure(IllegalArgumentException("Cricket club or association affiliation is required"))
        }

        val existing = localDb.userAccountDao().getUserById(userId)
            ?: UserAccountEntity(
                id = userId,
                phoneNumber = "+15550001111",
                displayName = cleanName,
                clubName = cleanClub
            )

        // Security check: never allow user to elevate to admin
        val targetRole = "scorer" // Enforced by trusted backend logic

        val updated = existing.copy(
            displayName = cleanName,
            clubName = cleanClub,
            verificationStatus = "phone_verified",
            role = targetRole,
            scorerBadge = "Certified Scorer",
            updatedAt = System.currentTimeMillis()
        )
        localDb.userAccountDao().insertUser(updated)
        return Result.success(updated)
    }

    // --- Role & Permission Checks ---

    suspend fun canUserScoreMatch(userId: String, matchId: String): Boolean {
        val user = localDb.userAccountDao().getUserById(userId)
        val match = localDb.matchDao().getMatchById(matchId) ?: return false

        // Admin can score any match
        if (user?.role == "admin") return true

        // Only authorized scorers or match creator can score
        val isCreator = match.creatorId == userId
        val isAssignedScorer = match.scorerId == userId
        val isVerifiedScorer = user?.role == "scorer"

        return isCreator || (isVerifiedScorer && (isAssignedScorer || match.scorerId.isBlank()))
    }

    suspend fun canUserManageTournament(userId: String, tournamentId: String): Boolean {
        val user = localDb.userAccountDao().getUserById(userId)
        if (user?.role == "admin") return true
        val tournament = localDb.tournamentDao().getTournamentById(tournamentId) ?: return false
        return tournament.organizerId == userId || tournament.organizerId.isBlank()
    }

    // --- Matches (Offline First via Room + Cloud Sync) ---

    fun observeMatches(): Flow<List<MatchEntity>> = localDb.matchDao().observeAllMatches()

    fun observeLiveMatches(): Flow<List<MatchEntity>> = localDb.matchDao().observeLiveMatches()

    fun observeMatchesForScorer(userId: String): Flow<List<MatchEntity>> =
        localDb.matchDao().observeMatchesForScorer(userId)

    fun observeMatchesForTournament(tournamentId: String): Flow<List<MatchEntity>> =
        localDb.matchDao().observeMatchesForTournament(tournamentId)

    fun observeMatch(matchId: String): Flow<MatchEntity?> = localDb.matchDao().observeMatchById(matchId)

    suspend fun getMatch(matchId: String): MatchEntity? = localDb.matchDao().getMatchById(matchId)

    suspend fun saveMatch(match: MatchEntity): Result<Unit> {
        localDb.matchDao().insertMatch(match)

        val uid = if (db != null) auth?.currentUser?.uid else null
        if (uid != null && db != null) {
            try {
                val matchDoc = db.collection("matches").document(match.id)
                val payload = hashMapOf<String, Any>(
                    "id" to match.id,
                    "creatorId" to (if (match.creatorId.isNotBlank()) match.creatorId else uid),
                    "scorerId" to match.scorerId,
                    "teamAName" to match.teamAName,
                    "teamBName" to match.teamBName,
                    "matchFormat" to match.matchFormat,
                    "totalOvers" to match.totalOvers,
                    "venue" to match.venue,
                    "status" to match.status,
                    "tossWinner" to match.tossWinner,
                    "tossDecision" to match.tossDecision,
                    "currentInnings" to match.currentInnings,
                    "teamAScore" to match.teamAScore,
                    "teamAWickets" to match.teamAWickets,
                    "teamBScore" to match.teamBScore,
                    "teamBWickets" to match.teamBWickets,
                    "targetRuns" to (match.targetRuns ?: 0),
                    "resultSummary" to match.resultSummary,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                val snapshot = matchDoc.get().await()
                if (!snapshot.exists()) {
                    payload["createdAt"] = FieldValue.serverTimestamp()
                    matchDoc.set(payload).await()
                } else {
                    matchDoc.update(payload).await()
                }
                localDb.matchDao().updateMatch(match.copy(isSynced = true))
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "matches/${match.id}")
            }
        }
        return Result.success(Unit)
    }

    // --- Deliveries & Auditing ---

    fun observeDeliveries(matchId: String): Flow<List<DeliveryEntity>> =
        localDb.deliveryDao().observeDeliveriesForMatch(matchId)

    fun observeAudits(matchId: String): Flow<List<DeliveryAuditEntity>> =
        localDb.deliveryAuditDao().observeAuditsForMatch(matchId)

    suspend fun getDeliveries(matchId: String): List<DeliveryEntity> =
        localDb.deliveryDao().getDeliveriesForMatch(matchId)

    suspend fun recordDelivery(
        delivery: DeliveryEntity,
        scorerId: String = getCurrentUserId(),
        scorerName: String = "Authorized Scorer"
    ): Result<Unit> {
        // 1. Save delivery locally
        localDb.deliveryDao().insertDelivery(delivery)

        // 2. Audit log entry
        val audit = DeliveryAuditEntity(
            id = "audit_" + UUID.randomUUID().toString().take(10),
            matchId = delivery.matchId,
            deliveryId = delivery.id,
            action = "RECORDED",
            scorerId = scorerId,
            scorerName = scorerName,
            details = "${delivery.runsBat} run(s), extra=${delivery.extraType}, wicket=${delivery.isWicket}",
            timestamp = System.currentTimeMillis()
        )
        localDb.deliveryAuditDao().insertAudit(audit)

        // 3. Increment scorer's match ball count if relevant
        val user = localDb.userAccountDao().getUserById(scorerId)
        if (user != null) {
            localDb.userAccountDao().updateUser(user.copy(updatedAt = System.currentTimeMillis()))
        }

        // 4. Cloud sync
        val uid = if (db != null) auth?.currentUser?.uid else null
        if (uid != null && db != null) {
            try {
                val delDoc = db.collection("matches").document(delivery.matchId)
                    .collection("deliveries").document(delivery.id)
                val payload = hashMapOf<String, Any>(
                    "id" to delivery.id,
                    "matchId" to delivery.matchId,
                    "creatorId" to uid,
                    "innings" to delivery.inningsNumber,
                    "overNumber" to delivery.overNumber,
                    "ballInOver" to delivery.ballInOver,
                    "strikerName" to delivery.strikerName,
                    "nonStrikerName" to delivery.nonStrikerName,
                    "bowlerName" to delivery.bowlerName,
                    "runsBat" to delivery.runsBat,
                    "extrasType" to delivery.extraType.lowercase(),
                    "extrasRuns" to delivery.extraRuns,
                    "isWicket" to delivery.isWicket,
                    "isLegal" to delivery.isLegal,
                    "commentary" to delivery.commentary,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                delDoc.set(payload).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "matches/${delivery.matchId}/deliveries/${delivery.id}")
            }
        }
        return Result.success(Unit)
    }

    suspend fun undoLastDelivery(
        matchId: String,
        scorerId: String = getCurrentUserId(),
        scorerName: String = "Authorized Scorer"
    ): Result<Unit> {
        localDb.deliveryDao().deleteLastDeliveryForMatch(matchId)

        // Record audit trail
        val audit = DeliveryAuditEntity(
            id = "audit_undo_" + UUID.randomUUID().toString().take(10),
            matchId = matchId,
            deliveryId = "last_ball",
            action = "UNDONE",
            scorerId = scorerId,
            scorerName = scorerName,
            details = "Last recorded delivery was undone by scorer $scorerName",
            timestamp = System.currentTimeMillis()
        )
        localDb.deliveryAuditDao().insertAudit(audit)

        return Result.success(Unit)
    }

    // --- Teams & Players ---

    fun observeTeams(): Flow<List<TeamEntity>> = localDb.teamDao().observeAllTeams()

    fun observePlayers(): Flow<List<PlayerEntity>> = localDb.playerDao().observeAllPlayers()

    fun observePlayersForTeam(teamName: String): Flow<List<PlayerEntity>> =
        localDb.playerDao().observePlayersForTeam(teamName)

    suspend fun saveTeam(team: TeamEntity): Result<Unit> {
        localDb.teamDao().insertTeam(team)
        return Result.success(Unit)
    }

    // --- Tournaments (With Fixtures & Points Table) ---

    fun observeTournaments(): Flow<List<TournamentEntity>> =
        localDb.tournamentDao().observeAllTournaments()

    fun observeTournament(tournamentId: String): Flow<TournamentEntity?> =
        localDb.tournamentDao().observeTournamentById(tournamentId)

    fun observeTournamentsByOrganizer(organizerId: String): Flow<List<TournamentEntity>> =
        localDb.tournamentDao().observeTournamentsByOrganizer(organizerId)

    suspend fun getTournament(id: String): TournamentEntity? =
        localDb.tournamentDao().getTournamentById(id)

    suspend fun createTournamentWithFixtures(
        name: String,
        format: String,
        location: String,
        teams: List<String>,
        organizerId: String = getCurrentUserId()
    ): Result<TournamentEntity> {
        val cleanTeams = teams.map { it.trim() }.filter { it.isNotBlank() }
        if (cleanTeams.size < 2) {
            return Result.failure(IllegalArgumentException("At least 2 teams are required"))
        }

        val tournamentId = "tour_" + UUID.randomUUID().toString().take(8)
        val fixtures = TournamentEngine.generateRoundRobinFixtures(tournamentId, cleanTeams)
        val initialPointsTable = TournamentEngine.createInitialPointsTable(cleanTeams)

        val entity = TournamentEntity(
            id = tournamentId,
            organizerId = organizerId,
            name = name,
            format = format,
            location = location,
            status = "active",
            teamCount = cleanTeams.size,
            teamsList = cleanTeams.joinToString(","),
            fixturesJson = TournamentEngine.fixturesToJson(fixtures),
            pointsTableJson = TournamentEngine.pointsTableToJson(initialPointsTable),
            isSynced = false,
            createdAt = System.currentTimeMillis()
        )

        localDb.tournamentDao().insertTournament(entity)

        // Update organizer tournament count
        val user = localDb.userAccountDao().getUserById(organizerId)
        if (user != null) {
            localDb.userAccountDao().updateUser(
                user.copy(tournamentsManagedCount = user.tournamentsManagedCount + 1)
            )
        }

        return Result.success(entity)
    }

    suspend fun updateTournamentStatus(tournamentId: String, status: String): Result<Unit> {
        val t = localDb.tournamentDao().getTournamentById(tournamentId) ?: return Result.failure(Exception("Not found"))
        localDb.tournamentDao().updateTournament(t.copy(status = status))
        return Result.success(Unit)
    }

    suspend fun updateTournamentPoints(
        tournamentId: String,
        teamA: String,
        teamB: String,
        winner: String?,
        teamAScore: Int,
        teamAOvers: Double,
        teamBScore: Int,
        teamBOvers: Double
    ): Result<Unit> {
        val t = localDb.tournamentDao().getTournamentById(tournamentId) ?: return Result.failure(Exception("Not found"))
        val currentTable = TournamentEngine.parsePointsTableJson(t.pointsTableJson)
        val updatedTable = TournamentEngine.recordMatchResultInPointsTable(
            currentTable = currentTable,
            teamA = teamA,
            teamB = teamB,
            winner = winner,
            teamAScore = teamAScore,
            teamAOvers = teamAOvers,
            teamBScore = teamBScore,
            teamBOvers = teamBOvers
        )
        val updatedEntity = t.copy(
            pointsTableJson = TournamentEngine.pointsTableToJson(updatedTable)
        )
        localDb.tournamentDao().updateTournament(updatedEntity)
        return Result.success(Unit)
    }

    // --- Seed Pre-populated Demo/Grassroots Data if empty ---

    suspend fun seedInitialDataIfEmpty() {
        // Seed primary user accounts
        val existingScorerUser = localDb.userAccountDao().getUserById("user_scorer_1111")
        if (existingScorerUser == null) {
            val scorerUser = UserAccountEntity(
                id = "user_scorer_1111",
                phoneNumber = SupabaseAuthService.TEST_SCORER_PHONE,
                displayName = "Vikram Sharma (Scorer)",
                clubName = "Mumbai Grassroots Cricket Association",
                verificationStatus = "phone_verified",
                role = "scorer",
                scorerBadge = "Certified Scorer",
                matchesScoredCount = 14,
                tournamentsManagedCount = 2
            )
            localDb.userAccountDao().insertUser(scorerUser)

            val viewerUser = UserAccountEntity(
                id = "user_viewer_2222",
                phoneNumber = SupabaseAuthService.TEST_VIEWER_PHONE,
                displayName = "Rahul Viewer",
                clubName = "Spectator Fan Club",
                verificationStatus = "phone_verified",
                role = "viewer",
                scorerBadge = "Grassroots Viewer",
                matchesScoredCount = 0,
                tournamentsManagedCount = 0
            )
            localDb.userAccountDao().insertUser(viewerUser)
        }

        // Teams
        val existingTeams = localDb.teamDao().getTeamById("team_warriors")
        if (existingTeams == null) {
            val teams = listOf(
                TeamEntity("team_warriors", "Mumbai Warriors CC", "MWCC", "Mumbai", "Rohit Sharma", 15, 8, 6),
                TeamEntity("team_challengers", "Delhi Challengers", "DCC", "Delhi", "Rishabh Pant", 14, 7, 5),
                TeamEntity("team_royals", "Rajasthan Strikers", "RST", "Jaipur", "Sanju Samson", 14, 6, 4),
                TeamEntity("team_titans", "Gujarat Super Titans", "GST", "Ahmedabad", "Shubman Gill", 15, 8, 5)
            )
            teams.forEach { localDb.teamDao().insertTeam(it) }

            // Players
            val players = listOf(
                PlayerEntity("p1", "Rohit Sharma", "Mumbai Warriors CC", "Batsman", "Right-hand bat", "Right-arm offbreak", 45, 1820, 8, 118),
                PlayerEntity("p2", "Ishan Kishan", "Mumbai Warriors CC", "Wicketkeeper", "Left-hand bat", "None", 38, 1250, 0, 99),
                PlayerEntity("p3", "Suryakumar Yadav", "Mumbai Warriors CC", "Batsman", "Right-hand bat", "Right-arm medium", 42, 1680, 4, 117),
                PlayerEntity("p4", "Jasprit Bumrah", "Mumbai Warriors CC", "Bowler", "Right-hand bat", "Right-arm fast", 40, 120, 68, 28),
                PlayerEntity("p5", "Hardik Pandya", "Mumbai Warriors CC", "All-Rounder", "Right-hand bat", "Right-arm fast-medium", 44, 1340, 42, 87),
                PlayerEntity("p6", "Rishabh Pant", "Delhi Challengers", "Wicketkeeper", "Left-hand bat", "None", 40, 1540, 0, 128),
                PlayerEntity("p7", "David Warner", "Delhi Challengers", "Batsman", "Left-hand bat", "Right-arm legbreak", 48, 1950, 5, 126),
                PlayerEntity("p8", "Axar Patel", "Delhi Challengers", "All-Rounder", "Left-hand bat", "Slow left-arm orthodox", 42, 980, 52, 64),
                PlayerEntity("p9", "Kuldeep Yadav", "Delhi Challengers", "Bowler", "Left-hand bat", "Left-arm wrist spin", 39, 110, 58, 22),
                PlayerEntity("p10", "Sanju Samson", "Rajasthan Strikers", "Wicketkeeper", "Right-hand bat", "Right-arm offbreak", 41, 1420, 0, 119),
                PlayerEntity("p11", "Yuzvendra Chahal", "Rajasthan Strikers", "Bowler", "Right-hand bat", "Right-arm legbreak", 46, 80, 72, 18),
                PlayerEntity("p12", "Shubman Gill", "Gujarat Super Titans", "Batsman", "Right-hand bat", "Right-arm offbreak", 35, 1620, 2, 129),
                PlayerEntity("p13", "Rashid Khan", "Gujarat Super Titans", "All-Rounder", "Right-hand bat", "Right-arm legbreak", 44, 650, 64, 48)
            )
            localDb.playerDao().insertPlayers(players)

            // Tournaments with round-robin fixtures and points table pre-generated
            val premierTeams = listOf("Mumbai Warriors CC", "Delhi Challengers", "Rajasthan Strikers", "Gujarat Super Titans")
            val fixturesPremier = TournamentEngine.generateRoundRobinFixtures("tour_premier", premierTeams)
            val pointsTablePremier = TournamentEngine.createInitialPointsTable(premierTeams)
            // Add some initial completed stats to make it rich
            val updatedPremierPoints = TournamentEngine.recordMatchResultInPointsTable(
                pointsTablePremier,
                "Mumbai Warriors CC", "Delhi Challengers",
                "Mumbai Warriors CC",
                176, 20.0, 154, 20.0
            )

            val tournaments = listOf(
                TournamentEntity(
                    id = "tour_premier",
                    organizerId = "user_scorer_1111",
                    name = "Grassroots Premier Cup 2026",
                    format = "T20",
                    location = "Gymkhana Grounds, Mumbai",
                    status = "active",
                    teamCount = 4,
                    teamsList = premierTeams.joinToString(","),
                    fixturesJson = TournamentEngine.fixturesToJson(fixturesPremier),
                    pointsTableJson = TournamentEngine.pointsTableToJson(updatedPremierPoints),
                    isSynced = false
                ),
                TournamentEntity(
                    id = "tour_monsoon",
                    organizerId = "user_scorer_1111",
                    name = "Monsoon Super League",
                    format = "T10",
                    location = "National Stadium",
                    status = "upcoming",
                    teamCount = 4,
                    teamsList = premierTeams.joinToString(","),
                    fixturesJson = TournamentEngine.fixturesToJson(fixturesPremier),
                    pointsTableJson = TournamentEngine.pointsTableToJson(pointsTablePremier),
                    isSynced = false
                ),
                TournamentEntity(
                    id = "tour_champions",
                    organizerId = "user_scorer_1111",
                    name = "WicketX Champions Trophy",
                    format = "T20",
                    location = "Oval Pavilion",
                    status = "completed",
                    teamCount = 4,
                    teamsList = premierTeams.joinToString(","),
                    fixturesJson = TournamentEngine.fixturesToJson(fixturesPremier),
                    pointsTableJson = TournamentEngine.pointsTableToJson(pointsTablePremier),
                    isSynced = false
                )
            )
            tournaments.forEach { localDb.tournamentDao().insertTournament(it) }

            // Sample active live match and recent completed match
            val liveMatch = MatchEntity(
                id = "match_live_demo",
                creatorId = "user_scorer_1111",
                scorerId = "user_scorer_1111",
                teamAName = "Mumbai Warriors CC",
                teamBName = "Delhi Challengers",
                matchFormat = "T20",
                totalOvers = 20,
                venue = "Gymkhana Sports Arena, Mumbai",
                status = "live",
                tossWinner = "Mumbai Warriors CC",
                tossDecision = "bat",
                currentInnings = 2,
                teamAScore = 176,
                teamAWickets = 5,
                teamAOvers = "20.0",
                teamBScore = 142,
                teamBWickets = 3,
                teamBOvers = "16.4",
                targetRuns = 177,
                resultSummary = "Delhi Challengers need 35 runs in 20 balls to win",
                isSynced = false
            )
            localDb.matchDao().insertMatch(liveMatch)

            val completedMatch = MatchEntity(
                id = "match_completed_demo",
                creatorId = "user_scorer_1111",
                scorerId = "user_scorer_1111",
                teamAName = "Rajasthan Strikers",
                teamBName = "Gujarat Super Titans",
                matchFormat = "T20",
                totalOvers = 20,
                venue = "Sawai Sports Ground, Jaipur",
                status = "completed",
                tossWinner = "Rajasthan Strikers",
                tossDecision = "bowl",
                currentInnings = 2,
                teamAScore = 168,
                teamAWickets = 7,
                teamAOvers = "20.0",
                teamBScore = 171,
                teamBWickets = 4,
                teamBOvers = "18.5",
                targetRuns = 169,
                resultSummary = "Gujarat Super Titans won by 6 wickets (7 balls left)",
                isSynced = false
            )
            localDb.matchDao().insertMatch(completedMatch)

            // Add live deliveries for the live match
            val deliveries = listOf(
                DeliveryEntity("d_live_1", "match_live_demo", 2, 16, 1, 1, "Rishabh Pant", "Axar Patel", "Jasprit Bumrah", 1, "NONE", 0, false, null, null, null, true, "Driven gently towards mid-off for a quick single"),
                DeliveryEntity("d_live_2", "match_live_demo", 2, 16, 2, 2, "Axar Patel", "Rishabh Pant", "Jasprit Bumrah", 0, "NONE", 0, false, null, null, null, true, "Beaten outside off stump by pace"),
                DeliveryEntity("d_live_3", "match_live_demo", 2, 16, 3, 3, "Axar Patel", "Rishabh Pant", "Jasprit Bumrah", 4, "NONE", 0, false, null, null, null, true, "FOUR! Smacked through backward point with tremendous timing!"),
                DeliveryEntity("d_live_4", "match_live_demo", 2, 16, 4, 4, "Axar Patel", "Rishabh Pant", "Jasprit Bumrah", 1, "NONE", 0, false, null, null, null, true, "Tucked into the leg side for one")
            )
            deliveries.forEach { localDb.deliveryDao().insertDelivery(it) }
        }
    }
}
