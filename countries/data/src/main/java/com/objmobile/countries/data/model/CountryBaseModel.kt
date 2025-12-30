package com.objmobile.countries.data.model

/**
 * Data model for Country entity in Firestore.
 * Following architecture rules - data models use BaseModel suffix.
 */
data class CountryBaseModel(
    val countryName: String = "", val countryConfig: String = ""
) {
    companion object {
        const val COLLECTION_NAME = "countries"
        const val FIELD_COUNTRY_NAME = "countryName"
        const val FIELD_COUNTRY_CONFIG = "countryConfig"
    }
}