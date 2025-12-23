package com.mentorme.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.datastore.DataStoreManager
import com.mentorme.app.core.utils.AppResult
import com.mentorme.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionState(
    val isLoggedIn: Boolean = false,
    val role: String? = null,
    val status: String? = null
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val session: StateFlow<SessionState> = combine(
        dataStoreManager.getToken(),
        dataStoreManager.getUserRole(),
        dataStoreManager.getUserStatus()
    ) { token, role, status ->
        SessionState(
            isLoggedIn = !token.isNullOrBlank(),
            role = role,
            status = status
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState())

    fun refreshStatus(onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            when (val res = profileRepository.getProfileMe()) {
                is AppResult.Success -> {
                    val rawStatus = res.data.profile?.user?.status?.lowercase()
                    val normalizedStatus = rawStatus?.replace('_', '-')
                    if (!normalizedStatus.isNullOrBlank()) {
                        dataStoreManager.saveUserStatus(normalizedStatus)
                    }
                    onResult(true, normalizedStatus)
                }
                is AppResult.Error -> onResult(false, res.throwable)
                AppResult.Loading -> Unit
            }
        }
    }
}
