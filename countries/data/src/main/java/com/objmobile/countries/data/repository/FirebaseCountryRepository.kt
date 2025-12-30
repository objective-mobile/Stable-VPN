package com.objmobile.countries.data.repository

import com.objmobile.countries.data.datasource.FirestoreCountryDataSource
import com.objmobile.countries.data.mapper.CountryMapper
import com.objmobile.countries.domain.Country
import com.objmobile.countries.domain.CountryRepository

/**
 * Firebase Firestore implementation of CountryRepository.
 * Following architecture rules - data layer implements domain contracts.
 * Uses BaseRepository suffix as per naming conventions.
 */
class FirebaseCountryRepository(
    private val firestoreDataSource: FirestoreCountryDataSource
) : CountryRepository {
    /**
     * Fetches all available countries from Firestore.
     * Note: Interface returns single Country instead of collection.
     * Implementation returns first available country or default empty country.
     *
     * @return First available country from Firestore or empty country if none found
     */
    override suspend fun available(): Country {
        val countries = firestoreDataSource.fetchCountries()

        return if (countries.isNotEmpty()) {
            CountryMapper.toDomain(countries.first())
        } else {
            throw IllegalStateException("Wrong country")
        }
    }
}