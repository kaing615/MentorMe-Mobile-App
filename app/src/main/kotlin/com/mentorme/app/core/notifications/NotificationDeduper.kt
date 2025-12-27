package com.mentorme.app.core.notifications

import com.mentorme.app.data.model.NotificationType

object NotificationDeduper {
    private const val TTL_MS = 2 * 60 * 1000L
    private const val MAX_SIZE = 200
    private val cache = LinkedHashMap<String, Long>(MAX_SIZE, 0.75f, true)

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
        if (cache.containsKey(key)) return false
        cache[key] = now
        trimToSize()
        return true
    }

    private fun cleanup(now: Long) {
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value > TTL_MS) {
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
