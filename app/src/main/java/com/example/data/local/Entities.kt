package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionGroup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val totalConfigs: Int = 0
)

@Entity(tableName = "configs")
data class ProxyProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subscriptionId: Int? = null,
    val originalUri: String,
    val name: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val lastLatencyMs: Long = -1,
    val healthScore: Int = 0,
    val isFavorite: Boolean = false
)
