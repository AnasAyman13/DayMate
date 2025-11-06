package com.day.mate.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel بسيط للتجربة الآن. لاحقًا هذا الملف سيقوم بجلب/حفظ الإعدادات من DataStore / Firestore.
 */
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        // هنا يمكن لاحقًا تحميل البيانات من Firebase/Auth/DataStore
        // الآن نستخدم mock (مُهيأ داخل SettingsState)
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(darkModeEnabled = enabled,)
        // لاحقًا: احفظ القيمة في DataStore أو Firebase
    }

    fun toggleCloudSync(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(cloudSyncEnabled = enabled,)
        // لاحقًا: تشغيل/إيقاف sync منطقياً أو حفظ القيمة
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled,)
        // لاحقًا: تفعيل/إيقاف قنوات الإشعارات أو حفظ الإعداد
    }

    fun onEditProfileClicked() {
        // placeholder: navigate to edit screen
    }

    fun onManageStorageClicked() {
        // placeholder
    }



    fun onLogoutClicked() {
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }
}
