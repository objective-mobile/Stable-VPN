package com.objmobile.countries.data.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.objmobile.countries.data.model.CountryBaseModel
import kotlinx.coroutines.tasks.await

/**
 * Firestore data source for country operations.
 * Handles direct communication with Firebase Firestore.
 */
class FirestoreCountryDataSource(
    private val firestore: FirebaseFirestore
) {
    /**
     * Fetches all countries from Firestore.
     * @return List of CountryBaseModel from Firestore
     */
    suspend fun fetchCountries(): List<CountryBaseModel> {
        return try {
            val snapshot = firestore.collection(CountryBaseModel.COLLECTION_NAME).get().await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(CountryBaseModel::class.java)
            }
        } catch (e: Exception) {
            Log.d(
                "FirestoreDataSource",
                "fetchCountries: ${e.message}"
            ) // Log error and return empty list
            emptyList()
        }
    }

    /**
     * Fetches a specific country by name from Firestore.
     * @param countryName The name of the country to fetch
     * @return CountryBaseModel or null if not found
     */
    suspend fun fetchCountryByName(countryName: String): CountryBaseModel? {
        return try {
            val snapshot = firestore.collection(CountryBaseModel.COLLECTION_NAME)
                .whereEqualTo(CountryBaseModel.FIELD_COUNTRY_NAME, countryName).limit(1).get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(CountryBaseModel::class.java)
        } catch (e: Exception) { // Log error and return null
            null
        }
    }
}