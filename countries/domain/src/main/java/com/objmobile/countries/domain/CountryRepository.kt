package com.objmobile.countries.domain

/**
 * Repository interface for country data operations.
 * Following Clean Architecture - domain defines contracts, data implements them.
 */
interface CountryRepository {
    /**
     * Fetches all available countries.
     * @return Countries collection containing all available countries
     */
    suspend fun available(): Country
}