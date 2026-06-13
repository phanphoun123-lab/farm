package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FarmRepository(private val db: AppDatabase) {

    val allUsers: Flow<List<UserProfile>> = db.userDao().getAllUsers()
    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()
    val allBuyRequests: Flow<List<BuyRequest>> = db.buyRequestDao().getAllBuyRequests()
    fun getOffersForUser(userId: Long): Flow<List<Offer>> = db.offerDao().getOffersForUser(userId)
    val allNotifications: Flow<List<Notification>> = db.notificationDao().getAllNotifications()
    val allMarketNews: Flow<List<MarketNews>> = db.marketNewsDao().getAllMarketNews()

    suspend fun getUser(id: Long): UserProfile? = db.userDao().getUserById(id)
    suspend fun getUserByPhone(phone: String): UserProfile? = db.userDao().getUserByPhone(phone)
    suspend fun saveUser(user: UserProfile): Long = db.userDao().insertUser(user)
    suspend fun updateUser(user: UserProfile) = db.userDao().updateUser(user)

    // Products
    fun getProductsByCategory(category: String): Flow<List<Product>> = db.productDao().getProductsByCategory(category)
    fun getProductsByUser(userId: Long): Flow<List<Product>> = db.productDao().getProductsByUser(userId)
    suspend fun getProduct(id: Long): Product? = db.productDao().getProductById(id)
    suspend fun insertProduct(product: Product): Long = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: Product) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(product: Product) = db.productDao().deleteProduct(product)
    fun getFeaturedProducts(): Flow<List<Product>> = db.productDao().getFeaturedProducts()

    // Buy Requests
    fun getBuyRequestsByCategory(category: String): Flow<List<BuyRequest>> = db.buyRequestDao().getBuyRequestsByCategory(category)
    fun getBuyRequestsByUser(userId: Long): Flow<List<BuyRequest>> = db.buyRequestDao().getBuyRequestsByUser(userId)
    suspend fun getBuyRequest(id: Long): BuyRequest? = db.buyRequestDao().getBuyRequestById(id)
    suspend fun insertBuyRequest(request: BuyRequest): Long = db.buyRequestDao().insertBuyRequest(request)
    suspend fun updateBuyRequest(request: BuyRequest) = db.buyRequestDao().updateBuyRequest(request)
    suspend fun deleteBuyRequest(request: BuyRequest) = db.buyRequestDao().deleteBuyRequest(request)
    fun getFeaturedBuyRequests(): Flow<List<BuyRequest>> = db.buyRequestDao().getFeaturedBuyRequests()

    // Offers & Negotiations
    suspend fun getOffer(id: Long): Offer? = db.offerDao().getOfferById(id)
    suspend fun saveOffer(offer: Offer): Long = db.offerDao().insertOffer(offer)
    suspend fun updateOffer(offer: Offer) = db.offerDao().updateOffer(offer)

    // Chats
    fun getChatHistory(userId: Long, otherId: Long): Flow<List<ChatMessage>> = db.chatMessageDao().getChatHistory(userId, otherId)
    fun getRecentMessages(userId: Long): Flow<List<ChatMessage>> = db.chatMessageDao().getRecentMessagesForUser(userId)
    suspend fun sendChatMessage(msg: ChatMessage): Long {
        val msgId = db.chatMessageDao().insertMessage(msg)
        // Also fire automatic intelligence mock reply to make the app interactive
        return msgId
    }
    suspend fun markMessagesAsRead(userId: Long, otherId: Long) = db.chatMessageDao().markAsRead(userId, otherId)

    // Notifications
    suspend fun addNotification(title: String, message: String, type: String) {
        db.notificationDao().insertNotification(Notification(title = title, message = message, type = type))
    }
    suspend fun markNotificationsRead() = db.notificationDao().markAllAsRead()
    suspend fun clearAllNotifications() = db.notificationDao().clearAll()

    // Prepopulate system database if empty
    suspend fun preseedDatabaseIfEmpty() {
        val existingUsers = db.userDao().getAllUsers().firstOrNull()
        if (existingUsers.isNullOrEmpty()) {
            // Add initial users (Farmers, Buyers)
            val farmer1Id = db.userDao().insertUser(
                UserProfile(
                    name = "Sokha Sophea",
                    role = "FARMER",
                    farmName = "Battambang Golden Paddy Farm",
                    phone = "+855 12 345 678",
                    email = "sokha.sophea@gmail.com",
                    province = "Battambang",
                    district = "Banan",
                    village = "Kantueu I",
                    isVerified = true,
                    latitude = 13.0183,
                    longitude = 103.1412,
                    balance = 120.0
                )
            )

            val farmer2Id = db.userDao().insertUser(
                UserProfile(
                    name = "Chan Piseth",
                    role = "FARMER",
                    farmName = "Kampot Pepper Organic Garden",
                    phone = "+855 98 765 432",
                    email = "piseth.chan@outlook.com",
                    province = "Kampot",
                    district = "Tuek Chhou",
                    village = "Snom Prampir",
                    isVerified = true,
                    latitude = 10.6558,
                    longitude = 104.1481,
                    balance = 45.0
                )
            )

            val buyer1Id = db.userDao().insertUser(
                UserProfile(
                    name = "Odom Serey",
                    role = "BUYER",
                    companyName = "Phnom Penh Agricultural Exporters Ltd",
                    phone = "+855 15 999 888",
                    email = "odom.s@pp-agri.com",
                    province = "Phnom Penh",
                    district = "Chamkar Mon",
                    isVerified = true,
                    longitude = 104.9282,
                    latitude = 11.5564,
                    isPremium = true,
                    balance = 1500.0
                )
            )

            val buyer2Id = db.userDao().insertUser(
                UserProfile(
                    name = "Vanna Roth",
                    role = "BUYER",
                    companyName = "Ankor Happy Supermarkets & Restaurants",
                    phone = "+855 77 111 222",
                    email = "vanna.roth@ankormarket.com",
                    province = "Siem Reap",
                    district = "Siem Reap",
                    isVerified = true,
                    latitude = 13.3671,
                    longitude = 103.8448,
                    isPremium = true,
                    balance = 800.0
                )
            )

            val adminId = db.userDao().insertUser(
                UserProfile(
                    name = "FarmJumnoy Support",
                    role = "ADMIN",
                    companyName = "Admin Control Panel",
                    phone = "+855 11 000 000",
                    email = "admin@farmjumnoy.com",
                    province = "Phnom Penh"
                )
            )

            // Insertion of sample products (market items)
            db.productDao().insertProduct(
                Product(
                    name = "Premium Jasmine Rice (Phka Rumduol)",
                    category = "Rice",
                    quantity = 15.0,
                    unit = "ton",
                    qualityGrade = "Grade A",
                    pricePerUnit = 850.0,
                    description = "Highly aromatic Phka Rumduol jasmine rice, award-winning specialty of Battambang. Moisture content is below 14%, complete pesticide-free organic certification.",
                    harvestDate = "15 May 2026",
                    province = "Battambang",
                    district = "Banan",
                    latitude = 13.0183,
                    longitude = 103.1412,
                    isFeatured = true,
                    userId = farmer1Id,
                    userName = "Sokha Sophea",
                    userPhone = "+855 12 345 678"
                )
            )

            db.productDao().insertProduct(
                Product(
                    name = "Red Organic Kampot Pepper",
                    category = "Pepper",
                    quantity = 400.0,
                    unit = "kg",
                    qualityGrade = "Grade A",
                    pricePerUnit = 15.0,
                    description = "Directly from Kampot GI production, sun-dried red grains, fruity and exceptionally spicy aroma. Excellent for high-end exports and restaurants.",
                    harvestDate = "28 April 2026",
                    province = "Kampot",
                    district = "Tuek Chhou",
                    latitude = 10.6558,
                    longitude = 104.1481,
                    isFeatured = true,
                    userId = farmer2Id,
                    userName = "Chan Piseth",
                    userPhone = "+855 98 765 432"
                )
            )

            db.productDao().insertProduct(
                Product(
                    name = "Sweet Organic Cassava Tubers",
                    category = "Cassava",
                    quantity = 25.0,
                    unit = "ton",
                    qualityGrade = "Grade B",
                    pricePerUnit = 90.0,
                    description = "Freshly harvested starch-rich cassava tubers. Ideal for processing into animal feed or premium tapioca flour. Immediate delivery available.",
                    harvestDate = "11 Jun 2026",
                    province = "Tboung Khmum",
                    district = "Memot",
                    latitude = 11.9567,
                    longitude = 106.1235,
                    userId = farmer2Id,
                    userName = "Chan Piseth",
                    userPhone = "+855 98 765 432"
                )
            )

            db.productDao().insertProduct(
                Product(
                    name = "Fresh Siem Reap Cauliflower",
                    category = "Vegetables",
                    quantity = 1200.0,
                    unit = "kg",
                    qualityGrade = "Grade A",
                    pricePerUnit = 1.10,
                    description = "Freshly picked Cauliflower from greenhouse farming in Siem Reap. Crunchy texture, gorgeous white colors. Minimum order 100 kilograms.",
                    harvestDate = "12 Jun 2026",
                    province = "Siem Reap",
                    district = "Siem Reap",
                    latitude = 13.3671,
                    longitude = 103.8448,
                    userId = farmer1Id,
                    userName = "Sokha Sophea",
                    userPhone = "+855 12 345 678"
                )
            )

            // Insertion of buyer purchase requests
            db.buyRequestDao().insertBuyRequest(
                BuyRequest(
                    name = "Seeking Organic Rice Millers",
                    category = "Rice",
                    quantityNeeded = 50.0,
                    unit = "ton",
                    qualityRequirement = "Grade A or B, Moisture <14%",
                    offeredPricePerUnit = 820.0,
                    description = "Urgent need of 50 Tons of polished Phka Rumduol or Jasmine rice for primary export shipment to Europe. We pay cash on delivery at our warehouse.",
                    province = "Phnom Penh",
                    district = "Chamkar Mon",
                    latitude = 11.5564,
                    longitude = 104.9282,
                    isFeatured = true,
                    userId = buyer1Id,
                    userName = "Odom Serey",
                    userPhone = "+855 15 999 888",
                    deadline = "30 Jun 2026"
                )
            )

            db.buyRequestDao().insertBuyRequest(
                BuyRequest(
                    name = "Sourcing Fresh Vegetables for Supermarket",
                    category = "Vegetables",
                    quantityNeeded = 2.0,
                    unit = "ton",
                    qualityRequirement = "Grade A, Organic Certified",
                    offeredPricePerUnit = 1.25,
                    description = "Looking for farming cooperatives who can supply regular weekly quantities of cabbage, cucumbers, lettuce, and tomatoes. Long-term contract provided.",
                    province = "Siem Reap",
                    district = "Siem Reap",
                    latitude = 13.3671,
                    longitude = 103.8448,
                    isFeatured = true,
                    userId = buyer2Id,
                    userName = "Vanna Roth",
                    userPhone = "+855 77 111 222",
                    deadline = "25 Jul 2026"
                )
            )

            // Insert matching news updates
            db.marketNewsDao().insertMarketNews(
                MarketNews(
                    title = "Battambang Jasmine Rice Price Booms",
                    description = "High international export demands have boosted Phka Rumduol pricing. Average wholesale price rose by 12% this week.",
                    category = "Rice",
                    priceTrend = "UP",
                    currentPriceRange = "$800 - $860 / Ton"
                )
            )

            db.marketNewsDao().insertMarketNews(
                MarketNews(
                    title = "Cassava Starch Processing Slowdown",
                    description = "Fuel price hikes affect heavy transportation costs, resulting in minor wholesale price decline in Eastern Border provinces.",
                    category = "Cassava",
                    priceTrend = "DOWN",
                    currentPriceRange = "$85 - $95 / Ton"
                )
            )

            db.marketNewsDao().insertMarketNews(
                MarketNews(
                    title = "Kampot Organic Pepper remains stable",
                    description = "The geographical designation maintains premium price levels. Export interest in EU, US, and Japan continues at high volume.",
                    category = "Pepper",
                    priceTrend = "STABLE",
                    currentPriceRange = "$15.00 - $17.00 / kg"
                )
            )

            // Pre-add an offer and conversational history
            val sampleOfferId = db.offerDao().insertOffer(
                Offer(
                    referenceId = 1L, // On premium Jasmine Rice
                    isProductOffer = true,
                    senderId = buyer1Id,
                    senderName = "Odom Serey",
                    receiverId = farmer1Id,
                    receiverName = "Sokha Sophea",
                    title = "Premium Jasmine Rice",
                    quantity = 15.0,
                    price = 830.0,
                    deliveryMethod = "Buyer Pickup (Banan Farm)",
                    notes = "We will send our own transport trucks if you agree at $830/ton.",
                    status = "PENDING",
                    conversationStatus = "NEGOTIATING"
                )
            )

            // Conversation messages
            db.chatMessageDao().insertMessage(
                ChatMessage(
                    senderId = buyer1Id,
                    receiverId = farmer1Id,
                    text = "Hello Sokha, I saw your Battambang Jasmine Rice listing! It looks high quality.",
                    timestamp = System.currentTimeMillis() - 7200000
                )
            )

            db.chatMessageDao().insertMessage(
                ChatMessage(
                    senderId = farmer1Id,
                    receiverId = buyer1Id,
                    text = "Susdey Odom! Yes, we have 15 Tons completely organic, dried and stored under strict temperature controls.",
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )

            db.chatMessageDao().insertMessage(
                ChatMessage(
                    senderId = buyer1Id,
                    receiverId = farmer1Id,
                    text = "Awesome, I would love to make an offer. I've sent you a bidding offer for your review.",
                    timestamp = System.currentTimeMillis() - 600000,
                    offerAttachedId = sampleOfferId
                )
            )

            // Notifications
            db.notificationDao().insertNotification(
                Notification(
                    title = "New Negotiating Offer!",
                    message = "Odom Serey sent an offer of $830/ton for 15 tons of Jasmine Rice.",
                    type = "OFFER"
                )
            )

            db.notificationDao().insertNotification(
                Notification(
                    title = "Match Found: seeking Jasmine Rice!",
                    message = "Buyer Odom Serey just posted a request seeking 50 tons of Jasmine Rice.",
                    type = "MATCH"
                )
            )
        }
    }
}
