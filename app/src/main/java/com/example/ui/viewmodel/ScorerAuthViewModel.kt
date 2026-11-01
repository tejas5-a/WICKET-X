package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserAccountEntity
import com.example.data.remote.supabase.OtpResult
import com.example.data.remote.supabase.SupabaseAuthService
import com.example.data.remote.supabase.VerifyResult
import com.example.data.repository.CricketRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AccountStatus(val label: String) {
    PHONE_UNVERIFIED("Phone not verified"),
    PHONE_VERIFIED("Phone verified"),
    SCORER_PENDING("Scorer access pending"),
    SCORER_ACTIVE("Scorer access active"),
    ACCESS_RESTRICTED("Access restricted")
}

class ScorerAuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CricketRepository(application)

    val currentUser: StateFlow<UserAccountEntity?> = repository.observeCurrentUserAccount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _cooldownSeconds = MutableStateFlow(0)
    val cooldownSeconds: StateFlow<Int> = _cooldownSeconds.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        // Ensure initial accounts and seed data exist
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setPhoneNumber(phone: String) {
        _phoneNumber.value = phone
        _errorMessage.value = null
    }

    fun setOtpCode(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _otpCode.value = code
        }
        _errorMessage.value = null
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }

    fun getAccountStatus(user: UserAccountEntity?): AccountStatus {
        if (user == null) return AccountStatus.PHONE_UNVERIFIED
        return when {
            user.role == "restricted" -> AccountStatus.ACCESS_RESTRICTED
            user.role == "scorer" || user.role == "admin" -> AccountStatus.SCORER_ACTIVE
            user.role == "scorer_pending" -> AccountStatus.SCORER_PENDING
            user.verificationStatus == "phone_verified" -> AccountStatus.PHONE_VERIFIED
            else -> AccountStatus.PHONE_UNVERIFIED
        }
    }

    fun sendOtp() {
        val phone = _phoneNumber.value.trim()
        if (phone.length < 10) {
            _errorMessage.value = "Enter a valid mobile number with country code (e.g. +1... or +91...)"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _statusMessage.value = null

            when (val result = repository.sendPhoneOtp(phone)) {
                is OtpResult.Success -> {
                    _isOtpSent.value = true
                    _statusMessage.value = result.message
                    startCooldown(result.cooldownSeconds)
                }
                is OtpResult.CooldownActive -> {
                    _errorMessage.value = "Please wait ${result.remainingSeconds}s before requesting a new OTP"
                    startCooldown(result.remainingSeconds)
                }
                is OtpResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun verifyOtp(onSuccess: () -> Unit = {}) {
        val phone = _phoneNumber.value.trim()
        val code = _otpCode.value.trim()

        if (code.length != 6) {
            _errorMessage.value = "Please enter the complete 6-digit OTP"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = repository.verifyPhoneOtp(phone, code)) {
                is VerifyResult.Success -> {
                    _statusMessage.value = "Phone verified successfully!"
                    _otpCode.value = ""
                    _isOtpSent.value = false
                    onSuccess()
                }
                is VerifyResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun requestScorerAccess(
        displayName: String,
        clubName: String,
        experienceDetails: String = "",
        onComplete: (Boolean) -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.activateScorerProfile(
                userId = user.id,
                displayName = displayName,
                clubName = clubName,
                experienceDetails = experienceDetails
            )

            if (result.isSuccess) {
                _statusMessage.value = "Scorer role activated successfully! You can now create tournaments and score matches."
                onComplete(true)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to activate Scorer role"
                onComplete(false)
            }
            _isLoading.value = false
        }
    }

    fun switchTestAccount(toScorer: Boolean) {
        viewModelScope.launch {
            val targetUserId = if (toScorer) "user_scorer_1111" else "user_viewer_2222"
            repository.setActiveSessionUserId(targetUserId)
            val user = repository.getUserAccount(targetUserId)
            if (user != null) {
                repository.saveUserAccount(user)
                _statusMessage.value = if (toScorer) "Switched to Official Scorer test account" else "Switched to Viewer test account"
            }
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        _cooldownSeconds.value = seconds
        cooldownJob = viewModelScope.launch {
            while (_cooldownSeconds.value > 0) {
                delay(1000L)
                _cooldownSeconds.value -= 1
            }
        }
    }
}
