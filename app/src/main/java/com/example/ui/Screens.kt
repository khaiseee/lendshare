package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.*
import kotlinx.coroutines.launch

class BookmarkRibbonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width / 2f, size.height - (size.width / 3.5f))
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun CompletedRentalsBookmark(
    completedCount: Int,
    modifier: Modifier = Modifier,
    isSmall: Boolean = false
) {
    val width = if (isSmall) 16.dp else 34.dp
    val height = if (isSmall) 26.dp else 52.dp
    val pb = if (isSmall) 2.dp else 6.dp
    val iconSize = if (isSmall) 7.dp else 13.dp
    val fontSize = if (isSmall) 6.sp else 11.sp

    val (tierName, color) = remember(completedCount) {
        when {
            completedCount < 5 -> Pair("Bronze", Color(0xFFCD7F32))       // Bronze
            completedCount in 5..9 -> Pair("Silver", Color(0xFF90A4AE))    // Silver
            completedCount in 10..14 -> Pair("Gold", Color(0xFFFFB300))     // Gold
            else -> Pair("Platinum", Color(0xFF00AA8D))                     // Platinum
        }
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(BookmarkRibbonShape())
            .background(color)
            .padding(bottom = pb),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 1.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "$tierName Status",
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
            if (!isSmall) {
                Spacer(modifier = Modifier.height(1.dp))
            }
            Text(
                text = completedCount.toString(),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LendShareAppContent(viewModel: RentalViewModel) {
    val isOnline by viewModel.isOnline.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    var activeTab by remember { mutableStateOf("Explore") }
    var selectedItemForDetail by remember { mutableStateOf<ListingEntity?>(null) }
    var activeChatIdByListing by remember { mutableStateOf<ListingEntity?>(null) }
    var isProfileOpen by remember { mutableStateOf(false) }
    var activeReceiptForPrinting by remember { mutableStateOf<RentalEntity?>(null) }

    Scaffold(
        topBar = {
            Column {
                // Top Custom Header Accent matching Professional Polish theme
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // "Lending to" Location layout
                        Column {
                            Text(
                                "LENDING TO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    // Cycles through locations on click as an app highlight!
                                    val currentIndex = viewModel.locations.indexOf(userLocation)
                                    val nextIndex = (currentIndex + 1) % viewModel.locations.size
                                    viewModel.updateSimulatedLocation(viewModel.locations[nextIndex])
                                }
                            ) {
                                Text(
                                    userLocation.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Change location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Right section: Online Toggle & User Profile Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Online/Offline toggle
                            IconButton(
                                onClick = { viewModel.toggleConnectionMode() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("toggle_online_status")
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = "Toggle Connection State",
                                    tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Professional Polish profile avatar circle
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .testTag("profile_avatar_button")
                                    .clickable {
                                        isProfileOpen = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile icon",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Connectivity Banner
                AnimatedVisibility(
                    visible = !isOnline,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline Action",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Offline Sync Enabled: Showing locally saved listing details",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Main navigation bottom bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val tabs = listOf(
                    Triple("Explore", Icons.Default.Explore, Icons.Outlined.Explore),
                    Triple("My Renting", Icons.Default.Handshake, Icons.Outlined.Handshake),
                    Triple("Inbox", Icons.Default.Forum, Icons.Outlined.Forum),
                    Triple("Wallet", Icons.Default.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
                )

                tabs.forEach { (name, filledIcon, outlinedIcon) ->
                    val selected = activeTab == name
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            activeTab = name
                            selectedItemForDetail = null
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) filledIcon else outlinedIcon,
                                contentDescription = name
                            )
                        },
                        label = { Text(name) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("tab_${name.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Active Tab UI routing
            when (activeTab) {
                "Explore" -> {
                    ExploreScreen(
                        viewModel = viewModel,
                        isOnline = isOnline,
                        onSelectItem = { selectedItemForDetail = it }
                    )
                }
                "My Renting" -> {
                    MyRentingScreen(
                        viewModel = viewModel,
                        isOnline = isOnline,
                        onSelectRentalItem = { item ->
                            viewModel.allListings.value.find { it.id == item.listingId }?.let {
                                selectedItemForDetail = it
                            }
                        },
                        onPrintReceipt = { rental -> activeReceiptForPrinting = rental }
                    )
                }
                "Inbox" -> {
                    InboxScreen(
                        viewModel = viewModel,
                        onOpenChat = { listing -> activeChatIdByListing = listing }
                    )
                }
                "Wallet" -> {
                    WalletScreen(
                        viewModel = viewModel,
                        onPrintReceipt = { rental -> activeReceiptForPrinting = rental }
                    )
                }
            }

            // Expanded Listing Detail Overlay Screen Layer
            selectedItemForDetail?.let { item ->
                ListingDetailOverlayScreen(
                    item = item,
                    viewModel = viewModel,
                    isOnline = isOnline,
                    onBack = { selectedItemForDetail = null },
                    onOpenChat = {
                        activeChatIdByListing = item
                        selectedItemForDetail = null
                    },
                    onBookingApproved = { rental ->
                        activeReceiptForPrinting = rental
                        selectedItemForDetail = null
                    }
                )
            }

            // Floating Direct Chat Window Layer
            activeChatIdByListing?.let { listing ->
                ChatThreadOverlayScreen(
                    listing = listing,
                    viewModel = viewModel,
                    onBack = { activeChatIdByListing = null }
                )
            }

            // Profile & Wishlist Overlay layer
            if (isProfileOpen) {
                ProfileOverlayScreen(
                    viewModel = viewModel,
                    isOnline = isOnline,
                    onClose = { isProfileOpen = false },
                    onSelectItem = { item ->
                        selectedItemForDetail = item
                        isProfileOpen = false
                    }
                )
            }

            // Printable Receipt Dialog Layer
            activeReceiptForPrinting?.let { rental ->
                PrintableReceiptDialog(
                    rental = rental,
                    onDismiss = { activeReceiptForPrinting = null }
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// tab 1: EXPLORE MARKETPLACE SCREEN
// --------------------------------------------------------------------------
@Composable
fun ExploreScreen(
    viewModel: RentalViewModel,
    isOnline: Boolean,
    onSelectItem: (ListingEntity) -> Unit
) {
    val listings by viewModel.uiListings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val maxDistanceMiles by viewModel.maxDistanceMiles.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()

    val categories = listOf("All", "Electronics", "Outdoor", "Tools", "Transportation")

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Geo / Filter Controls Header Card
        Card(
            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // GeoLocation simulated pinpoint selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Simulated GPS Location Pin",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Near: ${userLocation.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // simulated click picker to switch location
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        viewModel.locations.forEachIndexed { idx, loc ->
                            val active = loc == userLocation
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) MaterialTheme.colorScheme.secondary else Color.Black.copy(alpha = 0.2f))
                                    .clickable { viewModel.updateSimulatedLocation(loc) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Zone ${idx + 1}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search tents, tools, or bikes...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("search_input"),
                    shape = CircleShape,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Distance selection slider (Geolocation range)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Radius: ${"%.1f".format(maxDistanceMiles)} mi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.width(84.dp)
                    )
                    Slider(
                        value = maxDistanceMiles.toFloat(),
                        onValueChange = { viewModel.updateMaxDistance(it.toDouble()) },
                        valueRange = 0.5f..10f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    )
                }

                // Horizontal Category Selection Container
                ScrollableRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val active = cat == selectedCategory
                        FilterChip(
                            selected = active,
                            onClick = { viewModel.updateCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = active,
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        // Sorting preference bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Found ${listings.size} listings nearby",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sort by: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                listOf("Distance", "Price Low", "Rating").forEach { sort ->
                    val active = sort == sortBy
                    Text(
                        text = sort,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier
                            .clickable { viewModel.updateSortPreference(sort) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Grid of items
        if (listings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.SearchOff else Icons.Default.OfflineShare,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isOnline) "No matched nearby items found" else "Offline: No locally saved listings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Text(
                        text = if (isOnline) "Try broadening the radius slider or searching other terms" else "Bookmark items as 'Saved' while online to read them offline",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(listings) { item ->
                    val dist = viewModel.calculateDistanceInMiles(
                        userLocation.latitude,
                        userLocation.longitude,
                        item.latitude,
                        item.longitude
                    )
                    ListingCardItem(
                        item = item,
                        calculatedDistance = dist,
                        onClick = { onSelectItem(item) },
                        onToggleSave = { viewModel.toggleSaveListing(item.id, item.isSaved) }
                    )
                }
            }
        }
    }
}

@Composable
fun ListingCardItem(
    item: ListingEntity,
    calculatedDistance: Double,
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("listing_card_${item.id}"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                // Colored item Portrait canvas drawing
                DrawerListingIllustration(
                    spec = item.imageSpec,
                    modifier = Modifier.fillMaxSize()
                )

                // Renter Trust Ribbon Bookmark (hanging from top-left)
                CompletedRentalsBookmark(
                    completedCount = item.completedRentalsCount,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp)
                )

                // Category tag floating clean above pricing bar on bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 28.dp, end = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        item.category,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bookmark Icon to save listing details offline (Wishlist bookmark toggler)
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .testTag("bookmark_${item.id}"),
                ) {
                    Icon(
                        imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save listing for offline details",
                        tint = if (item.isSaved) MaterialTheme.colorScheme.primary else Color.White
                    )
                }

                // Pricing Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "\$${item.pricePerDay.toInt()}/day",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                    Text(
                        "Dep: \$${item.securityDeposit.toInt()}",
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // GPS Proximity Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${"%.1f".format(calculatedDistance)} mi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Owner Stars rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "%.1f".format(item.ownerRating),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Tab 2: MY LISTINGS & RENTALS DASHBOARD
// --------------------------------------------------------------------------
@Composable
fun MyRentingScreen(
    viewModel: RentalViewModel,
    isOnline: Boolean,
    onSelectRentalItem: (RentalEntity) -> Unit,
    onPrintReceipt: (RentalEntity) -> Unit
) {
    var subTab by remember { mutableStateOf("My Rentals") } // or "Post Listing"
    val rentals by viewModel.allRentals.collectAsState()
    val listings by viewModel.allListings.collectAsState()

    // Filtering listings user uploaded themselves (simulated)
    val myPosts = listings.filter { it.ownerName.contains("You") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (subTab == "My Rentals") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = subTab == "My Rentals",
                onClick = { subTab = "My Rentals" },
                text = { Text("Active Bookings / History", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == "Post Listing",
                onClick = { subTab = "Post Listing" },
                text = { Text("Lend My Stuff", fontWeight = FontWeight.Bold) }
            )
        }

        if (subTab == "My Rentals") {
            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text(
                        "Your Rental Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        "Items guarded by LendShare secure payment escrow system.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (rentals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "No rental bookings found",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                                Text(
                                    "Your booked items and transaction receipts will display here.",
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(rentals) { rental ->
                        RentalReceiptCard(
                            rental = rental,
                            onReturnClick = { viewModel.endRentalAndRefund(rental) },
                            onItemClick = { onSelectRentalItem(rental) },
                            onPrintReceiptClick = { onPrintReceipt(rental) }
                        )
                    }
                }

                // Add listings user uploaded themselves section
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "My Listed Gear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (myPosts.isEmpty()) {
                    item {
                        Text(
                            "You haven't uploaded any items for rent yet. Go to 'Lend My Stuff' to post!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                    }
                } else {
                    items(myPosts) { post ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DrawerListingIllustration(
                                    spec = post.imageSpec,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(post.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "${post.category} • \$${post.pricePerDay.toInt()}/day",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(post.locationName, fontSize = 11.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Listed", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Post listing Input Form
            PostListingForm(viewModel = viewModel, onComplete = { subTab = "My Rentals" })
        }
    }
}

@Composable
fun RentalReceiptCard(
    rental: RentalEntity,
    onReturnClick: () -> Unit,
    onItemClick: () -> Unit,
    onPrintReceiptClick: () -> Unit
) {
    val isCompleted = rental.status == "COMPLETED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rental.listingTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Receipt ID: ${rental.transactionId}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                // Badge status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isCompleted) Color(0xFF455A64) else MaterialTheme.colorScheme.secondary.copy(
                                alpha = 0.15f
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCompleted) "RETURNED" else "ACTIVE RENTAL",
                        color = if (isCompleted) Color.White else MaterialTheme.colorScheme.secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Rental Term", fontSize = 11.sp, color = Color.Gray)
                    Text("${rental.rentalDays} Days Booked", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Column {
                    Text("Total Escrow Protected", fontSize = 11.sp, color = Color.Gray)
                    Text("\$${"%.2f".format(rental.totalAmount)} Paid", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }

                Column {
                    Text("Refundable Sec Dep", fontSize = 11.sp, color = Color.Gray)
                    Text("\$${"%.0f".format(rental.securityDeposit)} Secured", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isCompleted) {
                    Button(
                        onClick = { onReturnClick() },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("return_item_${rental.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.KeyboardReturn, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Process Return", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { onPrintReceiptClick() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("print_receipt_${rental.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PostListingForm(
    viewModel: RentalViewModel,
    onComplete: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var pricePerDay by remember { mutableStateOf("") }
    var securityDeposit by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var imageSpec by remember { mutableStateOf("gradient_custom") }

    val categories = listOf("Electronics", "Outdoor", "Tools", "Transportation")
    val specs = listOf(
        Pair("Blue Wave", "gradient_custom"),
        Pair("Mint Tech", "gradient_mavic"),
        Pair("Ocean Chill", "gradient_yeti"),
        Pair("Neon Ruby", "gradient_sony"),
        Pair("Sunset Lava", "gradient_patagonia"),
        Pair("Gold Rider", "gradient_segway")
    )

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPosting by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Lend out your idle gear", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Enter item information. Earnings go straight to your wallet.", fontSize = 12.sp, color = Color.Gray)
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Item Title (e.g., Makita Cordless Drill)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_title"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Instructions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .testTag("input_description"),
                maxLines = 4
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = pricePerDay,
                    onValueChange = { pricePerDay = it },
                    label = { Text("Price / Day ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_price"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = securityDeposit,
                    onValueChange = { securityDeposit = it },
                    label = { Text("Security Deposit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_deposit"),
                    singleLine = true
                )
            }
        }

        item {
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("General Location (e.g. Presidio, SF)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_location"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        item {
            Text("Category Selector", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            ScrollableRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val active = cat == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f))
                            .clickable { category = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(cat, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Select Canvas Image Spec Theme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            ScrollableRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                specs.forEach { (name, format) ->
                    val active = format == imageSpec
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { imageSpec = format }
                    ) {
                        DrawerListingIllustration(
                            spec = format,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (active) 3.dp else 0.dp,
                                    color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                        Text(name, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (active) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }
        }

        item {
            errorMessage?.let { error ->
                Text(error, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    when {
                        title.trim().isEmpty() -> errorMessage = "Item Title is required"
                        description.trim().isEmpty() -> errorMessage = "Please enter item details"
                        pricePerDay.toDoubleOrNull() == null -> errorMessage = "Price must be a valid number"
                        securityDeposit.toDoubleOrNull() == null -> errorMessage = "Security deposit must be a number"
                        locationName.trim().isEmpty() -> errorMessage = "Location descriptor is required"
                        else -> {
                            errorMessage = null
                            isPosting = true
                            viewModel.createListing(
                                title = title,
                                description = description,
                                category = category,
                                pricePerDay = pricePerDay.toDouble(),
                                securityDeposit = securityDeposit.toDouble(),
                                locationName = locationName,
                                spec = imageSpec,
                                onComplete = {
                                    isPosting = false
                                    onComplete()
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_listing_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Publish Rental Listing & Pin Nearby Map", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Tab 3: INBOX MESSAGING SCREEN
// --------------------------------------------------------------------------
@Composable
fun InboxScreen(
    viewModel: RentalViewModel,
    onOpenChat: (ListingEntity) -> Unit
) {
    val inbox by viewModel.chatInbox.collectAsState()
    val listings by viewModel.allListings.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column {
                Text("Chat Channels", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Coordinate pickups, deliveries, and conditions securely in-app.", fontSize = 12.sp, color = Color.Gray)
            }
        }

        if (inbox.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "No ongoing discussions yet",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Text(
                        "Select any item and tap 'Message Owner' to coordinate.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(inbox) { message ->
                    val matchingItem = listings.find { it.id == message.listingId }
                    matchingItem?.let { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenChat(item) }
                                .testTag("inbox_item_${item.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DrawerListingIllustration(
                                    spec = item.imageSpec,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.ownerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.width(84.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = message.messageText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Tab 4: SECURE WALLET MANAGEMENT SCREEN
// --------------------------------------------------------------------------
@Composable
fun WalletScreen(
    viewModel: RentalViewModel,
    onPrintReceipt: (RentalEntity) -> Unit
) {
    val walletBalance by viewModel.walletBalance.collectAsState()
    val rentals by viewModel.allRentals.collectAsState()

    // Calculate dynamic safety holds
    val activeEscrowSum = rentals.filter { it.status == "ACTIVE" }.sumOf { it.totalAmount }
    val activeSecDepSum = rentals.filter { it.status == "ACTIVE" }.sumOf { it.securityDeposit }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("Secure Digital Wallet", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Secure multi-token ledger for escrow security holding and payouts.", fontSize = 12.sp, color = Color.Gray)
        }

        // Wallet Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("AVAILABLE BALANCE", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(
                        "\$${"%.2f".format(walletBalance)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.addWalletFunds(50.0) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("top_up_50"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ \$50 Top-up", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.addWalletFunds(100.0) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("top_up_100"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ \$100 Top-up", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Escrow Hold stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🛡️ LendShare Protective Escrow", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Collateral held securely in trust and automatically refunded on checkout return.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("\$${"%.2f".format(activeEscrowSum)}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("Secured Hold", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        item {
            Text("Escrow Ledger History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (rentals.isEmpty()) {
            item {
                Text(
                    "No payments found. Rentals you complete will list clear escrow receipts here.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(rentals) { rental ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (rental.status == "COMPLETED") Color(0xFF00C853).copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (rental.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (rental.status == "COMPLETED") Color(0xFF00C853) else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(rental.listingTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("ID: ${rental.transactionId}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-\$${"%.2f".format(rental.totalAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFFFF5252)
                            )
                            Text(
                                text = if (rental.status == "COMPLETED") "Settled" else "Escrow Hold",
                                fontSize = 10.sp,
                                color = if (rental.status == "COMPLETED") Color(0xFF00C853) else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onPrintReceipt(rental) },
                            modifier = Modifier.size(36.dp).testTag("wallet_print_${rental.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Print transaction receipt",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// POPUP SCREEN: LISTING DETAIL + SIMULATED MAP + REVIEWS + BOOKING CHECKOUT
// --------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailOverlayScreen(
    item: ListingEntity,
    viewModel: RentalViewModel,
    isOnline: Boolean,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onBookingApproved: (RentalEntity) -> Unit
) {
    var checkoutSheetOpen by remember { mutableStateOf(false) }

    // Dynamic database streams for comments and review count
    val reviews by viewModel.getListingReviews(item.id).collectAsState(initial = emptyList())
    val averageRating = if (reviews.isEmpty()) item.ownerRating else reviews.map { it.rating }.average().toFloat()

    // Mock geolocation coordinates offset helper
    val mockLocations = listOf(
        Pair("User GPS Center", Offset(0f, 0f)),
        Pair(item.title, Offset(0.42f, -0.3f))
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) {} // block click throughs
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Text(
                    "Item Listing Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Bookmark toggle
                IconButton(
                    onClick = { viewModel.toggleSaveListing(item.id, item.isSaved) },
                    modifier = Modifier.testTag("detail_bookmark_toggle")
                ) {
                    Icon(
                        imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save listing details",
                        tint = if (item.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Adaptive schematic vector background
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        DrawerListingIllustration(spec = item.imageSpec, modifier = Modifier.fillMaxSize())
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(item.category, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                // Rates panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("DAILY RENTAL FEE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("\$${"%.2f".format(item.pricePerDay)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    Text(" / day", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 3.dp))
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("REFUNDABLE SECURITY DEP", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("\$${"%.0f".format(item.securityDeposit)} held safe", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Description
                item {
                    Column {
                        Text("About this item", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }

                // Owner Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.ownerName.first().toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(item.ownerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Verified LendShare Owner", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${"%.1f".format(averageRating)} Star rating", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // GEOLOCATION MAP PINPOINT DISPLAY
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Geolocation (Nearby items)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "PIN: ${item.locationName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Drawing our Beautiful Custom Radar map canvas!
                        SimulatedRadarMap(
                            centerLat = item.latitude,
                            centerLng = item.longitude,
                            items = mockLocations,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        )
                    }
                }

                // REVIEWS & FEEDBACK TRUST SYSTEM
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Community Star Reviews (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trust Insured", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (reviews.isEmpty()) {
                    item {
                        Text(
                            "No reviews for this item yet. Be the first to rent and submit feedback!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                } else {
                    items(reviews) { review ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(review.reviewerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row {
                                        repeat(5) { i ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (i < review.rating.toInt()) Color(0xFFFFD54F) else Color.Gray,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    review.comment,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Write Review Field
                item {
                    AddReviewBlock(listingId = item.id, viewModel = viewModel)
                }

                // Bottom padding spacer
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Bottom action layout (Checkout & chat trigger)
            Card(
                shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Chat message action (Offline proof: disable if offline)
                    OutlinedButton(
                        onClick = onOpenChat,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("negotiate_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Pickup", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Booking checkout trigger button
                    val checkoutBlocked = !isOnline && !item.isSaved
                    Button(
                        onClick = { checkoutSheetOpen = true },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("book_checkout_button"),
                        enabled = isOnline, // Booking requires server payment confirmation
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Book & Rent Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Checkout Modal dialog Overlay (Secure Payment System)
        if (checkoutSheetOpen) {
            CheckoutDialogGateway(
                item = item,
                viewModel = viewModel,
                onDismiss = { checkoutSheetOpen = false },
                onBookingApproved = { rental ->
                    checkoutSheetOpen = false
                    onBookingApproved(rental)
                }
            )
        }
    }
}

@Composable
fun AddReviewBlock(
    listingId: Int,
    viewModel: RentalViewModel
) {
    var rating by remember { mutableStateOf(5.0f) }
    var text by remember { mutableStateOf("") }
    var completeMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.15f))
            .padding(10.dp)
    ) {
        Text("Share your Rental feedback", fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your rating: ", fontSize = 11.sp)
            repeat(5) { i ->
                val num = (i + 1).toFloat()
                IconButton(
                    onClick = { rating = num },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (num <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Rate $num stars",
                        tint = if (num <= rating) Color(0xFFFFD54F) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Write helpful details on item quality, owner pickups...", fontSize = 11.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("review_comments_input"),
            shape = RoundedCornerShape(6.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                if (text.trim().isNotEmpty()) {
                    viewModel.submitItemReview(listingId, rating, text)
                    text = ""
                    completeMessage = "Review submitted! Thank you."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .height(30.dp)
                .align(Alignment.End)
                .testTag("publish_review_btn"),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Text("Submit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        if (completeMessage.isNotEmpty()) {
            Text(completeMessage, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// --------------------------------------------------------------------------
// SECURE BILLING GATEWAY CHECKOUT MODAL WINDOW
// --------------------------------------------------------------------------
@Composable
fun CheckoutDialogGateway(
    item: ListingEntity,
    viewModel: RentalViewModel,
    onDismiss: () -> Unit,
    onBookingApproved: (RentalEntity) -> Unit
) {
    val walletBalance by viewModel.walletBalance.collectAsState()

    var days by remember { mutableStateOf(3) }
    var cardName by remember { mutableStateOf("LendShare Verified") }
    var cardNumber by remember { mutableStateOf("4111222233334444") }
    var cardExpiry by remember { mutableStateOf("09/28") }
    var cardCvv by remember { mutableStateOf("942") }

    val rate = item.pricePerDay
    val serviceFee = 3.50
    val totalEst = (rate * days) + item.securityDeposit + serviceFee

    var activeProcessing by remember { mutableStateOf(false) }
    var checkoutFeedback by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!activeProcessing) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalActivity, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Secure Escrow Billing", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Rent schedule days selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rental Term (Days)", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (days > 1) days-- }, enabled = !activeProcessing) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        Text("$days Days", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                        IconButton(onClick = { days++ }, enabled = !activeProcessing) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rental item fee ($days days)", fontSize = 12.sp, color = Color.Gray)
                            Text("\$${"%.2f".format(rate * days)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Share Escrow Deposit (Refundable)", fontSize = 12.sp, color = Color.Gray)
                            Text("\$${"%.2f".format(item.securityDeposit)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Broker Tech Fee", fontSize = 12.sp, color = Color.Gray)
                            Text("\$${"%.2f".format(serviceFee)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Escrow Total cost", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("\$${"%.2f".format(totalEst)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Balance warning
                if (walletBalance < totalEst) {
                    Text(
                        text = "⚠️ Warning: Account funds ($${"%.2f".format(walletBalance)}) insufficient. Please top-up in Wallet tab.",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Account balance: \$${"%.2f".format(walletBalance)} (Approved)",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secured Credit Card Mock form UI
                Text("Simulated Escrow Authorization Card", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        placeholder = { Text("Name on Card") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("checkout_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent)
                    )

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        placeholder = { Text("Secured Account Card Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("checkout_card_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = { cardExpiry = it },
                            placeholder = { Text("MM/YY") },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent)
                        )
                        OutlinedTextField(
                            value = cardCvv,
                            onValueChange = { cardCvv = it },
                            placeholder = { Text("CVV") },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent)
                        )
                    }
                }

                if (activeProcessing) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Authorizing Secure payment with bank...", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                checkoutFeedback?.let { feedback ->
                    Text(feedback, color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    activeProcessing = true
                    viewModel.processRentalCheckout(
                        item = item,
                        days = days,
                        cardName = cardName,
                        cardNumber = cardNumber,
                        cardLast4 = if (cardNumber.length >= 4) cardNumber.takeLast(4) else "4444",
                        onComplete = { success, msg, txId ->
                            activeProcessing = false
                            if (success && txId != null) {
                                val rental = RentalEntity(
                                    listingId = item.id,
                                    listingTitle = item.title,
                                    pricePerDay = item.pricePerDay,
                                    totalAmount = (item.pricePerDay * days) + item.securityDeposit,
                                    securityDeposit = item.securityDeposit,
                                    status = "ACTIVE",
                                    rentalDays = days,
                                    paymentCardLast4 = if (cardNumber.length >= 4) cardNumber.takeLast(4) else "4444",
                                    transactionId = txId
                                )
                                onBookingApproved(rental)
                            } else {
                                checkoutFeedback = msg
                            }
                        }
                    )
                },
                enabled = walletBalance >= totalEst && !activeProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("confirm_escrow_payment"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Process Safely through Secured Gateway", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !activeProcessing) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// --------------------------------------------------------------------------
// POPUP SCREEN: DIRECT CHAT ROOM THREAD WITH AUTO-REPLY SIMULATION
// --------------------------------------------------------------------------
@Composable
fun ChatThreadOverlayScreen(
    listing: ListingEntity,
    viewModel: RentalViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.getListingMessages(listing.id).collectAsState(initial = emptyList())
    val typingId by viewModel.isTyping.collectAsState()
    val isRenterTyping = typingId == listing.id

    var chatInputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) {} // block click throughs
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                DrawerListingIllustration(
                    spec = listing.imageSpec,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(listing.ownerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Related gears: ${listing.title}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Message List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg ->
                    val isYou = msg.isFromUser
                    val isPaySys = msg.senderName == "LendShare Pay"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isYou) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isYou) 12.dp else 0.dp,
                                        bottomEnd = if (isYou) 0.dp else 12.dp
                                    )
                                )
                                .background(
                                    when {
                                        isYou -> MaterialTheme.colorScheme.primary
                                        isPaySys -> Color(0xFF1B5E20)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .padding(10.dp)
                                .widthIn(max = 240.dp)
                        ) {
                            Column {
                                if (!isYou) {
                                    Text(
                                        msg.senderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isPaySys) Color(0xFF00E676) else MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    msg.messageText,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // typing bubble
                if (isRenterTyping) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "${listing.ownerName} is typing response...",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Messaging inputs
            Card(
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // pre-populated messaging templates for fast rental scheduling
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Is this item available today?",
                            "Can I negotiate a lower deposit hold?",
                            "Where can we meet up for pickup?",
                            "Is item completely sanitized?"
                        ).forEach { temp ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .clickable {
                                        viewModel.sendDirectMessage(listing.id, listing.ownerName, temp)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(temp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Write chat details...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text")
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (chatInputText.trim().isNotEmpty()) {
                                    viewModel.sendDirectMessage(listing.id, listing.ownerName, chatInputText)
                                    chatInputText = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .size(44.dp)
                                .testTag("chat_send_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// GENERIC HELPERS FOR COMPOSABLES
// --------------------------------------------------------------------------
@Composable
fun ScrollableRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

// --------------------------------------------------------------------------
// PROFILE AND SAVED WISHLIST OVERLAY SCREEN
// --------------------------------------------------------------------------
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileOverlayScreen(
    viewModel: RentalViewModel,
    isOnline: Boolean,
    onClose: () -> Unit,
    onSelectItem: (ListingEntity) -> Unit
) {
    val myRentals by viewModel.allRentals.collectAsState(initial = emptyList())
    val completedRentalsCount = remember(myRentals) { myRentals.count { it.status == "COMPLETED" } }
    val savedListings by viewModel.savedListings.collectAsState(initial = emptyList())

    // Define ProfileTier holder structure safely
    class ProfileTier(val name: String, val color: Color, val nextTierText: String, val icon: ImageVector)
    val tier = remember(completedRentalsCount) {
        when {
            completedRentalsCount < 5 -> {
                val rem = 5 - completedRentalsCount
                ProfileTier(
                    "Bronze Renter", 
                    Color(0xFFCD7F32), 
                    "$rem more completed rentings to reach Silver Level!", 
                    Icons.Default.WorkspacePremium
                )
            }
            completedRentalsCount in 5..9 -> {
                val rem = 10 - completedRentalsCount
                ProfileTier(
                    "Silver Companion", 
                    Color(0xFF90A4AE), 
                    "Only $rem more successful rentings to claim Gold Status! 🏆", 
                    Icons.Default.Verified
                )
            }
            completedRentalsCount in 10..14 -> {
                val rem = 15 - completedRentalsCount
                ProfileTier(
                    "Gold Creator", 
                    Color(0xFFFFB300), 
                    "$rem more until Elite Platinum! Keep it up!", 
                    Icons.Default.WorkspacePremium
                )
            }
            else -> {
                ProfileTier(
                    "Elite Platinum", 
                    Color(0xFF00AA8D), 
                    "You've unlocked maximum LendShare trust tier! Elite discount enabled.", 
                    Icons.Default.Stars
                )
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) {} // block click throughs
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("profile_close_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Text(
                    "User Profile & Wishlist",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336))
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User info card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("profile_info_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AC",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                "Alex Carter",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                "alex.carter@lendshare-app.com",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Room,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("San Francisco, CA", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // DYNAMIC MILESTONE BOOKMARK CARD
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("profile_trust_landmark_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "GAMIFIED TRUST LEVEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic Hanging Bookmark Ribbon
                                CompletedRentalsBookmark(
                                    completedCount = completedRentalsCount,
                                    modifier = Modifier.padding(end = 16.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tier.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tier.color
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = tier.icon,
                                            contentDescription = null,
                                            tint = tier.color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "$completedRentalsCount Completed successful rentings",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = tier.nextTierText,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            val progressFactor = (completedRentalsCount / 15f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progressFactor },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = tier.color,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Milestone Simulator
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "🏆 Milestones Tester Simulator (Try different levels!)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.simulateCompletedRental() },
                                    modifier = Modifier.weight(1f).testTag("simulate_add_rental_button"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simulate completed", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.removeOneCompletedRental() },
                                    enabled = completedRentalsCount > 0,
                                    modifier = Modifier.weight(1f).testTag("simulate_remove_rental_button"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                                ) {
                                    Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove simulated", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // WISHLIST & SAVED ITEMS FEATURE SECTION
                item {
                    Column {
                        Text(
                            text = "YOUR WISHLIST & SAVED ITEMS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (savedListings.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("wishlist_empty_card"),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Your Wishlist is Empty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tap the bookmark icon on any item in the marketplace grid to save its details for offline view.",
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            savedListings.forEach { savedItem ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onSelectItem(savedItem) }
                                        .testTag("wishlist_item_${savedItem.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            DrawerListingIllustration(
                                                spec = savedItem.imageSpec,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            // Mini bookmark ribbon overlay inside thumbnail
                                            CompletedRentalsBookmark(
                                                completedCount = savedItem.completedRentalsCount,
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(2.dp),
                                                isSmall = true
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = savedItem.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = savedItem.category,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "\$${"%.2f".format(savedItem.pricePerDay)} / day",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onSelectItem(savedItem) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Launch,
                                                    contentDescription = "View listing details",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.toggleSaveListing(savedItem.id, true) },
                                                modifier = Modifier.size(36.dp).testTag("unsave_wishlist_${savedItem.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bookmark,
                                                    contentDescription = "Remove from wishlist",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrintableReceiptDialog(
    rental: RentalEntity,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPrinting by remember { mutableStateOf(false) }
    var printProgress by remember { mutableStateOf(0f) }
    var printSuccess by remember { mutableStateOf(false) }

    val formattedDate = remember(rental.timestamp) {
        try {
            java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
                .format(java.util.Date(rental.timestamp))
        } catch (e: Exception) {
            "June 01, 2026 - 08:48 AM"
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isPrinting) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secure Escrow Receipt", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDismiss, enabled = !isPrinting) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Printer Progress Layout
                if (isPrinting) {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("printing_progress_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                progress = { printProgress },
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "CONNECTING DUPLEX THERMAL PRINTER...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Streaming receipt image: ${(printProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (printSuccess) {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("printing_success_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00C853).copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PDF PRINT SENT!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00C853)
                            )
                            Text(
                                text = "Sent to Local Android Spooler (LENDSHARE-${rental.transactionId}.pdf)",
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // THE THERMAL RECEIPT CANVAS LOOK
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .testTag("thermal_receipt_card"),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFBF8)) // Clean paper ivory
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Jagged Tearing line top
                        Text(
                            text = "- - - - - - - - - - - - - - - - - -",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Receipt header logo brand
                        Text(
                            text = "L E N D S H A R E",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF1E2122)
                        )
                        Text(
                            text = "SECURED ESCROW NETWORK",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "VERIFIED PEER-TO-PEER CO.",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "= = = = = = = = = = = = = = = = = =",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Black
                        )

                        // Itemized receipts details align
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ReceiptMonoRow("DATE/TIME:", formattedDate)
                            ReceiptMonoRow("RECEIPT ID:", rental.transactionId)
                            ReceiptMonoRow("NODE ADDR:", "SECURE_GATEWAY_SF_#402")
                            ReceiptMonoRow("AUTH STATE:", "SECURE_ESCROW_HOLD")
                            ReceiptMonoRow("CARD USED:", "XXXX-XXXX-XXXX-${rental.paymentCardLast4}")
                        }

                        Text(
                            text = "- - - - - - - - - - - - - - - - - -",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        // DURATION & RENTAL ITEM BLOCK
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "BOOKED CONTRACT DETAIL:",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = "ITEM: ${rental.listingTitle.uppercase()}",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E2122),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "RENTAL TERM:",
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${rental.rentalDays} DAYS BOOKED",
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "DAILY CONTRACT RATE:",
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "\$${"%.2f".format(rental.pricePerDay)} / Day",
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        Text(
                            text = "- - - - - - - - - - - - - - - - - -",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        // COST BREAKDOWN SECTION
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            val subtotal = rental.rentalDays * rental.pricePerDay
                            ReceiptMonoRow("SUBTOTAL FEE:", "\$${"%.2f".format(subtotal)}")
                            ReceiptMonoRow("ESCROW SEC DEPOSIT:", "\$${"%.2f".format(rental.securityDeposit)}")
                            ReceiptMonoRow("LENDSHARE TECH:", "\$0.00 (Promo)")

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "TOTAL ESCROW PAID:",
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                                Text(
                                    text = "\$${"%.2f".format(rental.totalAmount)}",
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }

                        Text(
                            text = "= = = = = = = = = = = = = = = = = =",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // BARCODE CANVAS DRAWING
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .padding(horizontal = 14.dp)
                        ) {
                            val barWidths = listOf(2f, 4f, 1f, 6f, 3f, 1f, 4f, 7f, 1f, 3f, 5f, 2f, 1f, 6f, 3f, 1f, 5f, 1f, 4f, 2f, 5f, 1f)
                            var currentX = 0f
                            val spacing = 3.5f
                            var index = 0
                            while (currentX < size.width) {
                                val w = barWidths[index % barWidths.size]
                                val isBar = index % 2 == 0
                                if (isBar) {
                                    drawRect(
                                        color = Color(0xFF1E2122),
                                        topLeft = androidx.compose.ui.geometry.Offset(currentX, 0f),
                                        size = androidx.compose.ui.geometry.Size(w * 1.5f, size.height)
                                    )
                                }
                                currentX += (w * 1.5f) + spacing
                                index++
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "*${rental.transactionId}*",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "THANK YOU FOR RENTING PEER-TO-PEER",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ESCROW PROTECTED BY LENDSHARE INC",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Jagged Tearing line bottom
                        Text(
                            text = "- - - - - - - - - - - - - - - - - -",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isPrinting = true
                        printProgress = 0f
                        printSuccess = false
                        while (printProgress < 1.0f) {
                            kotlinx.coroutines.delay(120)
                            printProgress += 0.1f
                        }
                        isPrinting = false
                        printSuccess = true
                        android.widget.Toast.makeText(
                            context,
                            "Receipt of transaction ${rental.transactionId} printed safely via LendShare cloud node!",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                },
                enabled = !isPrinting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("execute_print_button")
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Print / Save PDF")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isPrinting,
                modifier = Modifier.testTag("dismiss_receipt_button")
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ReceiptMonoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 150.dp)
        )
    }
}
