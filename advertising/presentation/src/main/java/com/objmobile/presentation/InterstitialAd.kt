package com.objmobile.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.objmobile.domain.AdvertisingConfiguration

@Composable
fun InterstitialAd(
    adUnitId: String,
    configuration: AdvertisingConfiguration,
    onAdShown: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
    onAdDismissed: () -> Unit = {},
    modifier: Modifier = Modifier,
    buttonText: String = "Show Ad"
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
            context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
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
        onDispose {
            interstitialAd = null
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                interstitialAd?.let { ad ->
                    if (context is androidx.activity.ComponentActivity) {
                        ad.show(context)
                    }
                } ?: loadInterstitialAd()
            }, enabled = isAdReady && !isLoading, modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = when {
                    isLoading -> "Loading..."
                    isAdReady -> buttonText
                    else -> "Load Ad"
                }
            )
        }
    }
}