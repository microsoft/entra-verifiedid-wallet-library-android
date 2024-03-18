package com.microsoft.walletlibrarydemo.feature.presentationlogic

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.microsoft.walletlibrary.VerifiedIdClient
import com.microsoft.walletlibrary.VerifiedIdClientBuilder
import com.microsoft.walletlibrary.requests.VerifiedIdRequest
import com.microsoft.walletlibrary.requests.input.VerifiedIdRequestURL
import com.microsoft.walletlibrary.requests.requirements.VerifiedIdRequirement
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import com.microsoft.walletlibrarydemo.db.VerifiedIdDatabase
import com.microsoft.walletlibrarydemo.db.entities.EncodedVerifiedId

class SampleViewModel(@SuppressLint("StaticFieldLeak") val context: Context) : ViewModel() {
    var verifiedIdRequest: VerifiedIdRequest<*>? = null
    var verifiedId: VerifiedId? = null
    private var verifiedIdClient: VerifiedIdClient
    private val verifiedIdDao = VerifiedIdDatabase.getInstance(context).verifiedIdDao()

    // VerifiedIdClientBuilder configures and returns a VerifiedIdClient.
    init {
        val builder = VerifiedIdClientBuilder(context)
        verifiedIdClient = builder.build()
    }

    enum class State(var value: String? = null) {
        INITIALIZED,
        CREATE_REQUEST_SUCCESS,
        ISSUANCE_SUCCESS,
        PRESENTATION_SUCCESS,
        ERROR
    }

    var state = State.INITIALIZED

    suspend fun initiateRequest(requestUrl: String) {
        val verifiedIdRequestUrl = VerifiedIdRequestURL(Uri.parse(requestUrl))

        // VerifiedIdClient creates a VerifiedIdRequest using the input provided above (VerifiedIdRequestURL).
        val requestResult = verifiedIdClient.createRequest(verifiedIdRequestUrl)
        requestResult.fold(
            onSuccess = {
                state = State.CREATE_REQUEST_SUCCESS
                verifiedIdRequest = it
            },
            onFailure = {
                populateErrorState(it.cause?.message ?: it.message)
            })
    }

    suspend fun completeIssuance() {
        verifiedIdRequest?.let { request ->
            // Completes an issuance request
            val issuanceResult = request.complete()
            issuanceResult.fold(
                onSuccess = { issuedVerifiedId ->
                    state = State.ISSUANCE_SUCCESS
                    verifiedId = issuedVerifiedId as VerifiedId
                    verifiedId?.let { encodeVerifiedId(it) }
                },
                onFailure = { exception -> populateErrorState(exception.cause?.message ?: exception.message) } )
        }
    }

    private suspend fun encodeVerifiedId(verifiedId: VerifiedId) {
        // Encodes a Verified Id
        val encodeResult = verifiedIdClient.encode(verifiedId)
        encodeResult.fold(
            onSuccess = {
                it.let { encodedVerifiedId ->
                    val vc = EncodedVerifiedId(verifiedId.id, encodedVerifiedId)
                    verifiedIdDao.insert(vc)
                }
            },
            onFailure = { populateErrorState(it.cause?.message ?: it.message) }
        )
    }

    suspend fun getVerifiedIds(): List<VerifiedId> {
        val encodedVerifiedIds = verifiedIdDao.queryVerifiedIds()
        val decodedVerifiedIds = ArrayList<VerifiedId>()
        encodedVerifiedIds.forEach { encodedVerifiedId ->
            // Decodes a Verified Id
            val decodeResult = verifiedIdClient.decodeVerifiedId(encodedVerifiedId.verifiedId)
            decodeResult.fold(
                onSuccess = {
                    it.let { decodedVerifiedId -> decodedVerifiedIds.add(decodedVerifiedId) }
                },
                onFailure = { populateErrorState(it.cause?.message ?: it.message) }
            )
        }
        return decodedVerifiedIds
    }

    suspend fun getMatchingVerifiedIds(requirement: VerifiedIdRequirement): List<VerifiedId> {
        val decodedVerifiedIds = getVerifiedIds()
        // The getMatches method filters a list of Verified Ids and returns only the ones that satisfy the VerifiedIdRequirement.
        return requirement.getMatches(decodedVerifiedIds)
    }

    suspend fun completePresentation() {
        verifiedIdRequest?.let {
            // Completes a presentation request
            val presentationResult = it.complete()
            presentationResult.fold(
                onSuccess = { state = State.PRESENTATION_SUCCESS },
                onFailure = { exception -> populateErrorState(exception.cause?.message ?: exception.message) })
        }
    }

    private fun populateErrorState(errorMessage: String?) {
        state = State.ERROR
        state.value = errorMessage
    }
}