package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Subscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions WHERE LOWER(userEmail) = LOWER(:userEmail) ORDER BY nextBillingDate ASC")
    fun getSubscriptionsForUser(userEmail: String): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions ORDER BY nextBillingDate ASC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id LIMIT 1")
    fun getSubscriptionById(id: Long): Flow<Subscription?>

    @Query("SELECT * FROM subscriptions WHERE LOWER(userEmail) = LOWER(:userEmail) AND isAutoRenew = 1 ORDER BY nextBillingDate ASC")
    fun getActiveSubscriptionsForUser(userEmail: String): Flow<List<Subscription>>

    @Query("SELECT COUNT(*) FROM subscriptions WHERE LOWER(userEmail) = LOWER(:userEmail)")
    suspend fun getSubscriptionCountForUser(userEmail: String): Int

    @Query("SELECT COUNT(*) FROM subscriptions")
    suspend fun getSubscriptionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: Subscription): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subscriptions: List<Subscription>)

    @Update
    suspend fun update(subscription: Subscription)

    @Delete
    suspend fun delete(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM subscriptions WHERE LOWER(userEmail) = LOWER(:userEmail)")
    suspend fun deleteAllForUser(userEmail: String)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}
