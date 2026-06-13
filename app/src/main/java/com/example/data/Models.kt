package com.example.data

import androidx.room.*

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String, // "FARMER", "BUYER", "ADMIN"
    val farmName: String = "",
    val companyName: String = "",
    val phone: String,
    val email: String = "",
    val province: String = "",
    val district: String = "",
    val village: String = "",
    val imageUrl: String = "",
    val isVerified: Boolean = false,
    val latitude: Double = 11.5564, // Default to Phnom Penh area
    val longitude: Double = 104.9282,
    val isPremium: Boolean = false,
    val balance: Double = 0.0
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String, // "kg", "ton", "units"
    val qualityGrade: String, // "Grade A", "Grade B", "Grade C"
    val pricePerUnit: Double,
    val description: String,
    val harvestDate: String,
    val province: String,
    val district: String,
    val latitude: Double = 11.5564,
    val longitude: Double = 104.9282,
    val status: String = "ACTIVE", // "ACTIVE", "SOLD"
    val isFeatured: Boolean = false,
    val userId: Long,
    val userName: String,
    val userPhone: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "buy_requests")
data class BuyRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val quantityNeeded: Double,
    val unit: String,
    val qualityRequirement: String,
    val offeredPricePerUnit: Double,
    val description: String,
    val province: String,
    val district: String,
    val latitude: Double = 11.5564,
    val longitude: Double = 104.9282,
    val status: String = "ACTIVE", // "ACTIVE", "CLOSED"
    val isFeatured: Boolean = false,
    val userId: Long,
    val userName: String,
    val userPhone: String = "",
    val deadline: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceId: Long, // Can be productId or buyRequestId
    val isProductOffer: Boolean = true, // True if bidding on a product, false if bidding on a buy request
    val senderId: Long,
    val senderName: String,
    val receiverId: Long,
    val receiverName: String,
    val title: String, // Crop name
    val quantity: Double,
    val price: Double,
    val deliveryMethod: String, // eg "Seller Deliver", "Buyer Pickup"
    val notes: String = "",
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED", "CANCELLED"
    val conversationStatus: String = "NEGOTIATING", // "INQUIRY", "NEGOTIATING", "AGREEMENT", "CLOSED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long,
    val receiverId: Long,
    val text: String,
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val offerAttachedId: Long? = null // If they sent an offer, store the offerId here
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "CHAT", "OFFER", "NEWS", "MATCH"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "market_news")
data class MarketNews(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "Rice", "Cassava", etc.
    val priceTrend: String, // "UP", "DOWN", "STABLE"
    val currentPriceRange: String, // e.g., "$0.25 - $0.30 / kg"
    val timestamp: Long = System.currentTimeMillis()
)
