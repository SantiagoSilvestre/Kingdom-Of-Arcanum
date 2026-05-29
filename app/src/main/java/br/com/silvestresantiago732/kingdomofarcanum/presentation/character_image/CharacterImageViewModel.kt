package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_image

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.BakingRepository
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterImageViewModel @Inject constructor(
    private val bakingRepository: BakingRepository,
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
                
                // Melhorando o prompt: 
                // 1. Adicionamos contexto de arte digital épica
                // 2. Traduzimos termos comuns de RPG que podem ser filtrados ou mal interpretados
                val artisticPrompt = prompt
                    .replace("faca", "dagger", ignoreCase = true)
                
                val finalPrompt = "Digital art, full color RPG character portrait, $characterContext, $artisticPrompt, high fantasy world, detailed armor, cinematic lighting, masterpiece, 8k resolution"
                
                val encodedPrompt = java.net.URLEncoder.encode(finalPrompt, "UTF-8")
                val url = "https://image.pollinations.ai/prompt/$encodedPrompt?width=1024&height=1024&nologo=true&seed=${System.currentTimeMillis()}"
                
                _generatedImageUrl.value = url
                _uiState.value = UiState.Success("IA está processando sua imagem...")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Erro de conexão com a IA")
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
                    _uiState.value = UiState.Success("Imagem vinculada ao personagem!")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro ao vincular imagem")
            }
        }
    }
}
