package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionGroup): Long

    @Delete
    suspend fun delete(subscription: SubscriptionGroup)
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM configs ORDER BY lastLatencyMs ASC")
    fun getAllConfigsSortedByLatency(): Flow<List<ProxyProfile>>

    @Query("SELECT * FROM configs WHERE subscriptionId = :subId")
    fun getConfigsBySubscription(subId: Int): Flow<List<ProxyProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<ProxyProfile>)

    @Update
    suspend fun update(config: ProxyProfile)

    @Query("UPDATE configs SET lastLatencyMs = :latency, healthScore = :health WHERE id = :id")
    suspend fun updateLatencyAndHealth(id: Int, latency: Long, health: Int)

    @Delete
    suspend fun delete(config: ProxyProfile)

    @Query("DELETE FROM configs WHERE subscriptionId = :subId")
    suspend fun deleteBySubscriptionId(subId: Int)
}
