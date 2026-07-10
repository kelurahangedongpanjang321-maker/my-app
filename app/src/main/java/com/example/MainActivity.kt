package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AppNavigation
import com.example.ui.SIMPELViewModel
import com.example.ui.SIMPELViewModelFactory
import com.example.ui.theme.SIMPELTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: SIMPELViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely initialize Firebase with zero crash risk on missing JSON configurations
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize local Room Database instance
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "simpel_database"
        ).fallbackToDestructiveMigration().build()

        // Repository & ViewModel Setup
        val repository = AppRepository(applicationContext, database)
        val factory = SIMPELViewModelFactory(applicationContext, repository)
        viewModel = ViewModelProvider(this, factory)[SIMPELViewModel::class.java]

        setContent {
            // Observe the real-time customizable system settings
            val settings by viewModel.appSettings.collectAsState()

            SIMPELTheme(settings = settings) {
                // Launch the navigation engine
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
