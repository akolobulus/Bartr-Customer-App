package com.example.navigation

sealed interface BartrScreen {
    object Splash : BartrScreen
    object Home : BartrScreen
    object Request : BartrScreen
    data class Parsed(val query: String) : BartrScreen
    data class Matching(val query: String) : BartrScreen
    object Matches : BartrScreen
    data class VendorProfile(val vendorId: String) : BartrScreen
    data class Sending(val vendorId: String) : BartrScreen
    data class JobStatus(val vendorId: String) : BartrScreen
    data class Chat(val vendorId: String) : BartrScreen
    data class Rating(val vendorId: String) : BartrScreen
    object Payments : BartrScreen
    object Promotions : BartrScreen
    object MyRequests : BartrScreen
    object SavedVendors : BartrScreen
    object GetHelp : BartrScreen
    object FaqFeatures : BartrScreen
    object FaqAccount : BartrScreen
    object FaqPayments : BartrScreen
    object About : BartrScreen
    object Profile : BartrScreen
    object EditProfile : BartrScreen
    object InviteFriend : BartrScreen
}
