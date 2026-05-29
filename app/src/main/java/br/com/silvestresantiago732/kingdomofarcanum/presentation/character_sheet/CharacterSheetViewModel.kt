package br.com.silvestresantiago732.kingdomofarcanum.presentation.character_sheet

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadCharacter()
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            _isLoading.value = true
            _character.value = characterRepository.getCharacterById(characterId)
            _isLoading.value = false
        }
    }

    fun updateAttribute(type: String, value: Int) {
        val currentChar = _character.value ?: return
        
        var updatedChar = when (type) {
            "gold" -> currentChar.copy(gold = value)
            else -> currentChar
        }
        
        saveCharacter(updatedChar)
    }

    fun spendPoints(intelligenceIncrease: Int, strengthIncrease: Int) {
        val currentChar = _character.value ?: return
        val totalCost = intelligenceIncrease + strengthIncrease
        if (totalCost > currentChar.attributePoints) return

        val newInt = currentChar.intelligence + intelligenceIncrease
        val newStr = currentChar.strength + strengthIncrease
        val newMaxHp = newStr * 5
        val newMaxMana = newInt * 5

        var updatedChar = currentChar.copy(
            intelligence = newInt,
            strength = newStr,
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

        // Lógica: Nível 1 -> 2 (100xp), Nível 2 -> 3 (200xp), Nível 3 -> 4 (300xp)
        // A regra é: maxXp = nível_atual * 100
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
            characterRepository.addCharacter(character)
            _character.value = character
        }
    }
}
