package com.objective.vpn.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.objmobile.vpn.domain.VpnStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VpnConnectionStorage private constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {
    private val STATUS_KEY = stringPreferencesKey("vpnStatus")
    suspend fun saveVpnStatus(vpnStatus: VpnStatusData) {
        dataStore.edit { preferences ->
            preferences[STATUS_KEY] = gson.toJson(vpnStatus)
        }
    }

    fun getVpnStatus(): Flow<VpnStatusData> {
        return dataStore.data.map { preferences ->
            gson.fromJson(
                preferences[STATUS_KEY], VpnStatusData::class.java
            ) ?: VpnStatusData(VpnStatus.Disconnected)
        }
    }

    companion object {
        private var vpnConnectionStorage: VpnConnectionStorage? = null
        fun newInstance(context: Context): VpnConnectionStorage {
            if (vpnConnectionStorage == null) {
                vpnConnectionStorage = VpnConnectionStorage(context.dataStore, Gson())
            }
            return vpnConnectionStorage ?: throw IllegalStateException("No VpnConnectionStorage");
        }
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")