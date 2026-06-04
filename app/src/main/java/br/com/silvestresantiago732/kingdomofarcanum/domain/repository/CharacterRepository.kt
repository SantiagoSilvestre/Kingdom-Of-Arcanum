package br.com.silvestresantiago732.kingdomofarcanum.domain.repository

import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacters(): Flow<List<Character>>
    suspend fun getCharacterById(id: String): Character?
    suspend fun addCharacter(character: Character)
    suspend fun deleteCharacter(id: String)
    suspend fun uploadCharacterImage(characterId: String, imageUrl: String): Result<String>
    suspend fun uploadCharacterImageBytes(characterId: String, imageBytes: ByteArray): Result<String>
}
