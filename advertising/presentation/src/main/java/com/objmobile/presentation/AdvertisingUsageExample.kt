package com.objmobile.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.objmobile.domain.AdvertisingConfiguration
import com.objmobile.domain.FirstStartLogic

@Composable
fun AdvertisingExampleScreen(
    configuration: AdvertisingConfiguration = AdvertisingConfiguration(),
    modifier: Modifier = Modifier
) {
    var adShownCount by remember { mutableIntStateOf(0) }

    Surface(
        modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Advertising Example",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Interstitial ads shown: $adShownCount",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AdvertisingSection(
                configuration = configuration,
                onAdShown = {
                    adShownCount++
                },
                onAdFailedToLoad = {},
                onAdDismissed = {},
                interstitialButtonText = "Show Interstitial Ad"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvertisingExamplePreview() {
    MaterialTheme {
        AdvertisingExampleScreen(
            configuration = AdvertisingConfiguration(
                enableAd = true, firstStartLogic = FirstStartLogic.SHOW, showing = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdvertisingDisabledPreview() {
    MaterialTheme {
        AdvertisingExampleScreen(
            configuration = AdvertisingConfiguration(
                enableAd = false, firstStartLogic = FirstStartLogic.HIDE, showing = false
            )
        )
    }
}