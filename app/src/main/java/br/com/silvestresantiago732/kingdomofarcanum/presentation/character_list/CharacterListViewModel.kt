package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Character
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> = _error

    private val _characters = kotlinx.coroutines.flow.MutableStateFlow<List<Character>>(emptyList())
    val characters = _characters.asStateFlow()

    init {
        loadCharacters()
    }

    fun loadCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Coleta os personagens e desativa o loading no primeiro resultado
                characterRepository.getCharacters().collect {
                    _characters.value = it
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                handleError(e)
                _isLoading.value = false
            }
        }
    }

    fun deleteCharacter(id: String) {
        viewModelScope.launch {
            try {
                characterRepository.deleteCharacter(id)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(e: Exception) {
        if (isNetworkError(e)) {
            _error.value = R.string.error_network
        } else {
            _error.value = R.string.error_generic
        }
    }

    private fun isNetworkError(e: Throwable?): Boolean {
        if (e == null) return false
        val message = e.message?.lowercase() ?: ""
        return e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                e is java.net.SocketTimeoutException ||
                e is kotlinx.coroutines.TimeoutCancellationException ||
                message.contains("network error") ||
                message.contains("unreachable host") ||
                message.contains("firebasenetworkexception") ||
                isNetworkError(e.cause)
    }

    fun resetError() {
        _error.value = null
    }
}
