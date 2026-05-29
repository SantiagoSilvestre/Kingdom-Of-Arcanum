package br.com.silvestresantiago732.kingdomofarcanum.presentation.register

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            _registerResult.value = Result.failure(Exception("As senhas não coincidem"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _registerResult.value = authRepository.signUp(_email.value, _password.value)
            _isLoading.value = false
        }
    }

    fun resetResult() {
        _registerResult.value = null
    }
}
