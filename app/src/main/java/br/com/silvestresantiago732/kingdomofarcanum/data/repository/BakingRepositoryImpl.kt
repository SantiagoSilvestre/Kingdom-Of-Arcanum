package br.com.silvestresantiago732.kingdomofarcanum.data.repository

import android.graphics.Bitmap
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.BakingRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BakingRepositoryImpl @Inject constructor() : BakingRepository {
    private val generativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-flash-latest",
        )
    }

    override suspend fun generateContent(bitmap: Bitmap, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            val text = response.text
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("No content generated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
