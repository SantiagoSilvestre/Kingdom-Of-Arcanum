package br.com.silvestresantiago732.kingdomofarcanum.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

object Logger {
    private const val TAG = "KingdomOfArcanum"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
        throwable?.let {
            Firebase.crashlytics.recordException(it)
        }
    }

    fun log(message: String) {
        Firebase.crashlytics.log(message)
    }

    fun setUserId(userId: String) {
        Firebase.crashlytics.setUserId(userId)
    }

    fun setCustomKey(key: String, value: String) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
}
