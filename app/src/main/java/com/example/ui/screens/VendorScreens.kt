package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.Vendor
import com.example.model.VendorIconType
import com.example.ui.components.BartrButtonPrimary
import com.example.ui.components.BartrButtonSecondary
import com.example.ui.components.BartrMapView
import com.example.ui.components.BartrSpinnerState
import com.example.ui.components.BartrTopBar
import com.example.ui.theme.BartrBackdrop
import com.example.ui.theme.BartrBlue
import com.example.ui.theme.BartrBlueT4
import com.example.ui.theme.BartrDanger
import com.example.ui.theme.BartrGreenT4
import com.example.ui.theme.BartrInk
import com.example.ui.theme.BartrInkSoft
import com.example.ui.theme.BartrStar
import com.example.ui.theme.BartrSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VendorProfileScreen(
    vendor: Vendor,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onRequestVendor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconVector = when (vendor.iconType) {
        VendorIconType.REPAIR -> Icons.Filled.Build
        VendorIconType.BEAUTY -> Icons.Filled.Spa
        VendorIconType.MECHANIC -> Icons.Filled.DirectionsCar
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrTopBar(
            title = "Vendor",
            onBack = onBack,
            trailing = {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BartrBackdrop)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = onOpenChat
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubble,
                        contentDescription = "Chat",
                        tint = BartrInk,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Vendor Hero
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BartrBlueT4),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = BartrBlue,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = vendor.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = BartrInk
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${vendor.rating} · ${vendor.reviews} · ${vendor.distance}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = BartrInkSoft,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // About
                Text(
                    text = "ABOUT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = BartrInkSoft,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = vendor.blurb,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = BartrInk,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Typical price
                Text(
                    text = "TYPICAL PRICE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = BartrInkSoft,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = vendor.price,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = BartrInk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Reviews
                Text(
                    text = "WHAT PEOPLE SAY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = BartrInkSoft,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                ReviewCard(text = vendor.review1)
                Spacer(modifier = Modifier.height(10.dp))
                ReviewCard(text = vendor.review2)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Request button bottom fixed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            BartrButtonPrimary(
                text = "Request this vendor",
                onClick = onRequestVendor
            )
        }
    }
}

@Composable
private fun ReviewCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BartrBackdrop)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BartrInk,
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Verified customer",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BartrInkSoft,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun SendingScreen(
    onSent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(1400)
        onSent()
    }

    BartrSpinnerState(
        title = "Sending your request",
        subtitle = "Letting them know you need help.",
        modifier = modifier
    )
}

@Composable
fun JobStatusScreen(
    vendor: Vendor,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onCancel: () -> Unit,
    onCompleteJob: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    val iconVector = when (vendor.iconType) {
        VendorIconType.REPAIR -> Icons.Filled.Build
        VendorIconType.BEAUTY -> Icons.Filled.Spa
        VendorIconType.MECHANIC -> Icons.Filled.DirectionsCar
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel request?", fontWeight = FontWeight.Bold, color = BartrInk) },
            text = { Text("The vendor will be notified that you cancelled.", color = BartrInkSoft) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancel()
                    }
                ) {
                    Text("Yes, cancel", color = BartrDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep request", color = BartrInk)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Map section (top 44%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.44f)
            ) {
                BartrMapView(
                    vendors = listOf(vendor),
                    isMiniMap = true,
                    trackedVendor = vendor,
                    onVendorSelected = {},
                    modifier = Modifier.fillMaxSize()
                )

                // Back button
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 20.dp, top = 16.dp)
                        .size(46.dp)
                        .shadow(10.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = onBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BartrInk,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom sheet section (bottom 56%)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.56f)
                    .shadow(16.dp, shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Arrive headline
                    Text(
                        text = "Arriving in 15 mins",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = BartrInk
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${vendor.name} · ${vendor.category}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BartrInkSoft,
                            fontSize = 14.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Driver actions row (Avatar / Chat / Safety)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Vendor column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(BartrBackdrop),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = null,
                                        tint = BartrInk,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                // Green checkmark verified badge
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(BartrSuccess)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Verified",
                                        tint = Color.White,
                                        modifier = Modifier.size(9.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = vendor.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BartrInkSoft,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Chat button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onOpenChat
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(BartrBlueT4),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChatBubble,
                                    contentDescription = "Chat",
                                    tint = BartrBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Chat",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BartrInkSoft,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Safety button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = {
                                        Toast.makeText(context, "Safety emergency line: +234 800 000 911", Toast.LENGTH_LONG).show()
                                    }
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(BartrBackdrop),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = "Safety",
                                    tint = BartrInk,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Safety",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BartrInkSoft,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BartrInk.copy(alpha = 0.08f), thickness = 1.dp)

                    // Info rows
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = BartrInkSoft,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "14 Market Road, Ikeja",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BartrInk,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit location",
                            tint = BartrBlue,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    Toast.makeText(context, "Location cannot be changed after dispatch", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Payments,
                            contentDescription = null,
                            tint = BartrSuccess,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Cash",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BartrInk,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = vendor.finalPrice,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = BartrInk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }

                    HorizontalDivider(color = BartrInk.copy(alpha = 0.08f), thickness = 1.dp)

                    // Cancel request row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { showCancelDialog = true }
                            )
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Block,
                            contentDescription = null,
                            tint = BartrDanger,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Cancel request",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BartrDanger,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Complete Job & Rate (Affordance to advance to Rating)
                    BartrButtonPrimary(
                        text = "Complete job & rate",
                        onClick = onCompleteJob
                    )

                    Spacer(modifier = Modifier.navigationBarsPadding())
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    vendor: Vendor,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("1", "Hi! Happy to help, what do you need?", false, "10:15 AM")
        )
    }
    var currentText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val iconVector = when (vendor.iconType) {
        VendorIconType.REPAIR -> Icons.Filled.Build
        VendorIconType.BEAUTY -> Icons.Filled.Spa
        VendorIconType.MECHANIC -> Icons.Filled.DirectionsCar
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BartrBackdrop)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = BartrInk,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BartrBlueT4),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = BartrBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = vendor.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = BartrInk
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        HorizontalDivider(color = BartrInk.copy(alpha = 0.08f), thickness = 1.dp)

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (msg.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (msg.isFromUser) 16.dp else 4.dp,
                                    bottomEnd = if (msg.isFromUser) 4.dp else 16.dp
                                )
                            )
                            .background(if (msg.isFromUser) BartrBlue else BartrBackdrop)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (msg.isFromUser) Color.White else BartrInk,
                                fontSize = 15.sp,
                                lineHeight = 21.sp
                            )
                        )
                    }
                }
            }
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(BartrBackdrop)
                    .border(1.5.dp, BartrInk.copy(alpha = 0.12f), CircleShape)
                    .padding(horizontal = 18.dp, vertical = 13.dp)
            ) {
                if (currentText.isEmpty()) {
                    Text(
                        text = "Type a message",
                        color = BartrInkSoft,
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = BartrInk,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(BartrBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (currentText.trim().isNotEmpty()) {
                                messages.add(
                                    ChatMessage(
                                        id = System.currentTimeMillis().toString(),
                                        text = currentText.trim(),
                                        isFromUser = true
                                    )
                                )
                                currentText = ""
                                scope.launch {
                                    listState.animateScrollToItem(messages.lastIndex)
                                    delay(900)
                                    messages.add(
                                        ChatMessage(
                                            id = (System.currentTimeMillis() + 1).toString(),
                                            text = "Got it! I'm on my way to your location now.",
                                            isFromUser = false
                                        )
                                    )
                                    listState.animateScrollToItem(messages.lastIndex)
                                }
                            }
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(BartrBlue)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = {
                            if (currentText.trim().isNotEmpty()) {
                                messages.add(
                                    ChatMessage(
                                        id = System.currentTimeMillis().toString(),
                                        text = currentText.trim(),
                                        isFromUser = true
                                    )
                                )
                                currentText = ""
                                scope.launch {
                                    listState.animateScrollToItem(messages.lastIndex)
                                    delay(900)
                                    messages.add(
                                        ChatMessage(
                                            id = (System.currentTimeMillis() + 1).toString(),
                                            text = "Got it! I'm on my way to your location now.",
                                            isFromUser = false
                                        )
                                    )
                                    listState.animateScrollToItem(messages.lastIndex)
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
fun RatingScreen(
    vendor: Vendor,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var rating by remember { mutableIntStateOf(0) }
    var reviewFeedback by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How was it?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = BartrInk
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Rate your experience with ${vendor.name}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = BartrInkSoft,
                fontSize = 15.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 5 Star interactive selection
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 1..5) {
                IconButton(
                    onClick = { rating = i },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "$i Stars",
                        tint = if (i <= rating) BartrStar else BartrInk.copy(alpha = 0.18f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Feedback field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BartrBackdrop)
                .border(1.5.dp, BartrInk.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            if (reviewFeedback.isEmpty()) {
                Text(
                    text = "Anything you'd like to add? (optional)",
                    color = BartrInkSoft.copy(alpha = 0.7f),
                    fontSize = 14.5.sp
                )
            }
            BasicTextField(
                value = reviewFeedback,
                onValueChange = { reviewFeedback = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    color = BartrInk,
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(BartrBlue)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        BartrButtonPrimary(
            text = "Submit rating",
            onClick = {
                Toast.makeText(context, "Thanks for rating your experience!", Toast.LENGTH_SHORT).show()
                onSubmit()
            }
        )
    }
}
