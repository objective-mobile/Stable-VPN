package com.objmobile.presentation.mapper

import com.objmobile.presentation.R

/**
 * Maps country names to their corresponding flag drawable resources
 */
object CountryFlagMapper {
    /**
     * Get flag drawable resource ID for a country name
     * Based on FlagKit designs converted to Android vector drawables
     * Includes European countries, USA, Canada, and Russia
     * @param countryName The name of the country
     * @return Drawable resource ID for the country flag
     */
    fun getFlagResource(countryName: String): Int {
        return when (countryName.trim().lowercase()) { // North America
            "united states", "usa", "us", "america" -> R.drawable.us
            "canada" -> R.drawable.ca // Europe - Western
            "united kingdom", "uk", "britain", "great britain", "england" -> R.drawable.gb
            "germany", "deutschland" -> R.drawable.de
            "france" -> R.drawable.fr
            "netherlands", "holland" -> R.drawable.nl
            "belgium" -> R.drawable.be
            "luxembourg" -> R.drawable.lu
            "switzerland" -> R.drawable.ch
            "austria" -> R.drawable.at
            "liechtenstein" -> R.drawable.li
            "monaco" -> R.drawable.mc
            "andorra" -> R.drawable.ad
            "san marino" -> R.drawable.sm
            "vatican", "vatican city" -> R.drawable.va // Europe - Northern
            "sweden" -> R.drawable.se
            "norway" -> R.drawable.no
            "denmark" -> R.drawable.dk
            "finland" -> R.drawable.fi
            "iceland" -> R.drawable.`is` // Europe - Southern
            "spain" -> R.drawable.es
            "portugal" -> R.drawable.pt
            "italy" -> R.drawable.it
            "greece" -> R.drawable.gr
            "malta" -> R.drawable.mt
            "cyprus" -> R.drawable.cy // Europe - Eastern
            "poland" -> R.drawable.pl
            "czech republic", "czechia" -> R.drawable.cz
            "slovakia" -> R.drawable.sk
            "hungary" -> R.drawable.hu
            "slovenia" -> R.drawable.si
            "croatia" -> R.drawable.hr
            "bosnia and herzegovina", "bosnia" -> R.drawable.ba
            "serbia" -> R.drawable.rs
            "montenegro" -> R.drawable.me
            "albania" -> R.drawable.al
            "north macedonia", "macedonia" -> R.drawable.mk
            "kosovo" -> R.drawable.xk
            "romania" -> R.drawable.ro
            "bulgaria" -> R.drawable.bg
            "moldova" -> R.drawable.md
            "ukraine" -> R.drawable.ua
            "belarus" -> R.drawable.by
            "lithuania" -> R.drawable.lt
            "latvia" -> R.drawable.lv
            "estonia" -> R.drawable.ee
            "russia", "russian federation" -> R.drawable.ru // Europe - Transcontinental
            "turkey" -> R.drawable.tr
            "georgia" -> R.drawable.ge
            "armenia" -> R.drawable.am
            "azerbaijan" -> R.drawable.az // Ireland
            "ireland" -> R.drawable.ie // European Union
            "european union", "eu" -> R.drawable.eu // Fallback
            "unknown", "", "local network", "europe", "asia" -> R.drawable.flag_unknown
            else -> R.drawable.flag_unknown
        }
    }
    /**
     * Get flag emoji for a country name (alternative to drawable)
     * @param countryName The name of the country
     * @return Flag emoji string
     */
    /**
     * Check if a country has a custom flag drawable
     * @param countryName The name of the country
     * @return true if custom flag exists, false otherwise
     */
    fun hasCustomFlag(countryName: String): Boolean {
        return getFlagResource(countryName) != R.drawable.flag_unknown
    }
}