package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.TikTokCleanerRoot
import com.example.ui.theme.TikTokCleanerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as TikTokCleanerApp
        MainViewModel.Factory(app.container.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingOAuthIntent(intent)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            val jsonPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                if (uri != null) {
                    viewModel.importDataExportFile(uri)
                }
            }

            TikTokCleanerTheme(darkTheme = isDarkMode) {
                TikTokCleanerRoot(
                    viewModel = viewModel,
                    onLaunchOAuthUri = ::openCustomTab,
                    onPickDataJson = {
                        jsonPickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                    onOpenVideoUrl = ::openExternalTikTokUrl
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingOAuthIntent(intent)
    }

    private fun handleIncomingOAuthIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "tiktokcleaner" && data.host == "oauth") {
            viewModel.handleOAuthRedirect(data)
        }
    }

    private fun openCustomTab(uri: Uri) {
        runCatching {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(this, uri)
        }.onFailure {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun openExternalTikTokUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
