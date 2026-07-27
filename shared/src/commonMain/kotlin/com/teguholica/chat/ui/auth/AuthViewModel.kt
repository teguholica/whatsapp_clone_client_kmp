package com.teguholica.chat.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teguholica.chat.data.remote.AuthApiException
import com.teguholica.chat.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object PhoneInput : AuthUiState
    data object OtpSent : AuthUiState
    data object Authenticated : AuthUiState
    data class OtpError(val message: String) : AuthUiState
    data class PhoneError(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    init {
        checkLoggedIn()
    }

    private fun checkLoggedIn() {
        if (authRepository.isLoggedIn()) {
            _uiState.value = AuthUiState.Authenticated
        } else {
            _uiState.value = AuthUiState.PhoneInput
        }
    }

    fun updatePhone(phone: String) {
        _phone.value = phone
        clearError()
    }

    fun updateOtp(otp: String) {
        _otp.value = otp
        clearError()
    }

    fun requestOtp() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val phone = _phone.value
            if (phone.isBlank()) {
                _uiState.value = AuthUiState.PhoneError("Nomor telepon tidak boleh kosong")
                return@launch
            }
            val result = authRepository.register(phone)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.OtpSent },
                onFailure = { error -> AuthUiState.PhoneError(errorMessage(error)) },
            )
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val phone = _phone.value
            val otp = _otp.value
            if (otp.isBlank()) {
                _uiState.value = AuthUiState.OtpError("Kode OTP tidak boleh kosong")
                return@launch
            }
            val result = authRepository.verify(phone, otp)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Authenticated },
                onFailure = { error -> AuthUiState.OtpError(errorMessage(error)) },
            )
        }
    }

    private fun errorMessage(error: Throwable): String {
        val code = if (error is AuthApiException) error.statusCode else 0
        val msg = error.message ?: "Terjadi kesalahan"
        return when (code) {
            429 -> "Terlalu banyak permintaan. Coba lagi 60 detik."
            401 -> "Sesi berakhir. Silakan login ulang."
            400 -> "Data tidak valid. Periksa kembali input Anda."
            else -> msg
        }
    }

    fun clearError() {
        val current = _uiState.value
        when (current) {
            is AuthUiState.OtpError -> _uiState.value = AuthUiState.OtpSent
            is AuthUiState.PhoneError -> _uiState.value = AuthUiState.PhoneInput
            else -> {}
        }
    }
}
