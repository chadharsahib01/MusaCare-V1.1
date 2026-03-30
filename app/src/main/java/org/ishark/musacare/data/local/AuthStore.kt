package org.ishark.musacare.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore("auth_store")

@Singleton
class AuthStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pinHashKey = stringPreferencesKey("pin_hash")

    suspend fun getPinHash(): String? = context.authDataStore.data.map { it[pinHashKey] }.first()

    suspend fun savePinHash(hash: String) {
        context.authDataStore.edit { prefs -> prefs[pinHashKey] = hash }
    }
}
