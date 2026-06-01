package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val pricePerDay: Double,
    val securityDeposit: Double,
    val ownerName: String,
    val ownerRating: Float,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val isAvailable: Boolean = true,
    val isSaved: Boolean = false, // Local bookmark for offline access
    val imageSpec: String = "gradient_teal_blue", // Custom aesthetic vector identifier
    val completedRentalsCount: Int = 0
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val reviewerName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val senderName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromUser: Boolean // true if current user sent it, false if owner replied
)

@Entity(tableName = "rentals")
data class RentalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val listingTitle: String,
    val pricePerDay: Double,
    val totalAmount: Double,
    val securityDeposit: Double,
    val status: String, // PENDING_DEPOSIT, ACTIVE, COMPLETED
    val rentalDays: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentCardLast4: String,
    val transactionId: String
)
