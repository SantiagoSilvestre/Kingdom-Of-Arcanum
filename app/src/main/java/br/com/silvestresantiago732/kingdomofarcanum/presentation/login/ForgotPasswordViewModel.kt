package br.com.silvestresantiago732.kingdomofarcanum.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _result = mutableStateOf<Result<Unit>?>(null)
    val result: State<Result<Unit>?> = _result

    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> = _error

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun sendResetEmail() {
        if (_email.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.sendPasswordResetEmail(_email.value)
            if (result.isSuccess) {
                _result.value = result
            } else {
                val exception = result.exceptionOrNull()
                if (isNetworkError(exception)) {
                    _error.value = R.string.error_network
                } else {
                    _error.value = R.string.forgot_password_error
                }
            }
            _isLoading.value = false
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
        _result.value = null
        _error.value = null
    }
}
