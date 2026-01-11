package com.mentorme.app.core.notifications

import androidx.lifecycle.ViewModel
import com.mentorme.app.core.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationPermissionViewModel @Inject constructor(
    val dataStoreManager: DataStoreManager
) : ViewModel()

