package com.day.mate.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    fun loadUser() {
        val currentUser = auth.currentUser ?: return

        _uiState.value = _uiState.value.copy(loading = true)

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(User::class.java)
                    if (userData != null) {
                        _uiState.value = _uiState.value.copy(
                            user = userData,
                            loading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "User not found"
                    )
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Error loading user"
                )
            }
    }


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
