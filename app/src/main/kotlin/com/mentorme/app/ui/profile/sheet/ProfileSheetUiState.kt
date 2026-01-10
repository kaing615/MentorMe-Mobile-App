package com.mentorme.app.ui.profile.sheet

import com.mentorme.app.data.model.Mentor

sealed interface ProfileSheetUiState {
    object Idle : ProfileSheetUiState
    object Loading : ProfileSheetUiState
    data class Success(val mentor: Mentor) : ProfileSheetUiState
    data class Error(val message: String) : ProfileSheetUiState
}