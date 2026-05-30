package br.com.silvestresantiago732.kingdomofarcanum.presentation.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _registerResult = mutableStateOf<Result<FirebaseUser?>?>(null)
    val registerResult: State<Result<FirebaseUser?>?> = _registerResult

    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> = _error

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        _confirmPassword.value = newValue
    }

    fun signUp() {
        if (_email.value.isBlank() || _password.value.isBlank()) return
        if (_password.value != _confirmPassword.value) {
            _error.value = R.string.register_error_passwords_mismatch
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signUp(_email.value, _password.value)
            handleAuthResult(result)
            _isLoading.value = false
        }
    }

    private fun handleAuthResult(result: Result<FirebaseUser?>) {
        if (result.isSuccess) {
            _registerResult.value = result
        } else {
            val exception = result.exceptionOrNull()
            if (isNetworkError(exception)) {
                _error.value = R.string.error_network
            } else {
                _error.value = R.string.register_error_msg
            }
        }
    }

    private fun isNetworkError(e: Throwable?): Boolean {
        if (e == null) return false
        val message = e.message?.lowercase() ?: ""
        return e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                e is java.net.SocketTimeoutException ||
                message.contains("network error") ||
                message.contains("unreachable host") ||
                message.contains("firebasenetworkexception") ||
                isNetworkError(e.cause)
    }

    fun resetResult() {
        _registerResult.value = null
        _error.value = null
    }
}
