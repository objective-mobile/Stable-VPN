package com.objmobile.domain

data class AdvertisingConfiguration(
    val enableAd: Boolean = true,
    val firstStartLogic: FirstStartLogic = FirstStartLogic.SHOW,
    val showing: Boolean = true,
    val isDebug: Boolean = true,
    val advertisingUnit: AdvertisingUnit
)

data class AdvertisingUnit(
    val adUnitReleaseId: String,
    val adUnitDebugId: String = "ca-app-pub-3940256099942544/6300978111"
)

enum class FirstStartLogic {
    SHOW, HIDE
}