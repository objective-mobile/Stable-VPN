package com.objmobile.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.objmobile.domain.AdvertisingConfiguration
import com.objmobile.domain.FirstStartLogic

object AdUnitIds {
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
}

@Composable
fun AdvertisingSection(
    configuration: AdvertisingConfiguration,
    bannerAdUnitId: String = AdUnitIds.BANNER_AD_UNIT_ID,
    interstitialAdUnitId: String = AdUnitIds.INTERSTITIAL_AD_UNIT_ID,
    onAdShown: () -> Unit = {},
    onAdFailedToLoad: () -> Unit = {},
    onAdDismissed: () -> Unit = {},
    modifier: Modifier = Modifier,
    showBanner: Boolean = true,
    showInterstitial: Boolean = true,
    interstitialButtonText: String = "Show Interstitial Ad"
) {
    if (!configuration.enableAd) return
    val shouldShow = when (configuration.firstStartLogic) {
        FirstStartLogic.SHOW -> true
        FirstStartLogic.HIDE -> configuration.showing
    }

    if (!shouldShow) return

    Column(modifier = modifier.fillMaxWidth()) {
        if (showBanner) {
            BannerAd(
                adUnitId = bannerAdUnitId,
                configuration = configuration,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showBanner && showInterstitial) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showInterstitial) {
            InterstitialAd(
                adUnitId = interstitialAdUnitId,
                configuration = configuration,
                onAdShown = onAdShown,
                onAdFailedToLoad = onAdFailedToLoad,
                onAdDismissed = onAdDismissed,
                buttonText = interstitialButtonText,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}