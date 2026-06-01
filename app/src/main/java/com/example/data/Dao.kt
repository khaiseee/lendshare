package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY id DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    fun getListingById(id: Int): Flow<ListingEntity?>

    @Query("SELECT * FROM listings WHERE isSaved = 1 ORDER BY id DESC")
    fun getSavedListings(): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<ListingEntity>)

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Query("UPDATE listings SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedStatus(id: Int, isSaved: Boolean)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListingById(id: Int)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE listingId = :listingId ORDER BY timestamp DESC")
    fun getReviewsForListing(listingId: Int): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE listingId = :listingId ORDER BY timestamp ASC")
    fun getMessagesForListing(listingId: Int): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // Gets the latest message for every item we have a conversation about
    @Query("""
        SELECT m1.* FROM messages m1
        INNER JOIN (
            SELECT listingId, MAX(timestamp) as max_ts
            FROM messages
            GROUP BY listingId
        ) m2 ON m1.listingId = m2.listingId AND m1.timestamp = m2.max_ts
        ORDER BY m1.timestamp DESC
    """)
    fun getChatInbox(): Flow<List<MessageEntity>>
}

@Dao
interface RentalDao {
    @Query("SELECT * FROM rentals ORDER BY timestamp DESC")
    fun getAllRentals(): Flow<List<RentalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRental(rental: RentalEntity)

    @Query("UPDATE rentals SET status = :status WHERE id = :id")
    suspend fun updateRentalStatus(id: Int, status: String)
}
