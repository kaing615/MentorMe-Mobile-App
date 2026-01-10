package com.mentorme.app.domain.usecase

import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.core.utils.Logx
import com.mentorme.app.data.remote.MentorMeApi
import com.mentorme.app.data.mapper.toUiMentorsFromCards
import javax.inject.Inject
import com.mentorme.app.ui.home.Mentor as UiMentor

/**
 * Search mentors (discovery) using MentorMeApi.listMentors and map MentorCardDto -> UiMentor.
 * Returns AppResult with a UI list; logs size and first id for verification.
 */
class SearchMentorsUseCase @Inject constructor(
    private val api: MentorMeApi
) {
    suspend operator fun invoke(
        q: String? = null,
        skills: List<String> = emptyList(),
        minRating: Float? = null,
        priceMin: Int? = null,
        priceMax: Int? = null,
        sort: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): AppResult<List<UiMentor>> {
        return try {
            val skillsCsv = skills.takeIf { it.isNotEmpty() }?.joinToString(",")

            Logx.d("SearchUseCase") {
                "🔍 Calling API: q=$q, skills=$skillsCsv, minRating=$minRating, " +
                "priceMin=$priceMin, priceMax=$priceMax, sort=$sort, page=$page, limit=$limit"
            }

            val res = api.listMentors(q, skillsCsv, minRating, priceMin, priceMax, sort, page, limit)
            if (!res.isSuccessful) {
                return AppResult.failure("HTTP ${res.code()} ${res.message()}")
            }

            // ✅ Debug: Log response metadata
            val data = res.body()?.data
            val total = data?.total ?: 0
            val backendLimit = data?.limit ?: 0
            val items = data?.items.orEmpty()

            Logx.d("SearchUseCase") {
                "📊 Backend response: total=$total, limit=$backendLimit, items.size=${items.size}, page=$page"
            }

            if (items.size != total && page == 1) {
                Logx.d("SearchUseCase") {
                    "⚠️  MISMATCH: Backend says total=$total but returned ${items.size} items!"
                }
            }

            // ✅ Log first and last items to verify completeness
            if (items.isNotEmpty()) {
                Logx.d("SearchUseCase") {
                    "First item: ${items.first().name} (id=${items.first().id})"
                }
                Logx.d("SearchUseCase") {
                    "Last item: ${items.last().name} (id=${items.last().id})"
                }
            }

            val ui = items.toUiMentorsFromCards()

            Logx.d("SearchUseCase") {
                "✅ Mapped to ${ui.size} UI mentors, firstId=${ui.firstOrNull()?.id ?: "-"}"
            }

            // ✅ Check if mapping lost any mentors
            if (ui.size != items.size) {
                Logx.d("SearchUseCase") {
                    "❌ MAPPER LOST DATA! DTOs=${items.size} → UI=${ui.size} (lost ${items.size - ui.size})"
                }
            }

            AppResult.success(ui)
        } catch (t: Throwable) {
            Logx.d("SearchUseCase") { "❌ Search failed: ${t.message}" }
            AppResult.failure(t)
        }
    }
}

