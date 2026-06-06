package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.firstOrNull

class SubscriptionWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(appContext)
        val repository = SubscriptionRepository(database.subscriptionDao(), database.configDao())

        val subscriptions = repository.allSubscriptions.firstOrNull() ?: return Result.success()

        for (sub in subscriptions) {
            repository.fetchAndUpdateSubscription(sub.id, sub.url)
        }

        return Result.success()
    }
}
