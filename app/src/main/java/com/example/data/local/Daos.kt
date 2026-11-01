package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY updatedAt DESC")
    fun observeAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = 'live' ORDER BY updatedAt DESC")
    fun observeLiveMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = 'completed' ORDER BY updatedAt DESC")
    fun observeCompletedMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE creatorId = :userId OR scorerId = :userId ORDER BY updatedAt DESC")
    fun observeMatchesForScorer(userId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY updatedAt DESC")
    fun observeMatchesForTournament(tournamentId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun observeMatchById(matchId: String): Flow<MatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatch(matchId: String)
}

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries WHERE matchId = :matchId ORDER BY timestamp ASC")
    fun observeDeliveriesForMatch(matchId: String): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE matchId = :matchId ORDER BY timestamp ASC")
    suspend fun getDeliveriesForMatch(matchId: String): List<DeliveryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: DeliveryEntity)

    @Query("DELETE FROM deliveries WHERE id = :deliveryId")
    suspend fun deleteDelivery(deliveryId: String)

    @Query("DELETE FROM deliveries WHERE matchId = :matchId AND id = (SELECT id FROM deliveries WHERE matchId = :matchId ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLastDeliveryForMatch(matchId: String)
}

@Dao
interface DeliveryAuditDao {
    @Query("SELECT * FROM delivery_audits WHERE matchId = :matchId ORDER BY timestamp DESC")
    fun observeAuditsForMatch(matchId: String): Flow<List<DeliveryAuditEntity>>

    @Query("SELECT * FROM delivery_audits WHERE matchId = :matchId ORDER BY timestamp DESC")
    suspend fun getAuditsForMatch(matchId: String): List<DeliveryAuditEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: DeliveryAuditEntity)
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun observeAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: String): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)
}

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY runs DESC")
    fun observeAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE teamName = :teamName ORDER BY name ASC")
    fun observePlayersForTeam(teamName: String): Flow<List<PlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY createdAt DESC")
    fun observeAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE organizerId = :organizerId ORDER BY createdAt DESC")
    fun observeTournamentsByOrganizer(organizerId: String): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getTournamentById(id: String): TournamentEntity?

    @Query("SELECT * FROM tournaments WHERE id = :id")
    fun observeTournamentById(id: String): Flow<TournamentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE id = :id")
    suspend fun getUserById(id: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id")
    fun observeUserById(id: String): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts LIMIT 1")
    fun observePrimaryUser(): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    @Update
    suspend fun updateUser(user: UserAccountEntity)
}
