// core/src/main/java/core/datastore/TokenManager.kt
package datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    val authToken: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_AUTH_TOKEN] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_AUTH_TOKEN)
        }
    }
}
