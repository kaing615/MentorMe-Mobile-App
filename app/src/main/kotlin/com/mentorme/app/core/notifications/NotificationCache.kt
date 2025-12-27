package com.mentorme.app.core.notifications

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mentorme.app.data.model.NotificationItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class NotificationCache @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val gson = Gson()
    private val cacheKey = stringPreferencesKey("notification_cache")

    suspend fun load(): List<NotificationItem> {
        val json = dataStore.data.first()[cacheKey]
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<NotificationItem>>() {}.type
            gson.fromJson<List<NotificationItem>>(json, type) ?: emptyList()
        }.getOrElse { emptyList() }
    }

    suspend fun save(items: List<NotificationItem>, maxItems: Int = 100) {
        val trimmed = items.sortedByDescending { it.timestamp }.take(maxItems)
        dataStore.edit { prefs ->
            prefs[cacheKey] = gson.toJson(trimmed)
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(cacheKey)
        }
    }
}
