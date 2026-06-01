package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

class RentalViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = RentalRepository(
        database.listingDao(),
        database.reviewDao(),
        database.messageDao(),
        database.rentalDao()
    )

    // User State / Wallet Balance
    private val _walletBalance = MutableStateFlow(550.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    // Offline / Connection Simulator Mode
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Geolocation Simulators
    data class SimulatedLocation(val name: String, val latitude: Double, val longitude: Double)
    val locations = listOf(
        SimulatedLocation("Union Square, SF (Downtown)", 37.7876, -122.4080),
        SimulatedLocation("Mission District, SF", 37.7599, -122.4376),
        SimulatedLocation("Golden Gate Park, SF", 37.7694, -122.4862)
    )
    private val _userLocation = MutableStateFlow(locations[0])
    val userLocation: StateFlow<SimulatedLocation> = _userLocation.asStateFlow()

    // Search and Filtering Parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _maxDistanceMiles = MutableStateFlow(5.0)
    val maxDistanceMiles: StateFlow<Double> = _maxDistanceMiles.asStateFlow()

    private val _sortBy = MutableStateFlow("Distance") // Distance, Price Low, Rating
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    // UI Feedback State
    private val _isTyping = MutableStateFlow<Int?>(null) // listingId being typing
    val isTyping: StateFlow<Int?> = _isTyping.asStateFlow()

    private val _checkoutSuccess = MutableStateFlow(false)
    val checkoutSuccess: StateFlow<Boolean> = _checkoutSuccess.asStateFlow()

    // Core Flows from Database
    val allListings: StateFlow<List<ListingEntity>> = repository.allListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedListings: StateFlow<List<ListingEntity>> = repository.savedListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatInbox: StateFlow<List<MessageEntity>> = repository.chatInbox
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRentals: StateFlow<List<RentalEntity>> = repository.allRentals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamically Filtered Listings based on Search, Category, Geolocation Distance, and Connection Status
    val uiListings: StateFlow<List<ListingEntity>> = combine(
        allListings,
        _searchQuery,
        _selectedCategory,
        _userLocation,
        _maxDistanceMiles,
        _sortBy,
        _isOnline
    ) { arrayFlows ->
        @Suppress("UNCHECKED_CAST")
        val listings = arrayFlows[0] as List<ListingEntity>
        val query = arrayFlows[1] as String
        val cat = arrayFlows[2] as String
        val loc = arrayFlows[3] as SimulatedLocation
        val maxDist = arrayFlows[4] as Double
        val sort = arrayFlows[5] as String
        val online = arrayFlows[6] as Boolean

        var list = listings

        // If offline, we can still browse saved listing details in offline state
        if (!online) {
            list = list.filter { it.isSaved }
        }

        // Apply Category
        if (cat != "All") {
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }

        // Apply Search query
        if (query.isNotEmpty()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        // Calculate Proximity and Filter by Distance
        val measuredListings = list.map { item ->
            val dist = calculateDistanceInMiles(loc.latitude, loc.longitude, item.latitude, item.longitude)
            Pair(item, dist)
        }

        val filteredByDistance = measuredListings.filter { it.second <= maxDist }

        // Sort Listings
        val sortedListings = when (sort) {
            "Price Low" -> filteredByDistance.sortedBy { it.first.pricePerDay }
            "Rating" -> filteredByDistance.sortedByDescending { it.first.ownerRating }
            else -> filteredByDistance.sortedBy { it.second } // Proximity
        }

        sortedListings.map { it.first }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fetch Details helper
    fun getListingDetails(id: Int): Flow<ListingEntity?> = repository.getListingById(id)
    fun getListingReviews(id: Int): Flow<List<ReviewEntity>> = repository.getReviewsForListing(id)
    fun getListingMessages(id: Int): Flow<List<MessageEntity>> = repository.getMessagesForListing(id)

    // User actions
    fun toggleConnectionMode() {
        _isOnline.value = !_isOnline.value
    }

    fun updateSimulatedLocation(loc: SimulatedLocation) {
        _userLocation.value = loc
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateMaxDistance(miles: Double) {
        _maxDistanceMiles.value = miles
    }

    fun updateSortPreference(pref: String) {
        _sortBy.value = pref
    }

    fun toggleSaveListing(id: Int, currentSaved: Boolean) {
        viewModelScope.launch {
            repository.updateListingSavedStatus(id, !currentSaved)
        }
    }

    fun addWalletFunds(amount: Double) {
        _walletBalance.value += amount
    }

    // Secure Payment System - Create Rental Transaction
    fun processRentalCheckout(
        item: ListingEntity,
        days: Int,
        cardName: String,
        cardNumber: String,
        cardLast4: String,
        onComplete: (Boolean, String, String?) -> Unit
    ) {
        val totalCost = (item.pricePerDay * days) + item.securityDeposit
        val txId = "TXN-${(100000..999999).random()}"

        if (_walletBalance.value < totalCost) {
            onComplete(false, "Insufficient wallet balance. Please top up funds.", null)
            return
        }

        viewModelScope.launch {
            _checkoutSuccess.value = false
            delay(1500) // Simulate secure bank transaction delay through payment gateway

            // Save Rental Transaction to Local DB
            val rental = RentalEntity(
                listingId = item.id,
                listingTitle = item.title,
                pricePerDay = item.pricePerDay,
                totalAmount = totalCost,
                securityDeposit = item.securityDeposit,
                status = "ACTIVE",
                rentalDays = days,
                paymentCardLast4 = cardLast4,
                transactionId = txId
            )
            repository.insertRental(rental)

            // Deduct from simulated balance
            _walletBalance.value -= totalCost

            // Save first auto-generated system checkout message in transaction chat channel
            val userMsg = MessageEntity(
                listingId = item.id,
                senderName = "LendShare Pay",
                messageText = "🛡️ Security Escrow: Paid \$${"%.2f".format(totalCost)} on Card (...$cardLast4). Security deposit of \$${"%.2f".format(item.securityDeposit)} held. Transaction Ref: $txId.",
                isFromUser = false
            )
            repository.insertMessage(userMsg)

            _checkoutSuccess.value = true
            onComplete(true, "Checkout approved safely. Enjoy your rental!", txId)
        }
    }

    // Release Rental Escrow and Refund Security Deposit
    fun endRentalAndRefund(rental: RentalEntity) {
        viewModelScope.launch {
            repository.updateRentalStatus(rental.id, "COMPLETED")
            _walletBalance.value += rental.securityDeposit

            // Create notification message
            val refundMsg = MessageEntity(
                listingId = rental.listingId,
                senderName = "LendShare Pay",
                messageText = "💸 Security Refund: Rental returned successfully! Refunded security deposit of \$${"%.2f".format(rental.securityDeposit)} back to wallet.",
                isFromUser = false
            )
            repository.insertMessage(refundMsg)
        }
    }

    // Real-time Chat Messaging Simulation with Intelligent Live Replies
    fun sendDirectMessage(listingId: Int, ownerName: String, text: String) {
        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            // 1. Save user's message
            val userMsg = MessageEntity(
                listingId = listingId,
                senderName = "You",
                messageText = text,
                isFromUser = true
            )
            repository.insertMessage(userMsg)

            // 2. Trigger intelligent automatic owner simulated reply
            _isTyping.value = listingId
            delay(1800) // Delay for high typing realism

            val autoResponse = when {
                text.contains("available", ignoreCase = true) || text.contains("when", ignoreCase = true) -> {
                    "Yes, it is fully available! When would you like to collect it? We can meet in public or I can deliver if nearby."
                }
                text.contains("discount", ignoreCase = true) || text.contains("cheaper", ignoreCase = true) -> {
                    "LendShare prices are already set to provide maximum value, but for bookings longer than a week, feel free to submit an offer!"
                }
                text.contains("place", ignoreCase = true) || text.contains("meet", ignoreCase = true) || text.contains("where", ignoreCase = true) -> {
                    "I am usually located around San Francisco. We can meet in a well-lit coffee shop or shopping zone safe point."
                }
                text.contains("condition", ignoreCase = true) || text.contains("work", ignoreCase = true) -> {
                    "It works perfectly. It is sanitized, clean, and checked after every single rental checkout. I will show you how it works on pickup!"
                }
                else -> {
                    "Thanks for details! That sounds perfectly fine. Let's arrange transit through LendShare Checkout to lock in the rental schedule."
                }
            }

            val ownerMsg = MessageEntity(
                listingId = listingId,
                senderName = ownerName,
                messageText = autoResponse,
                isFromUser = false
            )
            repository.insertMessage(ownerMsg)
            _isTyping.value = null
        }
    }

    // Create New Rental Listing (Give item for rent)
    fun createListing(
        title: String,
        description: String,
        category: String,
        pricePerDay: Double,
        securityDeposit: Double,
        locationName: String,
        spec: String = "gradient_custom",
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            // Random near-by coordinates so it instantly shows up in searches
            val userLat = _userLocation.value.latitude + (Math.random() - 0.5) * 0.05
            val userLng = _userLocation.value.longitude + (Math.random() - 0.5) * 0.05

            val newListing = ListingEntity(
                title = title,
                description = description,
                category = category,
                pricePerDay = pricePerDay,
                securityDeposit = securityDeposit,
                ownerName = "You (Owner)",
                ownerRating = 5.0f,
                latitude = userLat,
                longitude = userLng,
                locationName = "$locationName (0.4 mi)",
                isAvailable = true,
                isSaved = true, // Owner listings are always saved/offline accessible
                imageSpec = spec
            )
            val generatedId = repository.insertListing(newListing)

            // Write first automated reviewer post
            val initialReview = ReviewEntity(
                listingId = generatedId.toInt(),
                reviewerName = "System Auditor",
                rating = 5.0f,
                comment = "Verified brand new listing uploaded by property owner. Standard LendShare collateral requirements apply."
            )
            repository.insertReview(initialReview)

            onComplete(true)
        }
    }

    // Submit Custom Item Review to Build Trust
    fun submitItemReview(listingId: Int, rating: Float, comment: String, reviewerName: String = "You") {
        if (comment.trim().isEmpty()) return
        viewModelScope.launch {
            val newReview = ReviewEntity(
                listingId = listingId,
                reviewerName = reviewerName,
                rating = rating,
                comment = comment
            )
            repository.insertReview(newReview)
        }
    }

    // Distance Calculation (Haversine Formula) returning miles
    fun calculateDistanceInMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusMiles = 3958.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMiles * c
    }

    // Simulate successful rental completions dynamically
    fun simulateCompletedRental() {
        viewModelScope.launch {
            val randomListingId = (1..6).random()
            val randomListing = allListings.value.find { it.id == randomListingId }
            val title = randomListing?.title ?: "Yeti Tundra 45 Cooler"
            val price = randomListing?.pricePerDay ?: 12.0
            val deposit = randomListing?.securityDeposit ?: 40.0
            val customRental = RentalEntity(
                listingId = randomListingId,
                listingTitle = title,
                pricePerDay = price,
                totalAmount = price * 2,
                securityDeposit = deposit,
                status = "COMPLETED",
                rentalDays = 2,
                paymentCardLast4 = "5678",
                transactionId = "SIM-${(10000..99999).random()}"
            )
            repository.insertRental(customRental)
        }
    }

    // Remove simulated complete rentals (decrement count to test boundaries)
    fun removeOneCompletedRental() {
        viewModelScope.launch {
            val completedList = allRentals.value.filter { it.status == "COMPLETED" }
            if (completedList.isNotEmpty()) {
                val oldestRental = completedList.last()
                repository.updateRentalStatus(oldestRental.id, "CANCELLED")
            }
        }
    }
}

class RentalViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RentalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RentalViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
