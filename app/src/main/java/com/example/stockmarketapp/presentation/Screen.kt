package com.example.stockmarketapp.presentation

sealed class Screen(val route: String) {
    object CompanyListingScreen: Screen("company_listing_screen")
    object CompanyDetailScreen: Screen("company_detail_screen")
}
