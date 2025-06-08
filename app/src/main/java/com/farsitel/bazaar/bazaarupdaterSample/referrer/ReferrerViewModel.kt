package com.farsitel.bazaar.bazaarupdaterSample.referrer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cafebazaar.referrersdk.ReferrerClient
import com.cafebazaar.referrersdk.model.ReferrerDetails
import com.cafebazaar.servicebase.state.ClientError
import com.cafebazaar.servicebase.state.ClientStateListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReferrerViewModel(application: Application) : AndroidViewModel(application) {

    private val referrerClient: ReferrerClient = ReferrerClient.getClient(application)
    private val stateListener = object : ClientStateListener {

        override fun onReady() {
            viewModelScope.launch {
                Log.e("BZRTAGD", "ReferrerViewModel onReady")
                getAndConsumeReferrer()
            }
        }

        override fun onError(clientError: ClientError) {
            viewModelScope.launch {
                Log.e("BZRTAGD", "onError clientError=$clientError")
                handleReferrerError(clientError)
            }
        }
    }
    private val _referrerContent = MutableStateFlow<ReferrerDetails?>(null)
    val referrerContent: StateFlow<ReferrerDetails?> = _referrerContent

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    fun onResume() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.e("BZRTAGD", "onResume startConnection")
            referrerClient.startConnection(stateListener)
        }
    }

    private suspend fun handleReferrerError(referrerError: ClientError) {
        Log.e("BZRTAGD", "handleReferrerError referrerError=$referrerError, message=${referrerError.message}")
        when (referrerError) {
            ClientError.ERROR_BAZAAR_IS_NOT_INSTALL,
            ClientError.ERROR_BAZAAR_IS_NOT_COMPATIBLE,
            ClientError.ERROR_SDK_COULD_NOT_CONNECT -> {
                _errorMessage.emit(referrerError.message)
            }

            ClientError.ERROR_SDK_IS_STARTED -> {
                _errorMessage.emit(referrerError.message)
            }

            ClientError.ERROR_DURING_GETTING_REFERRER_DETAILS,
            ClientError.ERROR_DURING_CONSUMING_REFERRER -> {
                _errorMessage.emit(referrerError.message)
            }
        }
    }

    private suspend fun getAndConsumeReferrer() {
        Log.e("BZRTAGD", "getAndConsumeReferrer")

        referrerClient.getReferrerDetails()?.let { referrerDetails ->
            Log.e("BZRTAGD", "getAndConsumeReferrer referrerDetails=$referrerDetails")

            _referrerContent.emit(referrerDetails)
            referrerClient.consumeReferrer(referrerDetails.installBeginTimestampMilliseconds)
            referrerClient.endConnection()
        } ?: run {
            Log.e("BZRTAGD", "Referrer details in empty")
            _errorMessage.emit("THERE IS NO REFERRER")
        }
    }
}