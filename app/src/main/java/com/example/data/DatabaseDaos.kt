package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getUserById(id: Long): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile): Long

    @Update
    suspend fun updateUser(user: UserProfile)

    @Query("SELECT * FROM user_profiles")
    fun getAllUsers(): Flow<List<UserProfile>>
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY timestamp DESC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE userId = :userId ORDER BY timestamp DESC")
    fun getProductsByUser(userId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE isFeatured = 1 ORDER BY timestamp DESC")
    fun getFeaturedProducts(): Flow<List<Product>>
}

@Dao
interface BuyRequestDao {
    @Query("SELECT * FROM buy_requests ORDER BY timestamp DESC")
    fun getAllBuyRequests(): Flow<List<BuyRequest>>

    @Query("SELECT * FROM buy_requests WHERE category = :category ORDER BY timestamp DESC")
    fun getBuyRequestsByCategory(category: String): Flow<List<BuyRequest>>

    @Query("SELECT * FROM buy_requests WHERE userId = :userId ORDER BY timestamp DESC")
    fun getBuyRequestsByUser(userId: Long): Flow<List<BuyRequest>>

    @Query("SELECT * FROM buy_requests WHERE id = :id")
    suspend fun getBuyRequestById(id: Long): BuyRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuyRequest(request: BuyRequest): Long

    @Update
    suspend fun updateBuyRequest(request: BuyRequest)

    @Delete
    suspend fun deleteBuyRequest(request: BuyRequest)

    @Query("SELECT * FROM buy_requests WHERE isFeatured = 1 ORDER BY timestamp DESC")
    fun getFeaturedBuyRequests(): Flow<List<BuyRequest>>
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers ORDER BY timestamp DESC")
    fun getAllOffers(): Flow<List<Offer>>

    @Query("SELECT * FROM offers WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getOffersForUser(userId: Long): Flow<List<Offer>>

    @Query("SELECT * FROM offers WHERE id = :id")
    suspend fun getOfferById(id: Long): Offer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer): Long

    @Update
    suspend fun updateOffer(offer: Offer)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getChatHistory(userId: Long, otherId: Long): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getRecentMessagesForUser(userId: Long): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("UPDATE chat_messages SET isRead = 1 WHERE senderId = :otherId AND receiverId = :userId")
    suspend fun markAsRead(userId: Long, otherId: Long)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Dao
interface MarketNewsDao {
    @Query("SELECT * FROM market_news ORDER BY timestamp DESC")
    fun getAllMarketNews(): Flow<List<MarketNews>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketNews(news: MarketNews): Long

    @Query("DELETE FROM market_news")
    suspend fun clearAll()
}
