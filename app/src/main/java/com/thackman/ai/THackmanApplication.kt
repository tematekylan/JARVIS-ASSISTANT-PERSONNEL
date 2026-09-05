package com.thackman.ai

import android.app.Application
import com.google.firebase.FirebaseApp

class THackmanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase services
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
