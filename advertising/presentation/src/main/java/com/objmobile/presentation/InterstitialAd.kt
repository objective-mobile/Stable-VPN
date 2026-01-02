package com.objmobile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.objmobile.domain.AdvertisingConfiguration

@Composable
fun InterstitialAd(
    configuration: AdvertisingConfiguration,
    onAdShown: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
    onAdDismissed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (!configuration.enableAd || !configuration.showing) {
        return
    }
    val context = LocalContext.current
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isAdReady by remember { mutableStateOf(false) }
    fun loadInterstitialAd() {
        if (isLoading) return

        isLoading = true
        isAdReady = false
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            if (configuration.isDebug) configuration.advertisingUnit.adUnitDebugId else configuration.advertisingUnit.adUnitReleaseId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                    onAdFailedToLoad()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                    isAdReady = true

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            onAdShown()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            isAdReady = false
                            onAdDismissed()
                            loadInterstitialAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            interstitialAd = null
                            isAdReady = false
                            onAdFailedToLoad()
                        }
                    }
                }
            })
    }

    DisposableEffect(Unit) {
        loadInterstitialAd()
        if (context is androidx.activity.ComponentActivity) {
            interstitialAd?.show(context)
        }
        onDispose {
            interstitialAd = null
        }
    }
}