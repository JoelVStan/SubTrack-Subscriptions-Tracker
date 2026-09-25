package com.example.data

import com.example.model.Subscription
import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    fun getSubscriptionsForUser(userEmail: String): Flow<List<Subscription>> {
        return subscriptionDao.getSubscriptionsForUser(userEmail)
    }

    fun getActiveSubscriptionsForUser(userEmail: String): Flow<List<Subscription>> {
        return subscriptionDao.getActiveSubscriptionsForUser(userEmail)
    }

    fun getSubscription(id: Long): Flow<Subscription?> {
        return subscriptionDao.getSubscriptionById(id)
    }

    suspend fun insert(subscription: Subscription): Long {
        return subscriptionDao.insert(subscription)
    }

    suspend fun update(subscription: Subscription) {
        subscriptionDao.update(subscription)
    }

    suspend fun delete(subscription: Subscription) {
        subscriptionDao.delete(subscription)
    }

    suspend fun deleteById(id: Long) {
        subscriptionDao.deleteById(id)
    }

    suspend fun resetWithSampleDataForUser(userEmail: String) {
        subscriptionDao.deleteAllForUser(userEmail)
        val samples = SampleDataProvider.getSampleSubscriptions().map { it.copy(userEmail = userEmail) }
        subscriptionDao.insertAll(samples)
    }

    suspend fun initDefaultDataForUserIfEmpty(userEmail: String) {
        if (userEmail.isNotBlank() && subscriptionDao.getSubscriptionCountForUser(userEmail) == 0) {
            val samples = SampleDataProvider.getSampleSubscriptions().map { it.copy(userEmail = userEmail) }
            subscriptionDao.insertAll(samples)
        }
    }
}
