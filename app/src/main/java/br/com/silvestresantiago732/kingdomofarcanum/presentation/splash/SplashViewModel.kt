package br.com.silvestresantiago732.kingdomofarcanum.presentation.splash

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val isUserLoggedIn: Boolean
        get() = authRepository.currentUser != null
}
