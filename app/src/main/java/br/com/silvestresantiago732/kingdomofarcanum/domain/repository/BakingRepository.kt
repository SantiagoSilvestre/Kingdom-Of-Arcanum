package br.com.silvestresantiago732.kingdomofarcanum.domain.repository

import android.graphics.Bitmap

interface BakingRepository {
    suspend fun generateContent(bitmap: Bitmap, prompt: String): Result<String>
}
