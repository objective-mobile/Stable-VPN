package com.objmobile.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.objmobile.domain.AdvertisingConfiguration

@Composable
fun BannerAd(
    configuration: AdvertisingConfiguration, modifier: Modifier = Modifier
) {
    if (!configuration.enableAd || !configuration.showing) {
        return
    }
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId =
                if (configuration.isDebug) configuration.advertisingUnit.adUnitDebugId else configuration.advertisingUnit.adUnitReleaseId
        }
    }

    DisposableEffect(adView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView }, modifier = modifier.fillMaxWidth()
    )
}