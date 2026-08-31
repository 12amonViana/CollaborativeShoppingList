package com.collaborativeshoppinglist.core.di

import com.collaborativeshoppinglist.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        if (BuildConfig.USE_FIREBASE_EMULATORS) {
            useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, BuildConfig.FIREBASE_AUTH_EMULATOR_PORT)
        }
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        if (BuildConfig.USE_FIREBASE_EMULATORS) {
            useEmulator(
                BuildConfig.FIREBASE_EMULATOR_HOST,
                BuildConfig.FIREBASE_FIRESTORE_EMULATOR_PORT,
            )
        }
    }

}
