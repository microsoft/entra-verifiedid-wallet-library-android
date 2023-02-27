package com.microsoft.walletlibrary

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiedIdClientBuilderTest {
    private lateinit var verifiedIdClientBuilder: VerifiedIdClientBuilder

    init {
        setupInput(0)
    }

    private fun setupInput(logConsumerCount: Int) {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        verifiedIdClientBuilder = VerifiedIdClientBuilder(context)
        mockLogConsumer(logConsumerCount)
    }

    private fun mockLogConsumer(logConsumerCount: Int) {
        class MockLogConsumer: WalletLibraryLogger.Consumer {
            override fun log(
                logLevel: WalletLibraryLogger.Level,
                message: String,
                throwable: Throwable?,
                tag: String
            ) {}

            override fun event(name: String, properties: Map<String, String>?) {}
        }

        for (consumerCount in 0 until logConsumerCount) {
            verifiedIdClientBuilder.with(MockLogConsumer())
        }
    }

    @Test
    fun builder_WithNoLogConsumers_ReturnsVerifiedIdWithNoLogConsumer() {
        val actualResult = verifiedIdClientBuilder.build()
        assertThat(actualResult.requestHandlerFactory.requestHandlers.size).isEqualTo(1)
        assertThat(actualResult.requestResolverFactory.requestResolvers.size).isEqualTo(1)
        assertThat(actualResult.logger.CONSUMERS).isEmpty()
    }

    @Test
    fun builder_WithOneLogConsumer_ReturnsVerifiedIdWithOneLogConsumer() {
        setupInput(1)
        val actualResult = verifiedIdClientBuilder.build()
        assertThat(actualResult.requestHandlerFactory.requestHandlers.size).isEqualTo(1)
        assertThat(actualResult.requestResolverFactory.requestResolvers.size).isEqualTo(1)
        assertThat(actualResult.logger.CONSUMERS.size).isEqualTo(1)
    }

    @Test
    fun builder_WithMultipleLogConsumers_ReturnsVerifiedIdWithMultipleLogConsumers() {
        setupInput(3)
        val actualResult = verifiedIdClientBuilder.build()
        assertThat(actualResult.requestHandlerFactory.requestHandlers.size).isEqualTo(1)
        assertThat(actualResult.requestResolverFactory.requestResolvers.size).isEqualTo(1)
        assertThat(actualResult.logger.CONSUMERS.size).isEqualTo(3)
    }
}