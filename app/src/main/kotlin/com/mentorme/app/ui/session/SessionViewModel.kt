package com.mentorme.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mentorme.app.core.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SessionState(
    val isLoggedIn: Boolean = false,
    val role: String? = null,
    val status: String? = null
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    dataStoreManager: DataStoreManager
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
}
