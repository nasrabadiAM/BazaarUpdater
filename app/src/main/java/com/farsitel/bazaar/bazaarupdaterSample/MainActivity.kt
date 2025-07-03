package com.farsitel.bazaar.bazaarupdaterSample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.farsitel.bazaar.bazaarupdaterSample.referrer.ReferrerViewModel
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.BazaarUpdaterSampleTheme
import com.farsitel.bazaar.updater.BazaarUpdater
import com.farsitel.bazaar.updater.UpdateResult

class MainActivity : ComponentActivity() {
    private val referrerViewModel by viewModels<ReferrerViewModel>()

    private val updateState = mutableStateOf<UpdateResult?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val referrerContent by referrerViewModel.referrerContent.collectAsState()
            val errorMessage by referrerViewModel.errorMessage.collectAsState()
            val logs by referrerViewModel.logs.collectAsState()
            BazaarUpdaterSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        UpdateScreen(
                            updateState = updateState,
                            modifier = Modifier.padding(innerPadding),
                            onUpdateClick = ::updateApplication,
                            onCheckVersionClick = ::checkUpdateState,
                        )
                        ReferrerSdkInfo(
                            error = errorMessage,
                            logs = logs,
                            referrerContent = referrerContent,
                        )
                    }
                }
            }
        }
    }

    private fun updateApplication() {
        BazaarUpdater.updateApplication(context = this)
    }

    override fun onResume() {
        super.onResume()
        referrerViewModel.onResume()
    }

    private fun checkUpdateState() {
        BazaarUpdater.getLastUpdateState(context = this) { result ->
            updateState.value = result
        }
    }
}