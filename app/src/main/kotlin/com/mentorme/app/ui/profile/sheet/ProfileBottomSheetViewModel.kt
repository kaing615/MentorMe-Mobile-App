package com.mentorme.app.ui.profile.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.repository.MentorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileBottomSheetViewModel @Inject constructor(
    private val mentorRepository: MentorRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ProfileSheetUiState>(ProfileSheetUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun loadMentor(mentorId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileSheetUiState.Loading

            when (val result = mentorRepository.getMentorById(mentorId)) {
                is AppResult.Success -> {
                    _uiState.value = ProfileSheetUiState.Success(result.data)
                }

                is AppResult.Error -> {
                    _uiState.value = ProfileSheetUiState.Error(
                        message = result.throwable ?: "Không tải được mentor"
                    )
                }

                AppResult.Loading -> {
                    _uiState.value = ProfileSheetUiState.Loading
                }
            }
        }
    }

    fun reset() {
        _uiState.value = ProfileSheetUiState.Idle
    }
}
