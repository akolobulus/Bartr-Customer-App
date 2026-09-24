package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.example.ai.AutonomousAction
import com.example.ai.VoiceConversationManager
import com.example.model.BartrRepository
import com.example.model.Vendor
import com.example.ui.components.BartrMapView
import com.example.ui.components.BartrVendorRow
import com.example.ui.components.LiveVoiceSheet
import com.example.ui.theme.BartrBackdrop
import com.example.ui.theme.BartrBlue
import com.example.ui.theme.BartrBlueS2
import com.example.ui.theme.BartrBlueT4
import com.example.ui.theme.BartrInk
import com.example.ui.theme.BartrInkSoft

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onVendorClick: (Vendor) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPayments: () -> Unit,
    onOpenPromotions: () -> Unit,
    onOpenMyRequests: () -> Unit,
    onOpenSavedVendors: () -> Unit,
    onOpenInviteFriend: () -> Unit,
    onOpenGetHelp: () -> Unit,
    onOpenAbout: () -> Unit,
    onRequestVendorDirect: ((String) -> Unit)? = null,
    onOpenChatDirect: ((String) -> Unit)? = null,
    onSearchMatchesDirect: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var recenterTrigger by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    var currentVendors by remember {
        mutableStateOf(
            listOf(
                BartrRepository.getVendor("chuka"),
                BartrRepository.getVendor("adaeze"),
                BartrRepository.getVendor("musa"),
            )
        )
    }
    var selectedVendorId by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    var showVoiceSheet by remember { mutableStateOf(false) }

    val voiceManager = remember {
        VoiceConversationManager(
            context = context,
            coroutineScope = coroutineScope,
            onExecuteAction = { action ->
                when (action) {
                    is AutonomousAction.SearchVendors -> {
                        Toast.makeText(context, "AI: Searching for ${action.query}", Toast.LENGTH_SHORT).show()
                        if (onSearchMatchesDirect != null) {
                            onSearchMatchesDirect(action.query)
                        } else {
                            onSearchClick()
                        }
                    }
                    is AutonomousAction.FilterVendors -> {
                        Toast.makeText(context, "AI: Filtered by ${action.value}", Toast.LENGTH_SHORT).show()
                        when (action.filterType.lowercase()) {
                            "cheapest" -> currentVendors = BartrRepository.vendors.sortedBy {
                                it.finalPrice.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 99999
                            }
                            "top_rated" -> currentVendors = BartrRepository.vendors.sortedByDescending { it.ratingNum }
                            "nearest" -> currentVendors = BartrRepository.vendors.sortedBy {
                                it.distance.replace("km", "").toFloatOrNull() ?: 99f
                            }
                            else -> {
                                val catMatch = BartrRepository.vendors.filter {
                                    it.category.contains(action.value, ignoreCase = true) ||
                                    it.name.contains(action.value, ignoreCase = true)
                                }
                                if (catMatch.isNotEmpty()) currentVendors = catMatch
                            }
                        }
                    }
                    is AutonomousAction.SelectVendor -> {
                        val vendor = BartrRepository.getVendor(action.vendorId)
                        selectedVendorId = vendor.id
                        Toast.makeText(context, "AI: Selected ${vendor.name}", Toast.LENGTH_SHORT).show()
                    }
                    is AutonomousAction.CallVendor -> {
                        Toast.makeText(context, "AI: Calling ${action.vendorName} (${action.phoneNumber})", Toast.LENGTH_SHORT).show()
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.phoneNumber}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening dialer for ${action.vendorName}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is AutonomousAction.NavigateTo -> {
                        Toast.makeText(context, "AI: Navigating to ${action.label}", Toast.LENGTH_SHORT).show()
                        when (action.destination) {
                            "payments" -> onOpenPayments()
                            "promotions" -> onOpenPromotions()
                            "my_requests" -> onOpenMyRequests()
                            "saved_vendors" -> onOpenSavedVendors()
                            "help" -> onOpenGetHelp()
                            "about" -> onOpenAbout()
                            "profile" -> onOpenProfile()
                            "edit_profile" -> onOpenProfile()
                            "invite_friend" -> onOpenInviteFriend()
                            "matches" -> onSearchClick()
                            else -> {
                                // Home screen - already on home
                            }
                        }
                    }
                    is AutonomousAction.RecenterMap -> {
                        recenterTrigger++
                        Toast.makeText(context, "AI: Map centered on 14 Market Road, Ikeja", Toast.LENGTH_SHORT).show()
                    }
                    is AutonomousAction.ZoomMap -> {
                        recenterTrigger++
                        Toast.makeText(context, "AI: Zooming map ${action.direction}", Toast.LENGTH_SHORT).show()
                    }
                    is AutonomousAction.ApplyPromo -> {
                        Toast.makeText(context, "AI: Promo code ${action.code} applied!", Toast.LENGTH_LONG).show()
                        onOpenPromotions()
                    }
                    is AutonomousAction.OpenVendorChat -> {
                        Toast.makeText(context, "AI: Opening chat with ${action.vendorName}", Toast.LENGTH_SHORT).show()
                        if (onOpenChatDirect != null) {
                            onOpenChatDirect(action.vendorId)
                        } else {
                            onVendorClick(BartrRepository.getVendor(action.vendorId))
                        }
                    }
                    is AutonomousAction.BookVendor -> {
                        Toast.makeText(context, "AI: Booking confirmed for ${action.vendorName}!", Toast.LENGTH_LONG).show()
                        if (onRequestVendorDirect != null) {
                            onRequestVendorDirect(action.vendorId)
                        } else {
                            onVendorClick(BartrRepository.getVendor(action.vendorId))
                        }
                    }
                    is AutonomousAction.ToggleSaveVendor -> {
                        Toast.makeText(context, "AI: ${if (action.save) "Saved" else "Removed"} ${action.vendorName} in favorites", Toast.LENGTH_SHORT).show()
                    }
                    is AutonomousAction.CalculateQuote -> {
                        Toast.makeText(context, "AI: ${action.trade} quote estimate: ${action.estimate}", Toast.LENGTH_LONG).show()
                    }
                    is AutonomousAction.ShareApp -> {
                        try {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, "Use Bartr in Lagos to find trusted local artisans! Referral code: ${action.referralCode}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Bartr"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Share referral code: ${action.referralCode}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is AutonomousAction.ClearFilters -> {
                        currentVendors = BartrRepository.vendors
                        selectedVendorId = null
                        Toast.makeText(context, "AI: Filters cleared. Showing all artisans", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceSheet = true
            voiceManager.startSession()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice commands", Toast.LENGTH_SHORT).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = BartrBlueS2.copy(alpha = 0.40f),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.82f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
                    // Drawer Top Header with close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Menu",
                                tint = BartrInkSoft,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Profile header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    onOpenProfile()
                                }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(BartrBlueT4),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                color = BartrBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Alex Johnson",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = BartrInk
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+234 704 200 1836",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BartrInkSoft,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }

                    HorizontalDivider(
                        color = BartrInk.copy(alpha = 0.08f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // Menu items list
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        DrawerMenuItem(
                            icon = Icons.Filled.CreditCard,
                            title = "Payments",
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onOpenPayments()
                            }
                        )
                        DrawerMenuItem(
                            icon = Icons.Filled.LocalOffer,
                            title = "Promotions",
                            subtitle = "Enter promo code",
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onOpenPromotions()
                            }
                        )
                        DrawerMenuItem(
                            icon = Icons.Filled.History,
                            title = "My requests",
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onOpenMyRequests()
                            }
                        )
                        DrawerMenuItem(
                            icon = Icons.Filled.Bookmark,
                            title = "Saved vendors",
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onOpenSavedVendors()
                            }
                        )
                        DrawerMenuItem(
                            icon = Icons.Filled.CardGiftcard,
                            title = "Invite a friend",
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onOpenInviteFriend()
                            }
                        )
                        DrawerMenuItem(
                            icon = Icons.AutoMirrored.Filled.Help,
                            title = "Get help",
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onOpenGetHelp()
                            }
                        )
                        DrawerMenuItem(
                            icon = Icons.Filled.Info,
                            title = "About",
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onOpenAbout()
                            }
                        )
                    }

                    // Become a vendor CTA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BartrBlue)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    Toast.makeText(context, "Vendor mode is coming soon!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "Become a vendor",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Get found for what you do",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Map section (top 36%)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.36f)
                ) {
                    BartrMapView(
                        vendors = currentVendors,
                        selectedVendorId = selectedVendorId,
                        onVendorSelected = { vendor ->
                            selectedVendorId = vendor.id
                            onVendorClick(vendor)
                        },
                        recenterTrigger = recenterTrigger,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Menu button (floating top left: 46dp circle, white)
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 20.dp, top = 16.dp)
                            .size(46.dp)
                            .shadow(elevation = 10.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = BartrInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Recenter button (floating bottom right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 28.dp)
                            .size(46.dp)
                            .shadow(12.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    recenterTrigger++
                                    Toast.makeText(context, "Centered on 14 Market Road, Ikeja", Toast.LENGTH_SHORT).show()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Locate",
                        tint = BartrBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Voice Conversation button hovering on the right hand side of the map
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(end = 18.dp, top = 16.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    BartrBlue,
                                    Color(0xFF7E57C2)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                val hasRecordAudio = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasRecordAudio) {
                                    showVoiceSheet = true
                                    voiceManager.startSession()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speaker Volume",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Voice AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Bottom sheet section (bottom 64%)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.64f)
                    .shadow(18.dp, shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)),
                shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Handle
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(BartrInk.copy(alpha = 0.15f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Search Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BartrBackdrop)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onSearchClick
                            )
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = BartrInkSoft,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "What do you need?",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = BartrInk,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "NEARBY VENDORS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            letterSpacing = 1.sp,
                            color = BartrInkSoft
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    currentVendors.forEachIndexed { index, vendor ->
                        BartrVendorRow(
                            vendor = vendor,
                            onClick = { onVendorClick(vendor) }
                        )
                        if (index < currentVendors.lastIndex) {
                            HorizontalDivider(
                                color = BartrInk.copy(alpha = 0.08f),
                                thickness = 1.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.navigationBarsPadding())
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

    if (showVoiceSheet) {
        LiveVoiceSheet(
            voiceManager = voiceManager,
            onDismiss = { showVoiceSheet = false }
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = BartrInk,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = BartrInk
                )
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BartrInkSoft,
                        fontSize = 12.5.sp
                    )
                )
            }
        }
    }
}
