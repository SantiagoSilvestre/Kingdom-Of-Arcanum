package br.com.silvestresantiago732.kingdomofarcanum.data.repository

import br.com.silvestresantiago732.kingdomofarcanum.domain.model.Character
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.CharacterRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : CharacterRepository {

    private val charactersRef = database.getReference("characters")
    private val userId get() = auth.currentUser?.uid

    override fun getCharacters(): Flow<List<Character>> = callbackFlow {
        val uid = userId ?: return@callbackFlow
        val userCharactersRef = charactersRef.child(uid)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Character::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        userCharactersRef.addValueEventListener(listener)
        awaitClose { userCharactersRef.removeEventListener(listener) }
    }

    override suspend fun getCharacterById(id: String): Character? {
        val uid = userId ?: return null
        return charactersRef.child(uid).child(id).get().await().getValue(Character::class.java)
    }

    override suspend fun addCharacter(character: Character) {
        val uid = userId ?: return
        val key = if (character.id.isBlank()) {
            charactersRef.child(uid).push().key ?: return
        } else character.id

        val newCharacter = character.copy(id = key)
        charactersRef.child(uid).child(key).setValue(newCharacter).await()
    }

    override suspend fun deleteCharacter(id: String) {
        val uid = userId ?: return
        charactersRef.child(uid).child(id).removeValue().await()
    }
}
