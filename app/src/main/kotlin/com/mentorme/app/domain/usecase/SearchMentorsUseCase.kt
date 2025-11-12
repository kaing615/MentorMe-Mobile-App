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
            val res = api.listMentors(q, skillsCsv, minRating, priceMin, priceMax, sort, page, limit)
            if (!res.isSuccessful) {
                return AppResult.failure("HTTP ${res.code()} ${res.message()}")
            }
            val items = res.body()?.data?.items.orEmpty()
            val ui = items.toUiMentorsFromCards()
            Logx.d("SearchUseCase") { "items=${ui.size} firstId=${ui.firstOrNull()?.id ?: "-"}" }
            AppResult.success(ui)
        } catch (t: Throwable) {
            AppResult.failure(t)
        }
    }
}

