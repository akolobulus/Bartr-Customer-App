package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.model.BartrRepository
import com.example.navigation.BartrScreen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.FaqDetailScreen
import com.example.ui.screens.GetHelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InviteFriendScreen
import com.example.ui.screens.JobStatusScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.MatchingScreen
import com.example.ui.screens.MyRequestsScreen
import com.example.ui.screens.ParsedScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PromotionsScreen
import com.example.ui.screens.RatingScreen
import com.example.ui.screens.RequestInputScreen
import com.example.ui.screens.SavedVendorsScreen
import com.example.ui.screens.SendingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VendorProfileScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BartrApp()
            }
        }
    }
}

@Composable
fun BartrApp() {
    val screenStack = remember { mutableStateListOf<BartrScreen>(BartrScreen.Splash) }
    val currentScreen = screenStack.lastOrNull() ?: BartrScreen.Home

    fun goTo(screen: BartrScreen) {
        screenStack.add(screen)
    }

    fun goBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
        }
    }

    fun replaceTop(screen: BartrScreen) {
        if (screenStack.isNotEmpty()) {
            screenStack.removeAt(screenStack.lastIndex)
        }
        screenStack.add(screen)
    }

    fun resetToHome() {
        screenStack.clear()
        screenStack.add(BartrScreen.Home)
    }

    BackHandler(enabled = screenStack.size > 1 || currentScreen !is BartrScreen.Home) {
        if (screenStack.size > 1) {
            goBack()
        } else if (currentScreen !is BartrScreen.Home) {
            resetToHome()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { screen ->
            when (screen) {
                is BartrScreen.Splash -> {
                    SplashScreen(
                        onSplashFinished = {
                            replaceTop(BartrScreen.Home)
                        }
                    )
                }

                is BartrScreen.Home -> {
                    HomeScreen(
                        onSearchClick = { goTo(BartrScreen.Request) },
                        onVendorClick = { vendor ->
                            goTo(BartrScreen.VendorProfile(vendor.id))
                        },
                        onOpenProfile = { goTo(BartrScreen.Profile) },
                        onOpenPayments = { goTo(BartrScreen.Payments) },
                        onOpenPromotions = { goTo(BartrScreen.Promotions) },
                        onOpenMyRequests = { goTo(BartrScreen.MyRequests) },
                        onOpenSavedVendors = { goTo(BartrScreen.SavedVendors) },
                        onOpenInviteFriend = { goTo(BartrScreen.InviteFriend) },
                        onOpenGetHelp = { goTo(BartrScreen.GetHelp) },
                        onOpenAbout = { goTo(BartrScreen.About) },
                        onRequestVendorDirect = { vendorId ->
                            goTo(BartrScreen.Sending(vendorId))
                        },
                        onOpenChatDirect = { vendorId ->
                            goTo(BartrScreen.Chat(vendorId))
                        },
                        onSearchMatchesDirect = { query ->
                            goTo(BartrScreen.Parsed(query))
                        }
                    )
                }

                is BartrScreen.Request -> {
                    RequestInputScreen(
                        onBack = { goBack() },
                        onFindVendors = { query ->
                            goTo(BartrScreen.Parsed(query))
                        }
                    )
                }

                is BartrScreen.Parsed -> {
                    ParsedScreen(
                        query = screen.query,
                        onBack = { goBack() },
                        onConfirm = {
                            goTo(BartrScreen.Matching(screen.query))
                        }
                    )
                }

                is BartrScreen.Matching -> {
                    MatchingScreen(
                        query = screen.query,
                        onMatched = {
                            replaceTop(BartrScreen.Matches)
                        }
                    )
                }

                is BartrScreen.Matches -> {
                    MatchesScreen(
                        onBack = { goBack() },
                        onVendorClick = { vendor ->
                            goTo(BartrScreen.VendorProfile(vendor.id))
                        }
                    )
                }

                is BartrScreen.VendorProfile -> {
                    val vendor = BartrRepository.getVendor(screen.vendorId)
                    VendorProfileScreen(
                        vendor = vendor,
                        onBack = { goBack() },
                        onOpenChat = {
                            goTo(BartrScreen.Chat(vendor.id))
                        },
                        onRequestVendor = {
                            goTo(BartrScreen.Sending(vendor.id))
                        }
                    )
                }

                is BartrScreen.Sending -> {
                    SendingScreen(
                        onSent = {
                            replaceTop(BartrScreen.JobStatus(screen.vendorId))
                        }
                    )
                }

                is BartrScreen.JobStatus -> {
                    val vendor = BartrRepository.getVendor(screen.vendorId)
                    JobStatusScreen(
                        vendor = vendor,
                        onBack = { goBack() },
                        onOpenChat = {
                            goTo(BartrScreen.Chat(vendor.id))
                        },
                        onCancel = {
                            resetToHome()
                        },
                        onCompleteJob = {
                            replaceTop(BartrScreen.Rating(vendor.id))
                        }
                    )
                }

                is BartrScreen.Chat -> {
                    val vendor = BartrRepository.getVendor(screen.vendorId)
                    ChatScreen(
                        vendor = vendor,
                        onBack = { goBack() }
                    )
                }

                is BartrScreen.Rating -> {
                    val vendor = BartrRepository.getVendor(screen.vendorId)
                    RatingScreen(
                        vendor = vendor,
                        onSubmit = {
                            resetToHome()
                        }
                    )
                }

                is BartrScreen.Payments -> {
                    PaymentsScreen(onBack = { goBack() })
                }

                is BartrScreen.Promotions -> {
                    PromotionsScreen(
                        onBack = { goBack() },
                        onInviteFriend = { goTo(BartrScreen.InviteFriend) }
                    )
                }

                is BartrScreen.MyRequests -> {
                    MyRequestsScreen(onBack = { goBack() })
                }

                is BartrScreen.SavedVendors -> {
                    SavedVendorsScreen(
                        onBack = { goBack() },
                        onVendorClick = { vendor ->
                            goTo(BartrScreen.VendorProfile(vendor.id))
                        }
                    )
                }

                is BartrScreen.GetHelp -> {
                    GetHelpScreen(
                        onBack = { goBack() },
                        onOpenFaqFeatures = { goTo(BartrScreen.FaqFeatures) },
                        onOpenFaqAccount = { goTo(BartrScreen.FaqAccount) },
                        onOpenFaqPayments = { goTo(BartrScreen.FaqPayments) }
                    )
                }

                is BartrScreen.FaqFeatures -> {
                    FaqDetailScreen(
                        title = "App and features",
                        items = BartrRepository.faqFeaturesList,
                        onBack = { goBack() }
                    )
                }

                is BartrScreen.FaqAccount -> {
                    FaqDetailScreen(
                        title = "Account and data",
                        items = BartrRepository.faqAccountList,
                        onBack = { goBack() }
                    )
                }

                is BartrScreen.FaqPayments -> {
                    FaqDetailScreen(
                        title = "Payments and pricing",
                        items = BartrRepository.faqPaymentsList,
                        onBack = { goBack() }
                    )
                }

                is BartrScreen.About -> {
                    AboutScreen(onBack = { goBack() })
                }

                is BartrScreen.Profile -> {
                    ProfileScreen(
                        onBack = { goBack() },
                        onEditProfile = { goTo(BartrScreen.EditProfile) }
                    )
                }

                is BartrScreen.EditProfile -> {
                    EditProfileScreen(
                        onBack = { goBack() },
                        onSave = { goBack() }
                    )
                }

                is BartrScreen.InviteFriend -> {
                    InviteFriendScreen(onBack = { goBack() })
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Bartr")
    }
}
