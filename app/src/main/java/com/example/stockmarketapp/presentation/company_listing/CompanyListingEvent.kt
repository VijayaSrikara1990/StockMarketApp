package com.example.stockmarketapp.presentation.company_listing

sealed class CompanyListingEvent {
    object Refresh: CompanyListingEvent()
    data class OnSearchQueryChanged(val query: String): CompanyListingEvent()
}
