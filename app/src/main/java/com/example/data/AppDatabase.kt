package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ListingEntity::class,
        ReviewEntity::class,
        MessageEntity::class,
        RentalEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
    abstract fun reviewDao(): ReviewDao
    abstract fun messageDao(): MessageDao
    abstract fun rentalDao(): RentalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lendshare_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val listingDao = database.listingDao()
            val reviewDao = database.reviewDao()
            val messageDao = database.messageDao()

            // Pre-populate Listings
            val initialListings = listOf(
                ListingEntity(
                    id = 1,
                    title = "DJI Mavic 3 Pro Drone Combo",
                    description = "Professional tri-camera drone system with 43-min flight time, Hasselblad main camera, omnidirectional obstacle sensing, and professional-grade stabilization. Comes with 3 batteries, hard carrying case, and RC Pro remote.",
                    category = "Electronics",
                    pricePerDay = 45.0,
                    securityDeposit = 150.0,
                    ownerName = "Michael Chang",
                    ownerRating = 4.9f,
                    latitude = 37.7749, // San Francisco
                    longitude = -122.4194,
                    locationName = "Mission District, SF (0.8 mi)",
                    isAvailable = true,
                    isSaved = true,
                    imageSpec = "gradient_mavic",
                    completedRentalsCount = 12
                ),
                ListingEntity(
                    id = 2,
                    title = "Yeti Tundra 45 Cooler",
                    description = "Heavy-duty outdoor rotomolded cooler. Perfect for weekend beach trips, camping, or backyard parties. Keeps ice completely frozen for up to 5 days, holds up to 26 cans. Fully sanitized between uses.",
                    category = "Outdoor",
                    pricePerDay = 12.0,
                    securityDeposit = 40.0,
                    ownerName = "Sarah Jenkins",
                    ownerRating = 4.8f,
                    latitude = 37.7858,
                    longitude = -122.4008,
                    locationName = "SoMa, SF (1.2 mi)",
                    isAvailable = true,
                    isSaved = false,
                    imageSpec = "gradient_yeti",
                    completedRentalsCount = 8
                ),
                ListingEntity(
                    id = 3,
                    title = "Sony Alpha 7 IV Mirrorless Camera",
                    description = "Hybrid camera with a 33MP sensor, 4K 60p recording, and exceptional real-time autofocussing. Includes a versatile Sony FE 24-70mm f/2.8 GM lens. Rental includes strap, battery charger, and 128GB high-speed memory card.",
                    category = "Electronics",
                    pricePerDay = 65.0,
                    securityDeposit = 250.0,
                    ownerName = "Elena Rostova",
                    ownerRating = 5.0f,
                    latitude = 37.8012,
                    longitude = -122.4586,
                    locationName = "Presidio Heights, SF (2.5 mi)",
                    isAvailable = true,
                    isSaved = true,
                    imageSpec = "gradient_sony",
                    completedRentalsCount = 18
                ),
                ListingEntity(
                    id = 4,
                    title = "Patagonia Black Hole 70L Duffel",
                    description = "Weather-resistant, ultra-durable duffel bag that converts into a comfortable backpack. Great for long-haul travel or camping. Includes lockable zippers and a reinforced bottom sheet.",
                    category = "Outdoor",
                    pricePerDay = 8.0,
                    securityDeposit = 20.0,
                    ownerName = "Marcus Brody",
                    ownerRating = 4.6f,
                    latitude = 37.7599,
                    longitude = -122.4376,
                    locationName = "Noe Valley, SF (1.9 mi)",
                    isAvailable = true,
                    isSaved = false,
                    imageSpec = "gradient_patagonia",
                    completedRentalsCount = 3
                ),
                ListingEntity(
                    id = 5,
                    title = "Segway Ninebot Max G30P",
                    description = "High-powered electric commuter kick scooter with up to 40 miles range and 18.6 mph top speed. Features self-healing 10-inch pneumatic tires. Helmet, charging lock, and safety instructions provided free.",
                    category = "Transportation",
                    pricePerDay = 25.0,
                    securityDeposit = 80.0,
                    ownerName = "Carlos Diaz",
                    ownerRating = 4.7f,
                    latitude = 37.7694,
                    longitude = -122.4862,
                    locationName = "Sunset District, SF (3.4 mi)",
                    isAvailable = true,
                    isSaved = false,
                    imageSpec = "gradient_segway",
                    completedRentalsCount = 10
                ),
                ListingEntity(
                    id = 6,
                    title = "Husqvarna 120 Mark II Chainsaw",
                    description = "Durable 16-inch gas-powered chainsaw. Easy to start, ideal for light yard maintenance, branch trimming, and simple firewood cutting. Comes with pre-mixed premium bar oil, gloves, and protective eyewear.",
                    category = "Tools",
                    pricePerDay = 30.0,
                    securityDeposit = 100.0,
                    ownerName = "David Miller",
                    ownerRating = 4.5f,
                    latitude = 37.7405, // South SF
                    longitude = -122.4132,
                    locationName = "Bernal Heights, SF (1.5 mi)",
                    isAvailable = true,
                    isSaved = false,
                    imageSpec = "gradient_chainsaw",
                    completedRentalsCount = 4
                )
            )
            listingDao.insertListings(initialListings)

            // Pre-populate Reviews
            val initialReviews = listOf(
                ReviewEntity(
                    listingId = 1,
                    reviewerName = "Emily Watson",
                    rating = 5.0f,
                    comment = "Michael was fantastic to work with. The drone was in flawless condition, fully charged, and ready for flight. Highly recommend renting from him!"
                ),
                ReviewEntity(
                    listingId = 1,
                    reviewerName = "Brandon Lee",
                    rating = 4.8f,
                    comment = "Excellent piece of equipment. Renting process went Super smoothly. Michael met me on time and gave a quick demo of the camera settings."
                ),
                ReviewEntity(
                    listingId = 2,
                    reviewerName = "Clara Evans",
                    rating = 5.0f,
                    comment = "Yeti kept our drinks ice cold during a scorching weekend. Clean and smelled great. Sarah is very nice and responsive!"
                ),
                ReviewEntity(
                    listingId = 3,
                    reviewerName = "Jordan K.",
                    rating = 5.0f,
                    comment = "Elena went above and beyond. The Sony lens was super clean. She took time to clean it before passing it. Outstanding experience!"
                )
            )
            initialReviews.forEach { reviewDao.insertReview(it) }

            // Pre-populate dynamic sample messages to represent actual real-time chat history
            val initialMessages = listOf(
                MessageEntity(
                    listingId = 1,
                    senderName = "Michael Chang",
                    messageText = "Hi there! Yes, the DJI Mavic 3 is fully operational and available this weekend. It includes 3 intelligent flight batteries.",
                    timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                    isFromUser = false
                ),
                MessageEntity(
                    listingId = 1,
                    senderName = "You",
                    messageText = "Awesome! That sounds perfect. Is the controller included?",
                    timestamp = System.currentTimeMillis() - 80000000,
                    isFromUser = true
                ),
                MessageEntity(
                    listingId = 1,
                    senderName = "Michael Chang",
                    messageText = "Yes, it comes with the premium DJI RC remote control with its own built-in high-brightness screen, so you don't even need to connect your phone!",
                    timestamp = System.currentTimeMillis() - 72000000,
                    isFromUser = false
                ),
                MessageEntity(
                    listingId = 2,
                    senderName = "Sarah Jenkins",
                    messageText = "Hi! Yes, the Yeti Tundra is available for pick up. Let me know what time works best for you.",
                    timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                    isFromUser = false
                )
            )
            initialMessages.forEach { messageDao.insertMessage(it) }

            // Pre-populate completed bookings to simulate active historical renting
            val rentalDao = database.rentalDao()
            val seedRentals = listOf(
                RentalEntity(listingId = 2, listingTitle = "Yeti Tundra 45 Cooler", pricePerDay = 12.0, totalAmount = 64.0, securityDeposit = 40.0, status = "COMPLETED", rentalDays = 2, paymentCardLast4 = "4321", transactionId = "TXN-712854"),
                RentalEntity(listingId = 4, listingTitle = "Patagonia Black Hole 70L Duffel", pricePerDay = 8.0, totalAmount = 44.0, securityDeposit = 20.0, status = "COMPLETED", rentalDays = 3, paymentCardLast4 = "4321", transactionId = "TXN-829140"),
                RentalEntity(listingId = 6, listingTitle = "Husqvarna 120 Mark II Chainsaw", pricePerDay = 30.0, totalAmount = 130.0, securityDeposit = 100.0, status = "COMPLETED", rentalDays = 1, paymentCardLast4 = "4321", transactionId = "TXN-651230"),
                RentalEntity(listingId = 2, listingTitle = "Yeti Tundra 45 Cooler", pricePerDay = 12.0, totalAmount = 76.0, securityDeposit = 40.0, status = "COMPLETED", rentalDays = 3, paymentCardLast4 = "4321", transactionId = "TXN-551042"),
                RentalEntity(listingId = 4, listingTitle = "Patagonia Black Hole 70L Duffel", pricePerDay = 8.0, totalAmount = 36.0, securityDeposit = 20.0, status = "COMPLETED", rentalDays = 2, paymentCardLast4 = "4321", transactionId = "TXN-104928"),
                RentalEntity(listingId = 2, listingTitle = "Yeti Tundra 45 Cooler", pricePerDay = 12.0, totalAmount = 52.0, securityDeposit = 40.0, status = "COMPLETED", rentalDays = 1, paymentCardLast4 = "4321", transactionId = "TXN-902481"),
                RentalEntity(listingId = 4, listingTitle = "Patagonia Black Hole 70L Duffel", pricePerDay = 8.0, totalAmount = 36.0, securityDeposit = 20.0, status = "COMPLETED", rentalDays = 2, paymentCardLast4 = "4321", transactionId = "TXN-382904"),
                RentalEntity(listingId = 6, listingTitle = "Husqvarna 120 Mark II Chainsaw", pricePerDay = 30.0, totalAmount = 160.0, securityDeposit = 100.0, status = "COMPLETED", rentalDays = 2, paymentCardLast4 = "4321", transactionId = "TXN-492019"),
                RentalEntity(listingId = 4, listingTitle = "Patagonia Black Hole 70L Duffel", pricePerDay = 8.0, totalAmount = 36.0, securityDeposit = 20.0, status = "COMPLETED", rentalDays = 2, paymentCardLast4 = "4321", transactionId = "TXN-112349")
            )
            seedRentals.forEach { rentalDao.insertRental(it) }
        }
    }
}
