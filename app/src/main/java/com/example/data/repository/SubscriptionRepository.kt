package com.example.data.repository

import android.util.Base64
import com.example.core.ConfigParser
import com.example.data.local.ConfigDao
import com.example.data.local.ProxyProfile
import com.example.data.local.SubscriptionDao
import com.example.data.local.SubscriptionGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val configDao: ConfigDao,
    private val configParser: ConfigParser = ConfigParser()
) {
    private val client = OkHttpClient()

    val allSubscriptions = subscriptionDao.getAllSubscriptions()
    val allConfigs = configDao.getAllConfigsSortedByLatency()

    suspend fun addSubscription(name: String, url: String) {
        val groupId = subscriptionDao.insert(SubscriptionGroup(name = name, url = url))
        fetchAndUpdateSubscription(groupId.toInt(), url)
    }

    suspend fun fetchAndUpdateSubscription(groupId: Int, url: String) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext

            val body = response.body?.string() ?: return@withContext
            // Decode base64 if needed
            val decodedString = try {
                if (body.contains("://")) body else String(Base64.decode(body.trim(), Base64.DEFAULT))
            } catch (e: Exception) {
                body
            }

            val lines = decodedString.lines().filter { it.isNotBlank() }
            val configs = mutableListOf<ProxyProfile>()

            for (line in lines) {
                val parsed = configParser.parse(line.trim())
                if (parsed != null) {
                    val profileName = try {
                        val uri = android.net.Uri.parse(line.trim())
                        java.net.URLDecoder.decode(uri.fragment ?: parsed.address, "UTF-8")
                    } catch (e: Exception) {
                        parsed.address
                    }
                    configs.add(
                        ProxyProfile(
                            subscriptionId = groupId,
                            originalUri = line.trim(),
                            name = profileName,
                            protocol = parsed.protocol,
                            address = parsed.address,
                            port = parsed.port
                        )
                    )
                }
            }

            if (configs.isNotEmpty()) {
                configDao.deleteBySubscriptionId(groupId)
                configDao.insertAll(configs)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
