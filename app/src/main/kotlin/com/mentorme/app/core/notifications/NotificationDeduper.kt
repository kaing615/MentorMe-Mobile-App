package com.mentorme.app.core.notifications

import com.mentorme.app.data.model.NotificationType

object NotificationDeduper {
    private const val DEFAULT_TTL_MS = 2 * 60 * 1000L // 2 minutes
    private const val REMINDER_TTL_MS = 5 * 60 * 1000L // 5 minutes for reminders
    private const val MAX_SIZE = 200
    private val cache = LinkedHashMap<String, CacheEntry>(MAX_SIZE, 0.75f, true)

    private data class CacheEntry(val timestamp: Long, val type: NotificationType)

    @Synchronized
    fun shouldNotify(id: String?, title: String, body: String, type: NotificationType): Boolean {
        val key = if (!id.isNullOrBlank()) {
            "id:$id"
        } else {
            val safeTitle = title.trim()
            val safeBody = body.trim()
            "payload:${type.name}|$safeTitle|$safeBody"
        }
        val now = System.currentTimeMillis()
        cleanup(now)
        
        // Check if we should throttle based on type-specific TTL
        val existing = cache[key]
        if (existing != null) {
            val ttl = if (existing.type == NotificationType.BOOKING_REMINDER) {
                REMINDER_TTL_MS
            } else {
                DEFAULT_TTL_MS
            }
            if (now - existing.timestamp < ttl) {
                return false
            }
        }
        
        cache[key] = CacheEntry(now, type)
        trimToSize()
        return true
    }

    private fun cleanup(now: Long) {
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val ttl = if (entry.value.type == NotificationType.BOOKING_REMINDER) {
                REMINDER_TTL_MS
            } else {
                DEFAULT_TTL_MS
            }
            if (now - entry.value.timestamp > ttl) {
                iterator.remove()
            }
        }
    }

    private fun trimToSize() {
        while (cache.size > MAX_SIZE) {
            val iterator = cache.entries.iterator()
            if (iterator.hasNext()) {
                iterator.next()
                iterator.remove()
            } else {
                return
            }
        }
    }
}
