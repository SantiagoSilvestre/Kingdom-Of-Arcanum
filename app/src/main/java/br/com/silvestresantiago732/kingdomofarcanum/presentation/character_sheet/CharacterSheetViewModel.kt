package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_sheet

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Character
import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Item
import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Skill
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val characterId: String = checkNotNull(savedStateHandle["characterId"])

    private val _character = mutableStateOf<Character?>(null)
    val character: State<Character?> = _character

    private val _isLoading = mutableStateOf(value = false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<Int?>(null)
    val error: State<Int?> = _error

    init {
        loadCharacter()
    }

    fun retryLoad() {
        loadCharacter()
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _character.value = characterRepository.getCharacterById(characterId)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAttribute(type: String, value: Int) {
        val currentChar = _character.value ?: return
        
        val updatedChar = when (type) {
            "gold" -> currentChar.copy(gold = value)
            else -> currentChar
        }
        
        saveCharacter(updatedChar)
    }

    fun spendPoints(intelligenceIncrease: Int, strengthIncrease: Int, agilityIncrease: Int) {
        val currentChar = _character.value ?: return
        val totalCost = intelligenceIncrease + strengthIncrease + agilityIncrease
        if (totalCost > currentChar.attributePoints) return

        val newInt = currentChar.intelligence + intelligenceIncrease
        val newStr = currentChar.strength + strengthIncrease
        val newAgi = currentChar.agility + agilityIncrease
        val newMaxHp = newStr * 5
        val newMaxMana = newInt * 5

        var updatedChar = currentChar.copy(
            intelligence = newInt,
            strength = newStr,
            agility = newAgi,
            attributePoints = currentChar.attributePoints - totalCost,
            maxHp = newMaxHp,
            currentHp = if (currentChar.currentHp > newMaxHp) newMaxHp else currentChar.currentHp,
            maxMana = newMaxMana,
            currentMana = if (currentChar.currentMana > newMaxMana) newMaxMana else currentChar.currentMana
        )

        if (updatedChar.currentHp == 0) updatedChar = updatedChar.copy(currentHp = updatedChar.maxHp)
        if (updatedChar.currentMana == 0) updatedChar = updatedChar.copy(currentMana = updatedChar.maxMana)

        saveCharacter(updatedChar)
    }

    fun addXp(amount: Int) {
        val currentChar = _character.value ?: return
        var newXp = currentChar.currentXp + amount
        var newLevel = currentChar.level
        var newMaxXp = currentChar.maxXp
        var newPoints = currentChar.attributePoints

        while (newXp >= newMaxXp) {
            newXp -= newMaxXp
            newLevel++
            newPoints++
            newMaxXp = newLevel * 100
        }

        saveCharacter(currentChar.copy(
            currentXp = newXp,
            level = newLevel,
            maxXp = newMaxXp,
            attributePoints = newPoints
        ))
    }

    fun addSkill(name: String, damage: String, observation: String, manaCost: Int) {
        val currentChar = _character.value ?: return
        val newSkill = Skill(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            damage = damage,
            observation = observation,
            manaCost = manaCost
        )
        val updatedSkills = currentChar.skills + newSkill
        saveCharacter(currentChar.copy(skills = updatedSkills))
    }

    fun updateSkill(skillId: String, name: String, damage: String, observation: String, manaCost: Int) {
        val currentChar = _character.value ?: return
        val updatedSkills = currentChar.skills.map {
            if (it.id == skillId) it.copy(name = name, damage = damage, observation = observation, manaCost = manaCost)
            else it
        }
        saveCharacter(currentChar.copy(skills = updatedSkills))
    }

    fun deleteSkill(skillId: String) {
        val currentChar = _character.value ?: return
        val updatedSkills = currentChar.skills.filter { it.id != skillId }
        saveCharacter(currentChar.copy(skills = updatedSkills))
    }

    fun useSkill(skill: Skill) {
        val currentChar = _character.value ?: return
        if (currentChar.currentMana >= skill.manaCost) {
            val updatedChar = currentChar.copy(
                currentMana = currentChar.currentMana - skill.manaCost
            )
            saveCharacter(updatedChar)
        }
    }

    fun updateHealth(amount: Int) {
        val currentChar = _character.value ?: return
        val newHp = (currentChar.currentHp + amount).coerceIn(0, currentChar.maxHp)
        saveCharacter(currentChar.copy(currentHp = newHp))
    }

    fun updateMana(amount: Int) {
        val currentChar = _character.value ?: return
        val newMana = (currentChar.currentMana + amount).coerceIn(0, currentChar.maxMana)
        saveCharacter(currentChar.copy(currentMana = newMana))
    }

    fun addItem(name: String, damage: String, observation: String) {
        val currentChar = _character.value ?: return
        val newItem = Item(id = java.util.UUID.randomUUID().toString(), name = name, damage = damage, observation = observation)
        val updatedItems = currentChar.items + newItem
        saveCharacter(currentChar.copy(items = updatedItems))
    }

    fun updateItem(itemId: String, name: String, damage: String, observation: String) {
        val currentChar = _character.value ?: return
        val updatedItems = currentChar.items.map {
            if (it.id == itemId) it.copy(name = name, damage = damage, observation = observation)
            else it
        }
        saveCharacter(currentChar.copy(items = updatedItems))
    }

    fun deleteItem(itemId: String) {
        val currentChar = _character.value ?: return
        val updatedItems = currentChar.items.filter { it.id != itemId }
        saveCharacter(currentChar.copy(items = updatedItems))
    }

    private fun saveCharacter(character: Character) {
        viewModelScope.launch {
            try {
                characterRepository.addCharacter(character)
                _character.value = character
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
        return (e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                e is java.net.SocketTimeoutException ||
                e is kotlinx.coroutines.TimeoutCancellationException ||
                message.contains("network error") ||
                message.contains("unreachable host") ||
                message.contains("firebasenetworkexception") ||
                isNetworkError(e.cause))
    }

    fun resetError() {
        _error.value = null
    }
}
