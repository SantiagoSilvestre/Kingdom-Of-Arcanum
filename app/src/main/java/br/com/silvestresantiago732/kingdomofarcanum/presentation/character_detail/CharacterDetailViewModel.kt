package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Character
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val characterId: String? = savedStateHandle["characterId"]

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _race = mutableStateOf("")
    val race: State<String> = _race

    private val _characterClass = mutableStateOf("")
    val characterClass: State<String> = _characterClass

    private val _observation = mutableStateOf("")
    val observation: State<String> = _observation

    private val _lore = mutableStateOf("")
    val lore: State<String> = _lore

    private val _intelligence = mutableStateOf("0")
    val intelligence: State<String> = _intelligence

    private val _strength = mutableStateOf("0")
    val strength: State<String> = _strength

    private val _agility = mutableStateOf("0")
    val agility: State<String> = _agility

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isSaved = mutableStateOf(false)
    val isSaved: State<Boolean> = _isSaved

    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> = _error

    val isNewCharacter: Boolean = characterId == "new"

    init {
        characterId?.let { id ->
            if (id != "new") {
                loadCharacter(id)
            }
        }
    }

    private fun loadCharacter(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val character = characterRepository.getCharacterById(id)
                character?.let {
                    _name.value = it.name
                    _race.value = it.race
                    _characterClass.value = it.characterClass
                    _observation.value = it.observation
                    _lore.value = it.lore
                    _intelligence.value = it.intelligence.toString()
                    _strength.value = it.strength.toString()
                    _agility.value = it.agility.toString()
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onRaceChange(newRace: String) {
        _race.value = newRace
    }

    fun onCharacterClassChange(newClass: String) {
        _characterClass.value = newClass
    }

    fun onObservationChange(newObservation: String) {
        _observation.value = newObservation
    }

    fun onLoreChange(newLore: String) {
        _lore.value = newLore
    }

    fun onIntelligenceChange(newValue: String) {
        if (isNewCharacter) _intelligence.value = newValue
    }

    fun onStrengthChange(newValue: String) {
        if (isNewCharacter) _strength.value = newValue
    }

    fun onAgilityChange(newValue: String) {
        if (isNewCharacter) _agility.value = newValue
    }

    fun saveCharacter() {
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val intVal = _intelligence.value.toIntOrNull() ?: 0
                val strVal = _strength.value.toIntOrNull() ?: 0
                val agiVal = _agility.value.toIntOrNull() ?: 0
                
                val character = Character(
                    id = if (characterId == "new") "" else characterId ?: "",
                    name = _name.value,
                    race = _race.value,
                    characterClass = _characterClass.value,
                    observation = _observation.value,
                    lore = _lore.value,
                    intelligence = intVal,
                    strength = strVal,
                    agility = agiVal,
                    maxHp = strVal * 5,
                    currentHp = strVal * 5,
                    maxMana = intVal * 5,
                    currentMana = intVal * 5
                )
                
                // Salvando no Firebase
                characterRepository.addCharacter(character)
                _isSaved.value = true
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleError(e: Exception) {
        val message = e.message?.lowercase() ?: ""
        if (message.contains("permission") || 
            message.contains("unauthenticated") || 
            message.contains("user is null") ||
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return
        }

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
                e is kotlinx.coroutines.TimeoutCancellationException || // Captura o estouro de tempo
                message.contains("network error") ||
                message.contains("unreachable host") ||
                message.contains("firebasenetworkexception") ||
                isNetworkError(e.cause)
    }

    fun resetError() {
        _error.value = null
    }
}
