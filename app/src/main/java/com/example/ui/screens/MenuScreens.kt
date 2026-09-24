package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BartrRepository
import com.example.model.Vendor
import com.example.model.VendorIconType
import com.example.ui.components.BartrButtonPrimary
import com.example.ui.components.BartrLogoMark
import com.example.ui.components.BartrNameLogo
import com.example.ui.components.BartrPageHeader
import com.example.ui.components.BartrTopBar
import com.example.ui.components.BartrVendorRow
import com.example.ui.theme.BartrBackdrop
import com.example.ui.theme.BartrBlue
import com.example.ui.theme.BartrBlueS2
import com.example.ui.theme.BartrBlueT4
import com.example.ui.theme.BartrDanger
import com.example.ui.theme.BartrGreenT4
import com.example.ui.theme.BartrInk
import com.example.ui.theme.BartrInkSoft
import com.example.ui.theme.BartrSuccess

// ==================== PAYMENTS ====================
@Composable
fun PaymentsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrPageHeader(title = "Payments", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Pay however feels safest to you. Cash works for every job, or add a card for faster checkout.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BartrInkSoft,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Cash on completion (Default)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BartrBackdrop)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Payments,
                    contentDescription = null,
                    tint = BartrBlue,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cash on completion",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BartrInk,
                            fontSize = 15.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pay the vendor directly when the job's done",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BartrInkSoft,
                            fontSize = 13.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BartrGreenT4)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Default",
                        color = BartrSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BartrBackdrop)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = {
                            Toast.makeText(context, "Card payments are coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CreditCard,
                    contentDescription = null,
                    tint = BartrBlue,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add a debit or credit card",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BartrInk,
                            fontSize = 15.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "For escrow-protected, higher-value jobs",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BartrInkSoft,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}

// ==================== PROMOTIONS ====================
@Composable
fun PromotionsScreen(
    onBack: () -> Unit,
    onInviteFriend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var promoCode by remember { mutableStateOf("") }
    var promoFeedback by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrPageHeader(title = "Enter promo code", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BartrBackdrop)
                    .border(1.5.dp, BartrInk.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                if (promoCode.isEmpty()) {
                    Text(text = "Promo code", color = BartrInkSoft, fontSize = 15.sp)
                }
                BasicTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = BartrInk, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    cursorBrush = SolidColor(BartrBlue)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "The promo will be applied to your next request.",
                style = MaterialTheme.typography.bodyMedium.copy(color = BartrInkSoft, fontSize = 14.5.sp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Referral callout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BartrBlueT4)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onInviteFriend
                    )
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CardGiftcard,
                        contentDescription = null,
                        tint = BartrBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Don't have a code yet?",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BartrInk,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Refer a friend to get one",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BartrInkSoft,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            if (promoFeedback.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = promoFeedback,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (promoFeedback.contains("applied", ignoreCase = true)) BartrSuccess else BartrInkSoft,
                        fontSize = 14.sp
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            BartrButtonPrimary(
                text = "Apply",
                onClick = {
                    val code = promoCode.trim()
                    promoFeedback = if (code.isEmpty()) {
                        "Enter a code to apply it."
                    } else if (code.equals("BARTR500", ignoreCase = true) || code.equals("WELCOME", ignoreCase = true)) {
                        "Promo code \"$code\" applied! ₦500 off your next request."
                    } else {
                        "\"$code\" doesn't match an active promotion right now."
                    }
                }
            )
        }
    }
}

// ==================== MY REQUESTS ====================
@Composable
fun MyRequestsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val requests = remember { BartrRepository.pastRequests }
    val grouped = remember { requests.groupBy { it.month } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrPageHeader(title = "My requests", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            grouped.forEach { (month, list) ->
                Text(
                    text = month,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BartrInk,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                )

                list.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconVector = when (item.iconType) {
                            VendorIconType.REPAIR -> Icons.Filled.Build
                            VendorIconType.BEAUTY -> Icons.Filled.Spa
                            VendorIconType.MECHANIC -> Icons.Filled.DirectionsCar
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BartrBlueT4),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = BartrBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BartrInk,
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.vendorName} · ${item.status}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BartrInkSoft,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Text(
                            text = item.price,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BartrInk,
                                fontSize = 15.sp
                            )
                        )
                    }

                    if (index < list.lastIndex) {
                        HorizontalDivider(color = BartrInk.copy(alpha = 0.08f), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

// ==================== SAVED VENDORS ====================
@Composable
fun SavedVendorsScreen(
    onBack: () -> Unit,
    onVendorClick: (Vendor) -> Unit,
    modifier: Modifier = Modifier,
) {
    val saved = remember {
        listOf(
            BartrRepository.getVendor("chuka"),
            BartrRepository.getVendor("adaeze"),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrPageHeader(title = "Saved vendors", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            saved.forEachIndexed { index, vendor ->
                BartrVendorRow(
                    vendor = vendor,
                    onClick = { onVendorClick(vendor) }
                )
                if (index < saved.lastIndex) {
                    HorizontalDivider(color = BartrInk.copy(alpha = 0.08f), thickness = 1.dp)
                }
            }
        }
    }
}

// ==================== GET HELP (HUB) ====================
@Composable
fun GetHelpScreen(
    onBack: () -> Unit,
    onOpenFaqFeatures: () -> Unit,
    onOpenFaqAccount: () -> Unit,
    onOpenFaqPayments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrPageHeader(title = "Get help", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HelpNavCard(
                icon = Icons.Filled.Tune,
                title = "App and features",
                onClick = onOpenFaqFeatures
            )
            Spacer(modifier = Modifier.height(12.dp))
            HelpNavCard(
                icon = Icons.Filled.ManageAccounts,
                title = "Account and data",
                onClick = onOpenFaqAccount
            )
            Spacer(modifier = Modifier.height(12.dp))
            HelpNavCard(
                icon = Icons.Filled.Receipt,
                title = "Payments and pricing",
                onClick = onOpenFaqPayments
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "STILL NEED HELP?",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BartrInkSoft,
                    letterSpacing = 1.sp,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HelpActionCard(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Chat with support",
                onClick = { Toast.makeText(context, "Support chat is available 24/7!", Toast.LENGTH_SHORT).show() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            HelpActionCard(
                icon = Icons.Filled.Call,
                title = "Call support",
                onClick = { Toast.makeText(context, "Support phone: +234 800 000 0000", Toast.LENGTH_LONG).show() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            HelpActionCard(
                icon = Icons.Filled.Email,
                title = "Email us",
                onClick = { Toast.makeText(context, "Email support: support@bartr.app", Toast.LENGTH_LONG).show() }
            )
        }
    }
}

@Composable
private fun HelpNavCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BartrBackdrop)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = BartrBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = BartrInk,
                fontSize = 15.5.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = BartrInkSoft,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun HelpActionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BartrBackdrop)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = BartrBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = BartrInk,
                fontSize = 15.5.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

// ==================== FAQ SCREENS ====================
@Composable
fun FaqDetailScreen(
    title: String,
    items: List<Pair<String, String>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrPageHeader(title = title, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            items.forEachIndexed { index, (question, answer) ->
                var expanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = { expanded = !expanded }
                        )
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = BartrInk,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = BartrInkSoft,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BartrInkSoft,
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            ),
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }

                if (index < items.lastIndex) {
                    HorizontalDivider(color = BartrInk.copy(alpha = 0.08f), thickness = 1.dp)
                }
            }
        }
    }
}

// ==================== ABOUT ====================
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrPageHeader(title = "About", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                BartrNameLogo(
                    fontSize = 32.sp,
                    color = BartrBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "...Let's help you find them.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = BartrInkSoft,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Version 1.0.0 (91896739)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BartrInkSoft.copy(alpha = 0.7f),
                    fontSize = 12.5.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            HelpActionCard(
                icon = Icons.Filled.Star,
                title = "Rate the app",
                onClick = { Toast.makeText(context, "Thanks for supporting Bartr!", Toast.LENGTH_SHORT).show() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            HelpActionCard(
                icon = Icons.Filled.ThumbUp,
                title = "Follow us on social media",
                onClick = { Toast.makeText(context, "@BartrApp on Twitter & Instagram", Toast.LENGTH_SHORT).show() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            HelpActionCard(
                icon = Icons.Filled.Work,
                title = "Careers at Bartr",
                onClick = { Toast.makeText(context, "Check bartr.app/careers for openings", Toast.LENGTH_SHORT).show() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            HelpActionCard(
                icon = Icons.Filled.Description,
                title = "Legal and privacy",
                onClick = { Toast.makeText(context, "Terms of Service & Privacy Policy", Toast.LENGTH_SHORT).show() }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==================== PROFILE ====================
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out of Bartr?", fontWeight = FontWeight.Bold, color = BartrInk) },
            text = { Text("You can sign back in anytime with your phone number.", color = BartrInkSoft) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        Toast.makeText(context, "Logged out (prototype only)", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Log out", color = BartrDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = BartrInk)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?", fontWeight = FontWeight.Bold, color = BartrDanger) },
            text = { Text("This will permanently delete your account and all your data. Continue?", color = BartrInkSoft) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        Toast.makeText(context, "Account deletion submitted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = BartrDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = BartrInk)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrTopBar(
            title = "",
            onBack = onBack,
            trailing = {
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = BartrBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        onClick = onEditProfile
                    )
                )
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(BartrBackdrop),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = BartrInkSoft,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Alex Johnson",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = BartrInk
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "+234 704 200 1836",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = BartrInkSoft,
                        fontSize = 14.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(BartrBackdrop))

            // Email row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = BartrInkSoft,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "alex.johnson@mail.com",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = BartrInk,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { Toast.makeText(context, "Verification email sent!", Toast.LENGTH_SHORT).show() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BartrBlue),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = "Verify", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(BartrBackdrop))

            // Favorite locations
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "FAVORITE LOCATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = BartrInkSoft,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BartrBlueT4),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Home, contentDescription = null, tint = BartrBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Home", style = MaterialTheme.typography.bodyLarge.copy(color = BartrInk, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Set home address", Toast.LENGTH_SHORT).show() },
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BartrBlue),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Add", color = BartrBlue, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = BartrInk.copy(alpha = 0.08f), thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BartrBlueT4),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Work, contentDescription = null, tint = BartrBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Work", style = MaterialTheme.typography.bodyLarge.copy(color = BartrInk, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Set work address", Toast.LENGTH_SHORT).show() },
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BartrBlue),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Add", color = BartrBlue, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(BartrBackdrop))

            // Communication preferences
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { Toast.makeText(context, "Communication preferences updated", Toast.LENGTH_SHORT).show() }
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Communication preferences",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = BartrInk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.5.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = BartrInkSoft,
                    modifier = Modifier.size(14.dp)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(BartrBackdrop))

            // Danger actions
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = { showLogoutDialog = true }
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = BartrDanger, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Log out", color = BartrDanger, fontWeight = FontWeight.Medium, fontSize = 15.5.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = { showDeleteDialog = true }
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = BartrDanger, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Delete account", color = BartrDanger, fontWeight = FontWeight.Medium, fontSize = 15.5.sp)
                }
            }
        }
    }
}

// ==================== EDIT PROFILE ====================
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var firstName by remember { mutableStateOf("Alex") }
    var lastName by remember { mutableStateOf("Johnson") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cancel",
                color = BartrBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false),
                    onClick = onBack
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Edit profile",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BartrInk,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Avatar with edit pencil
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(BartrBackdrop),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = BartrInkSoft, modifier = Modifier.size(44.dp))
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(BartrBlue)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit photo", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form fields
            FormField(label = "First name", value = firstName, onValueChange = { firstName = it })
            Spacer(modifier = Modifier.height(12.dp))
            FormField(label = "Last name", value = lastName, onValueChange = { lastName = it })
            Spacer(modifier = Modifier.height(12.dp))
            FormField(label = "Phone number", value = "+234 704 200 1836", onValueChange = {}, enabled = false)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your phone number can't be changed. If you want to link your account to another phone number, please contact customer support.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BartrInkSoft,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            BartrButtonPrimary(
                text = "Save changes",
                onClick = {
                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    onSave()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { Toast.makeText(context, "Facebook connect is coming soon!", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BartrInk.copy(alpha = 0.15f))
            ) {
                Text(text = "Connect to Facebook", color = BartrInk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We will not post anything without your permission.",
                style = MaterialTheme.typography.bodySmall.copy(color = BartrInkSoft, fontSize = 12.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BartrBackdrop)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = BartrInkSoft,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = if (enabled) BartrInk else BartrInkSoft,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(BartrBlue)
            )
        }
    }
}

// ==================== INVITE A FRIEND ====================
@Composable
fun InviteFriendScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BartrBlue)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .padding(start = 20.dp, top = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
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
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Gift & Friends illustration
            Canvas(modifier = Modifier.size(width = 220.dp, height = 150.dp)) {
                val cw = size.width
                val ch = size.height

                // decorative dots
                drawCircle(Color.White.copy(alpha = 0.5f), radius = 6f, center = androidx.compose.ui.geometry.Offset(cw * 0.25f, ch * 0.25f))
                drawCircle(Color.White.copy(alpha = 0.4f), radius = 4f, center = androidx.compose.ui.geometry.Offset(cw * 0.75f, ch * 0.20f))
                drawCircle(Color.White.copy(alpha = 0.5f), radius = 5f, center = androidx.compose.ui.geometry.Offset(cw * 0.85f, ch * 0.45f))

                // Ground base
                drawRoundRect(
                    Color.White.copy(alpha = 0.25f),
                    topLeft = androidx.compose.ui.geometry.Offset(cw * 0.12f, ch * 0.80f),
                    size = androidx.compose.ui.geometry.Size(cw * 0.76f, 14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
                )

                // Friend 1
                drawCircle(Color(0xFFE1F6FF), radius = 42f, center = androidx.compose.ui.geometry.Offset(cw * 0.36f, ch * 0.45f))
                // Friend 2
                drawCircle(Color.White, radius = 42f, center = androidx.compose.ui.geometry.Offset(cw * 0.64f, ch * 0.45f))

                // Gift Box
                drawRoundRect(
                    Color(0xFFFFD166),
                    topLeft = androidx.compose.ui.geometry.Offset(cw * 0.42f, ch * 0.52f),
                    size = androidx.compose.ui.geometry.Size(38f, 38f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                // Ribbon
                drawRect(
                    Color(0xFFF4A300),
                    topLeft = androidx.compose.ui.geometry.Offset(cw * 0.42f + 14f, ch * 0.52f),
                    size = androidx.compose.ui.geometry.Size(10f, 38f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Better together!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Bartr works best when your circle's on it too. Invite a friend, and get things done faster.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 16.sp,
                    lineHeight = 23.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Button(
                onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "I've been using Bartr to find trusted vendors nearby. Try it out: https://bartr.app")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Invite via")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = BartrBlueS2
                )
            ) {
                Text(
                    text = "Invite a friend",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        color = BartrBlueS2
                    )
                )
            }
        }
    }
}
