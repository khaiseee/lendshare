package com.example.data

import kotlinx.coroutines.flow.Flow

class RentalRepository(
    private val listingDao: ListingDao,
    private val reviewDao: ReviewDao,
    private val messageDao: MessageDao,
    private val rentalDao: RentalDao
) {
    val allListings: Flow<List<ListingEntity>> = listingDao.getAllListings()
    val savedListings: Flow<List<ListingEntity>> = listingDao.getSavedListings()
    val chatInbox: Flow<List<MessageEntity>> = messageDao.getChatInbox()
    val allRentals: Flow<List<RentalEntity>> = rentalDao.getAllRentals()

    fun getListingById(id: Int): Flow<ListingEntity?> {
        return listingDao.getListingById(id)
    }

    fun getReviewsForListing(listingId: Int): Flow<List<ReviewEntity>> {
        return reviewDao.getReviewsForListing(listingId)
    }

    fun getMessagesForListing(listingId: Int): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForListing(listingId)
    }

    suspend fun insertListing(listing: ListingEntity): Long {
        return listingDao.insertListing(listing)
    }

    suspend fun updateListingSavedStatus(id: Int, isSaved: Boolean) {
        listingDao.updateSavedStatus(id, isSaved)
    }

    suspend fun insertReview(review: ReviewEntity) {
        reviewDao.insertReview(review)
    }

    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun insertRental(rental: RentalEntity) {
        rentalDao.insertRental(rental)
    }

    suspend fun updateRentalStatus(id: Int, status: String) {
        rentalDao.updateRentalStatus(id, status)
    }
}
