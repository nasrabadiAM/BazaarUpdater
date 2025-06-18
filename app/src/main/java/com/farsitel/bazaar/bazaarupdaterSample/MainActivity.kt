package com.farsitel.bazaar.bazaarupdaterSample

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cafebazaar.referrersdk.model.ReferrerDetails
import com.farsitel.bazaar.bazaarupdaterSample.referrer.ReferrerViewModel
import com.farsitel.bazaar.bazaarupdaterSample.ui.theme.BazaarupdaterSampleTheme
import com.farsitel.bazaar.updater.BazaarUpdater
import com.farsitel.bazaar.updater.UpdateResult

class MainActivity : ComponentActivity() {
    private val referrerViewModel by viewModels<ReferrerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val referrerContent by referrerViewModel.referrerContent.collectAsState()
            val errorMessage by referrerViewModel.errorMessage.collectAsState()
            val logs by referrerViewModel.logs.collectAsState()
            BazaarupdaterSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        UpdateScreen()
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

    override fun onResume() {
        super.onResume()
        referrerViewModel.onResume()
    }
}

@Composable
private fun UpdateScreen(
    modifier: Modifier = Modifier
) {
    val updateState = remember { mutableStateOf<UpdateResult?>(null) }
    val context = LocalContext.current
    fun onResult(result: UpdateResult) {
        updateState.value = result
    }
    Box(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        when (val state = updateState.value) {
            UpdateResult.AlreadyUpdated -> AlreadyUpdatedView()
            is UpdateResult.Error -> ErrorView(message = state.message)
            is UpdateResult.NeedUpdate -> NeedUpdateView(
                targetVersion = state.targetVersion,
                onResult = {
                    BazaarUpdater.updateApplication(context = context)
                }
            )

            null -> CheckUpdateStateView(context = LocalContext.current, onResult = ::onResult)
        }
    }
}

@Composable
private fun AlreadyUpdatedView(modifier: Modifier = Modifier) {
    Text(modifier = modifier, text = stringResource(R.string.your_application_is_updated))
}

@Composable
private fun ErrorView(message: String, modifier: Modifier = Modifier) {
    Column {
        Text(modifier = modifier, text = message)
    }
}

@Composable
private fun NeedUpdateView(
    targetVersion: Long,
    modifier: Modifier = Modifier,
    onResult: (UpdateResult) -> Unit = {}
) {
    UpdateButton(
        context = LocalContext.current,
        text = stringResource(R.string.there_is_new_update_version, targetVersion),
        modifier = modifier,
        onResult = onResult,
    )
}

@Composable
private fun UpdateButton(
    context: Context,
    text: String,
    modifier: Modifier = Modifier,
    onResult: (UpdateResult) -> Unit = {}
) {
    Button(
        modifier = modifier,
        onClick = {
            checkUpdateState(
                context = context,
                onResult = onResult
            )
        }
    ) {
        Text(text = text)
    }
}

@Composable
private fun CheckUpdateStateView(context: Context, onResult: (UpdateResult) -> Unit) {
    UpdateButton(
        context = context,
        onResult = onResult,
        text = stringResource(R.string.check_update)
    )
}

@Composable
private fun ReferrerSdkInfo(
    error: String?,
    logs: String?,
    referrerContent: ReferrerDetails?,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(24.dp)) {
        HorizontalDivider()
        Text(text = "Referrer SDK Info: ")
        Text(text = "appVersion= ${referrerContent?.appVersion}")
        Text(text = "installBeginTimestampMilliseconds= ${referrerContent?.installBeginTimestampMilliseconds}")
        Text(text = "referrer= ${referrerContent?.referrer}")
        Text(text = "referrerClickTimestampMilliseconds= ${referrerContent?.referrerClickTimestampMilliseconds}")
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = "error= $error", color = Color.Red)
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Text(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            text = logs.toString()
        )
    }
}

private fun checkUpdateState(context: Context, onResult: (UpdateResult) -> Unit) {
    BazaarUpdater.getLastUpdateState(context = context) { result ->
        when (result) {
            UpdateResult.AlreadyUpdated -> onResult.invoke(result)
            is UpdateResult.Error -> onResult.invoke(result)
            is UpdateResult.NeedUpdate -> {
                onResult.invoke(result)
                result.targetVersion
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateScreenPreview() {
    BazaarupdaterSampleTheme {
        UpdateScreen()
    }
}