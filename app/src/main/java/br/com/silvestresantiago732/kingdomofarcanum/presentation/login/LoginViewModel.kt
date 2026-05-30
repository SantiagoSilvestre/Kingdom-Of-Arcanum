package br.com.silvestresantiago732.kingdomofarcanum.presentation.login

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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _loginResult = mutableStateOf<Result<FirebaseUser?>?>(null)
    val loginResult: State<Result<FirebaseUser?>?> = _loginResult

    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> = _error

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.login(_email.value, _password.value)
            handleAuthResult(result)
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithGoogle(idToken)
            handleAuthResult(result)
            _isLoading.value = false
        }
    }

    private fun handleAuthResult(result: Result<FirebaseUser?>) {
        if (result.isSuccess) {
            _loginResult.value = result
        } else {
            val exception = result.exceptionOrNull()
            if (isNetworkError(exception)) {
                _error.value = R.string.error_network
            } else {
                _error.value = R.string.login_error_msg
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
        _loginResult.value = null
        _error.value = null
    }
}
