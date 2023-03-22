package com.microsoft.walletlibrary

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.walletlibrary.util.WalletLibraryLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VerifiedIdClientBuilderTest {
    private lateinit var verifiedIdClientBuilder: VerifiedIdClientBuilder

    init {
        setupInput(0)
    }

    private fun setupInput(logConsumerCountToAddToExisting: Int) {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        verifiedIdClientBuilder = VerifiedIdClientBuilder(context)
        mockLogConsumer(logConsumerCountToAddToExisting)
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
    fun builder1_WithNoLogConsumers_ReturnsVerifiedIdWithNoLogConsumer() {
        // Arrange
        setupInput(0)

        // Act
        val actualResult = verifiedIdClientBuilder.build()

        // Assert
        assertThat(actualResult.requestHandlerFactory.requestHandlers.size).isEqualTo(1)
        assertThat(actualResult.requestResolverFactory.requestResolvers.size).isEqualTo(1)
        assertThat(actualResult.logger.CONSUMERS).isEmpty()
    }

    @Test
    fun builder2_WithOneLogConsumer_ReturnsVerifiedIdWithOneLogConsumer() {
        // Arrange
        setupInput(1)

        // Act
        val actualResult = verifiedIdClientBuilder.build()

        // Assert
        assertThat(actualResult.requestHandlerFactory.requestHandlers.size).isEqualTo(1)
        assertThat(actualResult.requestResolverFactory.requestResolvers.size).isEqualTo(1)
        assertThat(actualResult.logger.CONSUMERS.size).isEqualTo(1)
    }

    @Test
    fun builder3_WithMultipleLogConsumers_ReturnsVerifiedIdWithMultipleLogConsumers() {
        // Arrange
        setupInput(3)

        // Act
        val actualResult = verifiedIdClientBuilder.build()

        // Assert
        assertThat(actualResult.requestHandlerFactory.requestHandlers.size).isEqualTo(1)
        assertThat(actualResult.requestResolverFactory.requestResolvers.size).isEqualTo(1)
        assertThat(actualResult.logger.CONSUMERS.size).isEqualTo(4)
    }
}