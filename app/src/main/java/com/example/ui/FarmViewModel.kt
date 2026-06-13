package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FarmRepository(database)

    // Current active user state
    var currentUserId by mutableStateOf(1L) // Default to Farmer 1
        private set

    var currentUser by mutableStateOf<UserProfile?>(null)
        private set

    var currentScreen by mutableStateOf("home") // "home", "product_details", "request_details", "chat_screen", "add_product", "add_request", "all_notifications"
        private set

    // Detail targets
    var selectedProduct by mutableStateOf<Product?>(null)
    var selectedBuyRequest by mutableStateOf<BuyRequest?>(null)
    var activeChatPartner by mutableStateOf<UserProfile?>(null)

    // Language Toggle
    var isKhmer by mutableStateOf(false)
        private set

    // Selected Category filter
    var selectedCategory by mutableStateOf("All")

    // UI state streams sourced reactively from Room
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buyRequests: StateFlow<List<BuyRequest>> = repository.allBuyRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketNews: StateFlow<List<MarketNews>> = repository.allMarketNews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConversations: StateFlow<List<ChatMessage>> = flow {
        // Collect all messages and group them to present conversation cells in chat history list
        repository.getRecentMessages(currentUserId).collect { list ->
            emit(list)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat history
    private val _activeChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeChatMessages: StateFlow<List<ChatMessage>> = _activeChatMessages.asStateFlow()

    // Gemini API states
    var aiLoading by mutableStateOf(false)
    var aiAdvisoryReport by mutableStateOf("")
    var aiTacticsReport by mutableStateOf("")
    var aiPolishResult by mutableStateOf("")

    init {
        viewModelScope.launch {
            // First launch preseed
            repository.preseedDatabaseIfEmpty()
            // Set current active user
            loadCurrentUser(currentUserId)
        }
    }

    fun toggleLanguage() {
        isKhmer = !isKhmer
    }

    fun navigateTo(screen: String) {
        currentScreen = screen
    }

    suspend fun loadCurrentUser(userId: Long) {
        currentUserId = userId
        val profile = repository.getUser(userId)
        currentUser = profile
        // Observe chat history if a recipient is selected
        activeChatPartner?.let { observeChatHistory(it.id) }
    }

    suspend fun getUser(id: Long): UserProfile? = repository.getUser(id)
    suspend fun getOffer(id: Long): Offer? = repository.getOffer(id)

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun switchActiveUser(userId: Long) {
        viewModelScope.launch {
            loadCurrentUser(userId)
            repository.addNotification(
                title = "Switched Profile Setup",
                message = "You are now logged in as ${currentUser?.name} (${currentUser?.role})",
                type = "NEWS"
            )
        }
    }

    // Products Management
    fun createProduct(name: String, category: String, qty: Double, unit: String, grade: String, price: Double, description: String, date: String, province: String, district: String) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            val prod = Product(
                name = name,
                category = category,
                quantity = qty,
                unit = unit,
                qualityGrade = grade,
                pricePerUnit = price,
                description = description,
                harvestDate = date,
                province = province,
                district = district,
                userId = user.id,
                userName = user.name,
                userPhone = user.phone,
                longitude = 104.9 + (Math.random() - 0.5) * 0.1,
                latitude = 11.5 + (Math.random() - 0.5) * 0.1
            )
            repository.insertProduct(prod)
            repository.addNotification(
                title = "Product Listed Successfully",
                message = "Your product $name is now live in Cambodia agricultural marketplace.",
                type = "MATCH"
            )
            currentScreen = "home"
        }
    }

    fun markProductSold(product: Product) {
        viewModelScope.launch {
            val updated = product.copy(status = "SOLD")
            repository.updateProduct(updated)
            repository.addNotification(
                title = "Crop Listing Marked Sold",
                message = "Great job! ${product.name} listed items successfully updated to sold.",
                type = "NEWS"
            )
            if (selectedProduct?.id == product.id) {
                selectedProduct = updated
            }
        }
    }

    fun promoteProduct(product: Product) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            if (user.balance >= 10.0 || user.isPremium) {
                val updated = product.copy(isFeatured = true)
                repository.updateProduct(updated)
                if (!user.isPremium) {
                    repository.updateUser(user.copy(balance = user.balance - 10.0))
                    loadCurrentUser(user.id)
                }
                repository.addNotification(
                    title = "Listing Premium Promoted",
                    message = "${product.name} is now highlighted on the main home featured feed!",
                    type = "NEWS"
                )
                if (selectedProduct?.id == product.id) {
                    selectedProduct = updated
                }
            } else {
                repository.addNotification(
                    title = "Insufficient Funds",
                    message = "Listing promotion costs $10.00. Please top up your profile wallet balance.",
                    type = "NEWS"
                )
            }
        }
    }

    // Buy Requests Management
    fun createBuyRequest(name: String, category: String, qty: Double, unit: String, qualityReq: String, priceOffer: Double, description: String, province: String, district: String, deadline: String) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            val req = BuyRequest(
                name = name,
                category = category,
                quantityNeeded = qty,
                unit = unit,
                qualityRequirement = qualityReq,
                offeredPricePerUnit = priceOffer,
                description = description,
                province = province,
                district = district,
                userId = user.id,
                userName = user.name,
                userPhone = user.phone,
                deadline = deadline,
                longitude = 104.9 + (Math.random() - 0.5) * 0.1,
                latitude = 11.5 + (Math.random() - 0.5) * 0.1
            )
            repository.insertBuyRequest(req)
            repository.addNotification(
                title = "Buy Request Posted",
                message = "Your request for $qty $unit of $name has been broadcasted to all farmers.",
                type = "MATCH"
            )
            currentScreen = "home"
        }
    }

    fun closeBuyRequest(request: BuyRequest) {
        viewModelScope.launch {
            val updated = request.copy(status = "CLOSED")
            repository.updateBuyRequest(updated)
            if (selectedBuyRequest?.id == request.id) {
                selectedBuyRequest = updated
            }
        }
    }

    // Messaging operations
    fun startChatWith(partnerId: Long) {
        viewModelScope.launch {
            val partner = repository.getUser(partnerId)
            if (partner != null) {
                activeChatPartner = partner
                currentScreen = "chat_screen"
                repository.markMessagesAsRead(currentUserId, partner.id)
                observeChatHistory(partner.id)
            }
        }
    }

    private fun observeChatHistory(partnerId: Long) {
        viewModelScope.launch {
            repository.getChatHistory(currentUserId, partnerId).collect { messages ->
                _activeChatMessages.value = messages
            }
        }
    }

    fun sendTextMessage(text: String) {
        val partner = activeChatPartner ?: return
        viewModelScope.launch {
            val msg = ChatMessage(
                senderId = currentUserId,
                receiverId = partner.id,
                text = text
            )
            repository.sendChatMessage(msg)
            
            // Build simple realistic simulation response to simulate chat interaction
            simulatePartnerReply(partner, text)
        }
    }

    private fun simulatePartnerReply(partner: UserProfile, lastMessage: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1800) // Realistic typing delays
            val replyText = when {
                lastMessage.contains("offer", ignoreCase = true) || lastMessage.contains("bid", ignoreCase = true) -> {
                    "Thanks for details! Can you accept delivery at my province warehouse or do you need me to pick it up?"
                }
                lastMessage.contains("price", ignoreCase = true) || lastMessage.contains("discount", ignoreCase = true) -> {
                    "Yes, I can decrease it by 3-5% if you take the entire quantity immediately."
                }
                lastMessage.contains("hello", ignoreCase = true) || lastMessage.contains("susdey", ignoreCase = true) -> {
                    "Susdey! Thanks for contacting me. Yes, these listed products are high-quality, currently fully available."
                }
                else -> {
                    "Understood. Let me discuss this with my farm cooperative and send you a counter-offer in the app!"
                }
            }
            val replyMsg = ChatMessage(
                senderId = partner.id,
                receiverId = currentUserId,
                text = replyText
            )
            repository.saveUser(partner) // keep updated
            database.chatMessageDao().insertMessage(replyMsg)
            repository.addNotification(
                title = "New Messenger Chat",
                message = "${partner.name}: $replyText",
                type = "CHAT"
            )
        }
    }

    // Offers and Negotiations
    fun sendNegotiationOffer(refId: Long, isProduct: Boolean, partnerId: Long, partnerName: String, cropTitle: String, qty: Double, price: Double, delivery: String, notes: String) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            val offer = Offer(
                referenceId = refId,
                isProductOffer = isProduct,
                senderId = currentUserId,
                senderName = user.name,
                receiverId = partnerId,
                receiverName = partnerName,
                title = cropTitle,
                quantity = qty,
                price = price,
                deliveryMethod = delivery,
                notes = notes,
                status = "PENDING",
                conversationStatus = "NEGOTIATING"
            )
            val offerId = repository.saveOffer(offer)

            // Attach offer as a specialized message object in the direct chat
            val chatMsg = ChatMessage(
                senderId = currentUserId,
                receiverId = partnerId,
                text = "📊 Sent a negotiation proposal for $cropTitle: $qty units at $$price / unit.",
                offerAttachedId = offerId
            )
            database.chatMessageDao().insertMessage(chatMsg)

            repository.addNotification(
                title = "New Negotiation Sent",
                message = "Proposed $$price/unit for $qty units of $cropTitle.",
                type = "OFFER"
            )
        }
    }

    fun respondToOffer(offer: Offer, isAccept: Boolean) {
        viewModelScope.launch {
            val finalStatus = if (isAccept) "ACCEPTED" else "REJECTED"
            val updated = offer.copy(status = finalStatus, conversationStatus = if (isAccept) "AGREEMENT" else "CLOSED")
            repository.updateOffer(updated)

            val replyText = if (isAccept) {
                "✅ Deal Agreed! I have accepted your offer proposal for ${offer.title} ($${offer.price}/unit)."
            } else {
                "❌ Offer Declined: Your proposal for ${offer.title} was rejected. Let's renegotiate."
            }

            val chatMsg = ChatMessage(
                senderId = currentUserId,
                receiverId = if (currentUserId == offer.senderId) offer.receiverId else offer.senderId,
                text = replyText
            )
            database.chatMessageDao().insertMessage(chatMsg)

            repository.addNotification(
                title = "Negotiate Update",
                message = "Offer for ${offer.title} has been $finalStatus.",
                type = "OFFER"
            )
        }
    }

    // Wallet balance
    fun topUpBalance(amount: Double) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            val updated = user.copy(balance = user.balance + amount)
            repository.updateUser(updated)
            loadCurrentUser(user.id)
            repository.addNotification(
                title = "Wallet Deposited",
                message = "Successfully topped up $$amount to your wallet balance.",
                type = "NEWS"
            )
        }
    }

    // AI Features utilizing direct Gemini Service
    fun fetchAISellerTranslatePolish(product: Product) {
        viewModelScope.launch {
            aiLoading = true
            aiPolishResult = ""
            try {
                val response = GeminiService.translateAndPolishListing(
                    title = product.name,
                    description = product.description,
                    category = product.category,
                    isKhmer = isKhmer
                )
                aiPolishResult = response
            } catch (e: Exception) {
                aiPolishResult = "Error optimizing: ${e.message}"
            } finally {
                aiLoading = false
            }
        }
    }

    fun fetchAIMarketIntelligenceReport(category: String, currentPrice: String, province: String) {
        viewModelScope.launch {
            aiLoading = true
            aiAdvisoryReport = ""
            try {
                val response = GeminiService.generateMarketIntelligenceReport(category, currentPrice, province)
                aiAdvisoryReport = response
            } catch (e: Exception) {
                aiAdvisoryReport = "Error downloading advisor report: ${e.message}"
            } finally {
                aiLoading = false
            }
        }
    }

    fun fetchAIBargainingTactics(offer: Offer, product: Product) {
        viewModelScope.launch {
            aiLoading = true
            aiTacticsReport = ""
            try {
                val response = GeminiService.suggestBargainTactics(
                    cropTitle = offer.title,
                    listedPrice = product.pricePerUnit,
                    quantity = offer.quantity,
                    unit = product.unit,
                    bidPrice = offer.price,
                    bidDelivery = offer.deliveryMethod,
                    notes = offer.notes
                )
                aiTacticsReport = response
            } catch (e: Exception) {
                aiTacticsReport = "Error analyzing bargain tactics: ${e.message}"
            } finally {
                aiLoading = false
            }
        }
    }

    // Utility list calculations for smart categories match making
    fun getSmartRecommendedProductsMatch(): List<Product> {
        val user = currentUser ?: return emptyList()
        val allProds = products.value
        if (user.role == "BUYER") {
            // Find products matching categories user is active in, or featured
            return allProds.filter { it.userId != user.id }.sortedWith(compareBy({ !it.isFeatured }, { it.timestamp }))
        }
        // If farmer, show featured or items similar
        return allProds.filter { it.userId != user.id }
    }

    fun getSmartRecommendedRequestsMatch(): List<BuyRequest> {
        val user = currentUser ?: return emptyList()
        val allReqs = buyRequests.value
        if (user.role == "FARMER") {
            // Show requests near farmer
            return allReqs.filter { it.userId != user.id }.sortedWith(compareBy({ !it.isFeatured }, { it.timestamp }))
        }
        return allReqs.filter { it.userId != user.id }
    }
}
