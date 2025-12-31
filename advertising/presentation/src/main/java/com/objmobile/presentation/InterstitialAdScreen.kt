package com.objmobile.presentation

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.objmobile.domain.AdvertisingConfiguration

class InterstitialAdScreen(
    private val activity: Activity, private val configuration: AdvertisingConfiguration
) {
    var interstitialAd: InterstitialAd? = null

    init {
        load()
    }

    private fun load() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            if (configuration.isDebug) configuration.advertisingUnit.adUnitDebugId else configuration.advertisingUnit.adUnitReleaseId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("InterstitialAdScreen", "onAdFailedToLoad: ${adError.message}")
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
            })
    }

    fun show() {
        if (interstitialAd == null) {
            load()
        } else {
            interstitialAd?.show(activity)
        }
    }
}