package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_image

import androidx.annotation.StringRes

sealed interface UiState {
    object Initial : UiState
    object Loading : UiState
    data class Success(@StringRes val messageRes: Int) : UiState
    data class Error(@StringRes val messageRes: Int) : UiState
}
