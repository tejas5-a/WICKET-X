package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DeliveryEntity
import com.example.data.local.MatchEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.WicketXDatabase
import com.example.data.remote.supabase.OtpResult
import com.example.data.remote.supabase.SupabaseAuthService
import com.example.data.remote.supabase.VerifyResult
import com.example.data.repository.CricketRepository
import com.example.domain.engine.CricketScoringEngine
import com.example.domain.engine.ExtraType
import com.example.domain.tournament.TournamentEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ScorerAndTournamentAccessTest {

    private lateinit var localDb: WicketXDatabase
    private lateinit var authService: SupabaseAuthService
    private lateinit var repository: CricketRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        localDb = Room.inMemoryDatabaseBuilder(context, WicketXDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        authService = SupabaseAuthService()
        // Repository with mock or in-memory components
        repository = CricketRepository(
            db = null,
            localDb = localDb,
            supabaseAuth = authService
        )
    }

    @After
    fun tearDown() {
        localDb.close()
    }

    @Test
    fun testPhoneOtpFlow_successAndInvalidCode() = runBlocking {
        val phone = SupabaseAuthService.TEST_SCORER_PHONE

        // 1. Send OTP
        val sendResult = authService.sendPhoneOtp(phone)
        assertTrue(sendResult is OtpResult.Success)

        // 2. Cooldown check: requesting immediately should trigger cooldown
        val immediateRepeat = authService.sendPhoneOtp(phone)
        assertTrue(immediateRepeat is OtpResult.CooldownActive)

        // 3. Invalid OTP should be rejected
        val badVerify = authService.verifyPhoneOtp(phone, "999999")
        assertTrue(badVerify is VerifyResult.Error)

        // 4. Correct 6-digit OTP succeeds
        val validVerify = authService.verifyPhoneOtp(phone, SupabaseAuthService.TEST_OTP_CODE)
        assertTrue(validVerify is VerifyResult.Success)
        val success = validVerify as VerifyResult.Success
        assertEquals(phone, success.phoneNumber)
        assertNotNull(success.userId)
    }

    @Test
    fun testScorerRoleActivation_andProtectionAgainstAdminPrivileges() = runBlocking {
        val userId = "user_test_scorer_99"
        val user = UserAccountEntity(
            id = userId,
            phoneNumber = "+15551239876",
            displayName = "Arjun Official",
            verificationStatus = "phone_verified",
            role = "viewer"
        )
        localDb.userAccountDao().insertUser(user)

        // User requests Scorer access with club affiliation
        val result = repository.activateScorerProfile(
            userId = userId,
            displayName = "Arjun Official",
            clubName = "National Cricket Academy"
        )
        assertTrue(result.isSuccess)
        val updatedUser = localDb.userAccountDao().getUserById(userId)
        assertNotNull(updatedUser)
        assertEquals("scorer", updatedUser?.role)
        assertEquals("Certified Scorer", updatedUser?.scorerBadge)
        assertEquals("National Cricket Academy", updatedUser?.clubName)

        // Security check: backend explicitly enforces "scorer" and prevents unauthorized self-assignment of "admin"
        assertFalse(updatedUser?.role == "admin")
    }

    @Test
    fun testTournamentCreation_andRoundRobinFixtureGeneration() = runBlocking {
        val teams = listOf("Mumbai Warriors CC", "Delhi Challengers", "Rajasthan Strikers", "Gujarat Super Titans")
        val fixtures = TournamentEngine.generateRoundRobinFixtures("tour_demo", teams)

        // 4 teams in round-robin = (4 * 3) / 2 = 6 unique matches over 3 rounds
        assertEquals(6, fixtures.size)

        // Verify each round has 2 matches
        val round1 = fixtures.filter { it.roundNumber == 1 }
        val round2 = fixtures.filter { it.roundNumber == 2 }
        val round3 = fixtures.filter { it.roundNumber == 3 }
        assertEquals(2, round1.size)
        assertEquals(2, round2.size)
        assertEquals(2, round3.size)

        // Repository tournament creation
        val result = repository.createTournamentWithFixtures(
            name = "Maharashtra Grassroots Trophy",
            format = "T20",
            location = "Gymkhana Mumbai",
            teams = teams,
            organizerId = "user_scorer_1111"
        )
        assertTrue(result.isSuccess)
        val savedTour = repository.getTournament(result.getOrThrow().id)
        assertNotNull(savedTour)
        assertEquals("Maharashtra Grassroots Trophy", savedTour?.name)
        assertEquals(4, savedTour?.teamCount)
    }

    @Test
    fun testPointsTableCalculation_withNetRunRate() {
        val teams = listOf("Mumbai Warriors CC", "Delhi Challengers", "Rajasthan Strikers")
        val initialTable = TournamentEngine.createInitialPointsTable(teams)
        assertEquals(3, initialTable.size)

        // Record Match 1: Mumbai Warriors (180 runs in 20 overs) vs Delhi Challengers (140 runs in 20 overs) -> Mumbai won
        val updatedTable = TournamentEngine.recordMatchResultInPointsTable(
            currentTable = initialTable,
            teamA = "Mumbai Warriors CC",
            teamB = "Delhi Challengers",
            winner = "Mumbai Warriors CC",
            teamAScore = 180,
            teamAOvers = 20.0,
            teamBScore = 140,
            teamBOvers = 20.0
        )

        val mumbai = updatedTable.first { it.teamName == "Mumbai Warriors CC" }
        val delhi = updatedTable.first { it.teamName == "Delhi Challengers" }

        assertEquals(1, mumbai.played)
        assertEquals(1, mumbai.won)
        assertEquals(2, mumbai.points)
        // Mumbai NRR: (180/20) - (140/20) = 9.0 - 7.0 = +2.000
        assertEquals(2.0, mumbai.nrr, 0.001)

        assertEquals(1, delhi.played)
        assertEquals(0, delhi.won)
        assertEquals(1, delhi.lost)
        assertEquals(0, delhi.points)
        // Delhi NRR: (140/20) - (180/20) = 7.0 - 9.0 = -2.000
        assertEquals(-2.0, delhi.nrr, 0.001)

        // Table is sorted with Mumbai at rank 1
        assertEquals("Mumbai Warriors CC", updatedTable[0].teamName)
    }

    @Test
    fun testDeliveryRecordingAndUndo_withAuditTrail() = runBlocking {
        val matchId = "match_audit_test"
        val match = MatchEntity(
            id = matchId,
            creatorId = "user_scorer_1111",
            scorerId = "user_scorer_1111",
            teamAName = "Mumbai Warriors CC",
            teamBName = "Delhi Challengers",
            status = "live"
        )
        localDb.matchDao().insertMatch(match)

        val delivery1 = DeliveryEntity(
            id = "del_audit_1",
            matchId = matchId,
            inningsNumber = 1,
            overNumber = 0,
            ballInOver = 1,
            legalBallInOver = 1,
            strikerName = "Rohit",
            nonStrikerName = "Ishan",
            bowlerName = "Bumrah",
            runsBat = 4,
            commentary = "FOUR! Pierces the off-side field."
        )

        // Record delivery with audit
        repository.recordDelivery(delivery1, scorerId = "user_scorer_1111", scorerName = "Vikram Scorer")

        val deliveries = repository.getDeliveries(matchId)
        assertEquals(1, deliveries.size)

        var audits = repository.observeAudits(matchId).first()
        assertEquals(1, audits.size)
        assertEquals("RECORDED", audits[0].action)
        assertEquals("user_scorer_1111", audits[0].scorerId)

        // Undo delivery with audit
        repository.undoLastDelivery(matchId, scorerId = "user_scorer_1111", scorerName = "Vikram Scorer")

        val deliveriesAfterUndo = repository.getDeliveries(matchId)
        assertEquals(0, deliveriesAfterUndo.size)

        audits = repository.observeAudits(matchId).first()
        assertEquals(2, audits.size)
        assertEquals("UNDONE", audits[0].action)
    }

    @Test
    fun testRoleSeparation_DeviceAAuthorizedScorerVsDeviceBViewer() = runBlocking {
        val matchId = "match_role_cuj"
        val match = MatchEntity(
            id = matchId,
            creatorId = "user_scorer_1111",
            scorerId = "user_scorer_1111",
            teamAName = "Mumbai Warriors CC",
            teamBName = "Delhi Challengers",
            status = "live"
        )
        localDb.matchDao().insertMatch(match)

        // Device A: Authorized Scorer
        val canDeviceAScore = repository.canUserScoreMatch("user_scorer_1111", matchId)
        assertTrue("Device A (Scorer) must have scoring authorization", canDeviceAScore)

        // Device B: Unassigned Viewer / Spectator
        val canDeviceBScore = repository.canUserScoreMatch("user_viewer_2222", matchId)
        assertFalse("Device B (Viewer) must NOT have scoring authorization", canDeviceBScore)
    }
}
