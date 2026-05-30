package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_image

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterImageViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val characterId: String = checkNotNull(savedStateHandle["characterId"])

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _generatedImageUrl = MutableStateFlow<String?>(null)
    val generatedImageUrl: StateFlow<String?> = _generatedImageUrl.asStateFlow()

    fun generateImage(prompt: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val character = characterRepository.getCharacterById(characterId)
                val characterContext = "${character?.race} ${character?.characterClass}".trim()
                
                val artisticPrompt = prompt
                    .replace("faca", "dagger", ignoreCase = true)
                
                val finalPrompt = "Digital art, full color RPG character portrait, $characterContext, $artisticPrompt, high fantasy world, detailed armor, cinematic lighting, masterpiece, 8k resolution"
                
                val encodedPrompt = java.net.URLEncoder.encode(finalPrompt, "UTF-8")
                val url = "https://image.pollinations.ai/prompt/$encodedPrompt?width=1024&height=1024&nologo=true&seed=${System.currentTimeMillis()}"
                
                _generatedImageUrl.value = url
                _uiState.value = UiState.Success(R.string.char_image_processing)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun saveImageToCharacter() {
        val url = _generatedImageUrl.value ?: return
        viewModelScope.launch {
            try {
                val character = characterRepository.getCharacterById(characterId)
                character?.let {
                    val updatedChar = it.copy(imageUrl = url)
                    characterRepository.addCharacter(updatedChar)
                    _uiState.value = UiState.Success(R.string.char_image_saved)
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(e: Exception) {
        val errorRes = if (isNetworkError(e)) {
            R.string.error_network
        } else {
            R.string.error_generic
        }
        _uiState.value = UiState.Error(errorRes)
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
}
