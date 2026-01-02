package com.objmobile.countries.data.mapper

import com.objmobile.countries.data.model.CountryBaseModel
import com.objmobile.countries.domain.Country

/**
 * Mapper for converting between data and domain models.
 * Following Clean Architecture - data layer handles mapping to domain models.
 */
object CountryMapper {
    /**
     * Converts CountryBaseModel to domain Country.
     * @param baseModel The data model from Firestore
     * @return Domain Country model
     */
    fun toDomain(baseModel: CountryBaseModel): Country {
        return Country(
            countryName = baseModel.countryName, countryConfig = baseModel.countryConfig
        )
    }
}