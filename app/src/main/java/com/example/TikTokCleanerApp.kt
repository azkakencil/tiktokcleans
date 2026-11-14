package com.example

import android.app.Application
import com.example.data.local.db.TikTokCleanerDatabase
import com.example.data.local.prefs.SecureTokenStorage
import com.example.data.remote.TikTokAuthService
import com.example.data.remote.TikTokDataExportParser
import com.example.data.remote.TikTokOEmbedService
import com.example.data.repository.TikTokCleanerRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class AppContainer(application: Application) {
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    private val database: TikTokCleanerDatabase by lazy {
        TikTokCleanerDatabase.getInstance(application)
    }

    private val dataStorage: com.example.data.local.DataStorage by lazy {
        com.example.data.local.DataStorage(database)
    }

    private val tokenStorage: SecureTokenStorage by lazy {
        SecureTokenStorage(application)
    }

    private val authService: TikTokAuthService by lazy {
        TikTokAuthService(okHttpClient)
    }

    private val oEmbedService: TikTokOEmbedService by lazy {
        TikTokOEmbedService(okHttpClient)
    }

    private val exportParser: TikTokDataExportParser by lazy {
        TikTokDataExportParser()
    }

    val repository: TikTokCleanerRepository by lazy {
        TikTokCleanerRepository(
            context = application,
            dataStorage = dataStorage,
            tokenStorage = tokenStorage,
            authService = authService,
            oEmbedService = oEmbedService,
            exportParser = exportParser
        )
    }
}

class TikTokCleanerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
