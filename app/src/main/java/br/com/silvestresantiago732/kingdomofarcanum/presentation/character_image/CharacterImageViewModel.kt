package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_image

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.CharacterRepository
import br.com.silvestresantiago732.kingdomofarcanum.util.Logger
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.generationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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

    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap: StateFlow<Bitmap?> = _generatedBitmap.asStateFlow()

    private val _generationsRemaining = MutableStateFlow(3)
    val generationsRemaining: StateFlow<Int> = _generationsRemaining.asStateFlow()

    init {
        loadCharacterInfo()
    }

    private fun loadCharacterInfo() {
        viewModelScope.launch {
            val character = characterRepository.getCharacterById(characterId)
            character?.let {
                updateRemainingGenerations(it)
            }
        }
    }

    private fun updateRemainingGenerations(character: br.com.silvestresantiago732.kingdomofarcanum.domain.model.Character) {
        val currentTime = System.currentTimeMillis()
        val isNewDay = !isSameDay(character.lastImageGenerationTimestamp, currentTime)
        val currentGenerations = if (isNewDay) 0 else character.imageGenerationsToday
        _generationsRemaining.value = (3 - currentGenerations).coerceAtLeast(0)
    }

    fun generateImage(prompt: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val character = characterRepository.getCharacterById(characterId)
                    ?: return@launch handleError(Exception("Character not found"))

                // Validação de limite diário
                val currentTime = System.currentTimeMillis()
                val isNewDay = !isSameDay(character.lastImageGenerationTimestamp, currentTime)
                
                val currentGenerations = if (isNewDay) 0 else character.imageGenerationsToday
                
                if (currentGenerations >= 3) {
                    _uiState.value = UiState.Error(R.string.char_image_error_limit)
                    _generationsRemaining.value = 0
                    return@launch
                }

                val characterContext = "${character.race} ${character.characterClass}".trim()
                
                val finalPrompt = """
                    Generate a full color RPG character portrait illustration.
                    Character details: $characterContext. 
                    Additional description: $prompt.
                    Style: Digital art, high fantasy, cinematic lighting.
                    Create the image to go alongside your description.
                """.trimIndent()

                val model = Firebase.ai.generativeModel(
                    modelName = "gemini-2.5-flash-image",
                    // Configure the model to respond with text and images (required)
                    generationConfig = generationConfig {
                        responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE) }
                )
                
                val response = model.generateContent(finalPrompt)
                val responseContent = response.candidates.first().content
                
                var foundBitmap: Bitmap? = null
                
                for (part in responseContent.parts) {
                    if (part is ImagePart) {
                        // Acessa o bitmap conforme a estrutura do ImagePart no SDK Android
                        foundBitmap = part.image
                        if (foundBitmap != null) break
                    }
                }
                
                if (foundBitmap != null) {
                    // Atualiza contador de gerações
                    val updatedCharacter = character.copy(
                        imageGenerationsToday = currentGenerations + 1,
                        lastImageGenerationTimestamp = currentTime
                    )
                    characterRepository.addCharacter(updatedCharacter)
                    updateRemainingGenerations(updatedCharacter)

                    _generatedBitmap.value = foundBitmap
                    _generatedImageUrl.value = "bitmap_placeholder" 
                    _uiState.value = UiState.Success(R.string.char_image_processing)
                } else {
                    _uiState.value = UiState.Error(R.string.char_image_error_ai)
                    android.util.Log.w("CharacterImageVM", "O modelo não incluiu uma imagem na resposta.")
                }
            } catch (e: Throwable) {
                handleError(e)
            }
        }
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    fun saveImageToCharacter() {
        val bitmap = _generatedBitmap.value ?: return
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                
                val uploadResult = characterRepository.uploadCharacterImageBytes(characterId, data)
                
                if (uploadResult.isSuccess) {
                    val firebaseDownloadUrl = uploadResult.getOrThrow()
                    
                    val character = characterRepository.getCharacterById(characterId)
                    character?.let {
                        val updatedChar = it.copy(imageUrl = firebaseDownloadUrl)
                        characterRepository.addCharacter(updatedChar)
                        _uiState.value = UiState.Success(R.string.char_image_saved)
                    }
                } else {
                    _uiState.value = UiState.Error(R.string.char_image_error_save)
                }
            } catch (e: Throwable) {
                handleError(e)
            }
        }
    }

    private fun handleError(e: Throwable) {
        val message = e.message?.lowercase() ?: ""
        if (message.contains("permission") || 
            message.contains("unauthenticated") || 
            message.contains("user is null") ||
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return
        }

        Logger.e("Error in ViewModel: ${e.message}", e)
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
