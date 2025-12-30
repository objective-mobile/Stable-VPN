package com.objmobile.countries.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.objmobile.countries.data.datasource.FirestoreCountryDataSource
import com.objmobile.countries.data.repository.FirebaseCountryRepository
import com.objmobile.countries.domain.CountryRepository

/**
 * Dependency injection module for country data layer.
 * Following project's manual DI approach without frameworks.
 */
object CountryDataModule {
    /**
     * Provides FirestoreCountryDataSource instance.
     * @param firestore Firebase Firestore instance
     * @return Configured FirestoreCountryDataSource
     */
    fun provideFirestoreCountryDataSource(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ): FirestoreCountryDataSource {
        return FirestoreCountryDataSource(firestore)
    }

    /**
     * Provides CountryRepository implementation.
     * @param firestoreDataSource Firestore data source dependency
     * @return CountryBaseRepository implementation
     */
    fun provideCountryRepository(
        firestoreDataSource: FirestoreCountryDataSource = provideFirestoreCountryDataSource()
    ): CountryRepository {
        return FirebaseCountryRepository(firestoreDataSource)
    }
}