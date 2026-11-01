package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.MatchEntity
import com.example.data.local.WicketXDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.UUID

class CricketRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private lateinit var localDb: WicketXDatabase
    private lateinit var repository: CricketRepository

    @Before
    override fun setUpFirebase() {
        super.setUpFirebase()
        val context = ApplicationProvider.getApplicationContext<Context>()
        localDb = Room.inMemoryDatabaseBuilder(context, WicketXDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CricketRepository(firestore, localDb)
    }

    @After
    override fun tearDownFirebase() {
        localDb.close()
        super.tearDownFirebase()
    }

    @Test
    fun createAndSaveMatch_authenticatedUser_succeedsLocallyAndInCloud() = runBlocking {
        val uid = signInTestUser("scorer_alice@cricket.com")
        val matchId = "m_" + UUID.randomUUID().toString().take(8)

        val match = MatchEntity(
            id = matchId,
            creatorId = uid,
            teamAName = "Mumbai CC",
            teamBName = "Delhi CC",
            matchFormat = "T20",
            totalOvers = 20,
            status = "live"
        )

        withTimeout(5000L) {
            val result = repository.saveMatch(match)
            assertTrue(result.isSuccess)
        }

        // Verify local Room save
        val localMatch = repository.getMatch(matchId)
        assertNotNull(localMatch)
        assertEquals("Mumbai CC", localMatch?.teamAName)

        // Verify cloud document exists
        val cloudDoc = withTimeout(5000L) {
            firestore.collection("matches").document(matchId).get().await()
        }
        assertTrue(cloudDoc.exists())
        assertEquals(uid, cloudDoc.getString("creatorId"))
    }

    @Test
    fun updateMatch_crossUserUnauthorized_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice@cricket.com")
        val matchId = "m_alice_" + UUID.randomUUID().toString().take(8)

        // Alice creates match
        withTimeout(5000L) {
            firestore.collection("matches").document(matchId).set(
                hashMapOf(
                    "id" to matchId,
                    "creatorId" to aliceUid,
                    "teamAName" to "Team A",
                    "teamBName" to "Team B",
                    "matchFormat" to "T20",
                    "totalOvers" to 20,
                    "status" to "live",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // Sign in as Bob
        signInTestUser("bob@cricket.com")

        // Bob tries to update Alice's match directly
        try {
            withTimeout(5000L) {
                firestore.collection("matches").document(matchId).update("status", "completed").await()
            }
            fail("Expected PERMISSION_DENIED when Bob updates Alice's match")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected FirebaseFirestoreException but got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    @Test
    fun queryMatches_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        try {
            withTimeout(5000L) {
                firestore.collection("matches").get().await()
            }
            fail("Expected PERMISSION_DENIED when unauthenticated user queries matches")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected FirebaseFirestoreException but got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }
}
