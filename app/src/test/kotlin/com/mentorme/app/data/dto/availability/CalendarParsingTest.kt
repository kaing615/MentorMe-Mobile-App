package com.mentorme.app.data.dto.availability

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CalendarParsingTest {
    private val gson = Gson()

    @Test
    fun `calendar item slot parses as string id`() {
        val json = """
            {
              "success": true,
              "message": null,
              "data": {
                "items": [
                  {
                    "_id": "68f8d440…",
                    "slot": "68f8d43f…",
                    "start": "2025-11-20T06:13:00.000Z",
                    "end": "2025-11-20T07:25:00.000Z",
                    "status": "open",
                    "title": "[type=video] Trial",
                    "description": null
                  }
                ]
              }
            }
        """.trimIndent()

        val type = object : TypeToken<ApiEnvelope<CalendarPayload>>() {}.type
        val envelope: ApiEnvelope<CalendarPayload> = gson.fromJson(json, type)
        val items = envelope.data?.items
        assertNotNull(items)
        assertEquals(1, items!!.size)
        assertEquals("68f8d43f…", items[0].slot)
    }
}

