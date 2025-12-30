package com.objmobile.domain

data class AdvertisingConfiguration(
    val enableAd: Boolean = true,
    val firstStartLogic: FirstStartLogic = FirstStartLogic.SHOW,
    val showing: Boolean = true
)

enum class FirstStartLogic {
    SHOW, HIDE
}