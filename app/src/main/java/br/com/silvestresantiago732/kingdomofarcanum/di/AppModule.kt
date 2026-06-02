package br.com.silvestresantiago732.kingdomofarcanum.di

import android.content.Context
import br.com.silvestresantiago732.kingdomofarcanum.R
import br.com.silvestresantiago732.kingdomofarcanum.data.repository.AuthRepositoryImpl
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import br.com.silvestresantiago732.kingdomofarcanum.data.repository.CharacterRepositoryImpl
import br.com.silvestresantiago732.kingdomofarcanum.domain.repository.CharacterRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCharacterRepository(characterRepositoryImpl: CharacterRepositoryImpl): CharacterRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseDatabase(): FirebaseDatabase {
            val db = FirebaseDatabase.getInstance()
            db.setPersistenceEnabled(true)
            return db
        }

        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }

        @Provides
        @Singleton
        fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            return GoogleSignIn.getClient(context, gso)
        }
    }
}
