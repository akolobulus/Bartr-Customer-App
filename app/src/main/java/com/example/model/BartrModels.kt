package com.example.model

enum class VendorIconType {
    REPAIR,
    BEAUTY,
    MECHANIC
}

data class Vendor(
    val id: String,
    val initial: String,
    val name: String,
    val category: String,
    val iconType: VendorIconType,
    val rating: String,
    val ratingNum: Float,
    val reviews: String,
    val distance: String,
    val response: String,
    val blurb: String,
    val price: String,
    val finalPrice: String,
    val review1: String,
    val review2: String,
    val lat: Double,
    val lng: Double,
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val time: String = "Just now",
)

data class PastRequest(
    val title: String,
    val vendorName: String,
    val status: String,
    val price: String,
    val month: String,
    val iconType: VendorIconType,
)

object BartrRepository {
    val vendors = listOf(
        Vendor(
            id = "chuka",
            initial = "C",
            name = "Chuka's Repairs",
            category = "Phone Repair",
            iconType = VendorIconType.REPAIR,
            rating = "★★★★★ 4.9",
            ratingNum = 4.9f,
            reviews = "128 reviews",
            distance = "0.4km",
            response = "replies in 3m",
            blurb = "Specialists in screen and battery replacement, with same-day turnaround for most repairs.",
            price = "₦4,500 – ₦6,000",
            finalPrice = "₦5,000",
            review1 = "\"Fixed my screen in 20 minutes, honest pricing.\"",
            review2 = "\"Very professional, explained everything clearly.\"",
            lat = 6.6035,
            lng = 3.3540,
        ),
        Vendor(
            id = "adaeze",
            initial = "A",
            name = "Adaeze Nails & Beauty",
            category = "Nail Tech",
            iconType = VendorIconType.BEAUTY,
            rating = "★★★★★ 4.8",
            ratingNum = 4.8f,
            reviews = "94 reviews",
            distance = "0.9km",
            response = "replies in 6m",
            blurb = "Gel, acrylic, and nail art, done in a clean, relaxed space near you.",
            price = "₦3,000 – ₦8,000",
            finalPrice = "₦5,000",
            review1 = "\"My nails always come out exactly how I picture them.\"",
            review2 = "\"Booked in minutes, no long wait at all.\"",
            lat = 6.6000,
            lng = 3.3490,
        ),
        Vendor(
            id = "musa",
            initial = "M",
            name = "Musa Auto Care",
            category = "Mechanic",
            iconType = VendorIconType.MECHANIC,
            rating = "★★★★★ 4.7",
            ratingNum = 4.7f,
            reviews = "201 reviews",
            distance = "1.2km",
            response = "replies in 10m",
            blurb = "General auto repairs and diagnostics, with an honest, upfront quote before any work starts.",
            price = "₦8,000 – ₦25,000",
            finalPrice = "₦12,000",
            review1 = "\"Told me exactly what was wrong before touching the car.\"",
            review2 = "\"Fair pricing, fast turnaround.\"",
            lat = 6.5985,
            lng = 3.3560,
        ),
        Vendor(
            id = "ifeoma",
            initial = "I",
            name = "Ifeoma Tech Fix",
            category = "Phone Repair",
            iconType = VendorIconType.REPAIR,
            rating = "★★★★★ 4.7",
            ratingNum = 4.7f,
            reviews = "76 reviews",
            distance = "0.9km",
            response = "replies in 8m",
            blurb = "Fast, reliable phone and tablet repairs, with genuine parts and a short warranty on every job.",
            price = "₦4,000 – ₦6,500",
            finalPrice = "₦5,200",
            review1 = "\"Replaced my battery same day, works like new.\"",
            review2 = "\"Clear about pricing from the start.\"",
            lat = 6.6020,
            lng = 3.3555,
        ),
        Vendor(
            id = "bode",
            initial = "B",
            name = "Bode's Gadget Clinic",
            category = "Phone Repair",
            iconType = VendorIconType.REPAIR,
            rating = "★★★★☆ 4.5",
            ratingNum = 4.5f,
            reviews = "52 reviews",
            distance = "1.6km",
            response = "replies in 12m",
            blurb = "Budget-friendly repairs for phones, laptops and small electronics.",
            price = "₦3,500 – ₦5,500",
            finalPrice = "₦4,200",
            review1 = "\"Good value, took a little longer than expected.\"",
            review2 = "\"Solved a problem two other shops couldn't.\"",
            lat = 6.5995,
            lng = 3.3500,
        )
    )

    fun getVendor(id: String): Vendor = vendors.firstOrNull { it.id == id } ?: vendors[0]

    val pastRequests = listOf(
        PastRequest(
            title = "Phone screen repair",
            vendorName = "Chuka's Repairs",
            status = "Completed",
            price = "₦5,000",
            month = "Sept 2026",
            iconType = VendorIconType.REPAIR
        ),
        PastRequest(
            title = "Gel manicure",
            vendorName = "Adaeze Nails & Beauty",
            status = "In progress",
            price = "₦6,500",
            month = "Sept 2026",
            iconType = VendorIconType.BEAUTY
        ),
        PastRequest(
            title = "Engine diagnostic",
            vendorName = "Musa Auto Care",
            status = "Completed",
            price = "₦15,000",
            month = "Aug 2026",
            iconType = VendorIconType.MECHANIC
        ),
        PastRequest(
            title = "Battery replacement",
            vendorName = "Ifeoma Tech Fix",
            status = "Completed",
            price = "₦4,500",
            month = "Aug 2026",
            iconType = VendorIconType.REPAIR
        )
    )

    val faqFeaturesList = listOf(
        "How to add a promo code" to "Open the menu drawer, tap Promotions, enter your promo code and tap Apply. The promo will automatically be deducted from your next service.",
        "How to describe a request by voice" to "On the 'What do you need?' screen, tap the microphone button in the center. Speak clearly in English or Pidgin, and Bartr will instantly parse your request into an actionable job.",
        "How to get a price estimate" to "Whenever you enter a request, Bartr analyzes current market rates across Lagos and suggests an upfront fair price range before matching you with artisans.",
        "How to cancel a request" to "While the vendor is en route, tap 'Cancel request' on the job status card. Your artisan will be notified right away.",
        "How to rate a vendor" to "After your service is complete, tap 'Complete job & rate' to leave a 1 to 5 star rating and optional comments.",
        "Managing app notifications" to "Manage arrival reminders, message alerts, and receipt delivery in Android App Settings > Notifications > Bartr.",
        "How to change app language" to "Bartr supports English, Nigerian Pidgin, Yoruba, Igbo, and Hausa. You can toggle language options under Account preferences.",
        "Downloading the Bartr app" to "Bartr is available as a native Android app on Google Play with offline-ready local cache support."
    )

    val faqAccountList = listOf(
        "I can't sign in to my account" to "Check your network connection and verify your phone number receives OTP SMS. You can also contact support for account recovery.",
        "Become a vendor on Bartr" to "Open the drawer and tap 'Become a vendor' at the bottom to start our artisan verification, identity vetting, and onboarding flow.",
        "Why is my account restricted?" to "Accounts may be temporarily placed under review if repeated cancellations or safety flags are triggered. Contact support to resolve.",
        "Edit my account information" to "From the menu drawer, tap your profile avatar, then tap 'Edit' in the top right to update your name, photo, and preferences.",
        "Receiving unwanted marketing messages" to "You can easily opt out of promotional messages under Profile > Communication preferences.",
        "Requesting a copy of my Bartr data" to "To receive an export of your account activity, past receipts, and profile data, email privacy@bartr.app.",
        "Delete my account" to "You can permanently delete your account and associated history from the Profile screen by tapping 'Delete account'."
    )

    val faqPaymentsList = listOf(
        "Vendor asked for cash outside the app" to "Cash on completion is our supported default method! Simply hand the agreed cash amount directly to your vendor once the job is completed and inspected.",
        "How to get a receipt" to "Every completed job generates an itemized digital receipt accessible anytime under 'My requests' in the menu drawer.",
        "I can't find my receipt" to "Navigate to 'My requests' to browse historical service requests organized by month and download previous invoices.",
        "I didn't receive my refund" to "If an electronic payment was processed for a cancelled service, bank card reversals typically settle within 1-3 business days.",
        "Price higher than expected" to "Bartr's fair price benchmark helps ensure you are never overcharged. If an artisan asks for an unexpected surcharge, you can decline or contact support.",
        "Report unknown charges" to "If you notice an unrecognized charge, please report it immediately to support@bartr.app or call our 24/7 hotline at +234 800 000 0000.",
        "Adding, changing or removing a payment method" to "Manage your payment options under Menu > Payments. You can switch between Cash on completion and saved debit cards.",
        "What is fair-price guidance" to "Fair-price guidance is Bartr's algorithmic benchmark calculating true local artisan rates in Lagos to eliminate haggling and price gouging."
    )
}
