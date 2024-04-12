package com.microsoft.walletlibrary.util.http

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

internal class URLFormEncodingTest {
    @Test
    fun testEncode_withStrings_shouldEncode() {
        // Arrange
        val input = mapOf("Test 1" to "abc", "Test 2" to "123")

        // Act
        val actual = URLFormEncoding.encode(input)

        // Assert
        assertThat(actual).isNotEmpty()
        val actualString = actual.toString(Charsets.UTF_8)
        assertThat(actualString).isNotEmpty()
        assertThat(actualString).isEqualTo("Test+1=abc&Test+2=123")
    }

    @Test
    fun testEncode_withNullValue_throwsException() {
        // Arrange
        val input = mapOf("Test 1" to null)

        //Act and Assert
        assertThatThrownBy {
            URLFormEncoding.encode(input)
        }.isInstanceOf(URLFormEncoding.URLEncodingException::class.java)
    }

    @Test
    fun testEncode_withArray_shouldEncodeValueMultipleTimes() {
        // Arrange
        val input = mapOf("Test 1" to arrayOf("A", "B", "C"), "Test 2" to "123")

        // Act
        val actual = URLFormEncoding.encode(input)

        // Assert
        assertThat(actual).isNotEmpty()
        val actualString = actual.toString(Charsets.UTF_8)
        assertThat(actualString).isNotEmpty()
        assertThat(actualString).isEqualTo("Test+1=A&Test+1=B&Test+1=C&Test+2=123")
    }

    @Test
    fun testEncode_withArrayWithNullAndString_shouldSkipNullAndEncodeRest() {
        // Arrange
        val input = mapOf("Test 1" to arrayOf("A", "B", null), "Test 2" to "123")

        // Act
        val actual = URLFormEncoding.encode(input)

        // Assert
        assertThat(actual).isNotEmpty()
        val actualString = actual.toString(Charsets.UTF_8)
        assertThat(actualString).isNotEmpty()
        assertThat(actualString).isEqualTo("Test+1=A&Test+1=B&Test+2=123")
    }

    @Test
    fun testEncode_withArrayWithNullAndNumber_throwsException() {
        // Arrange
        val input = mapOf("Test 1" to arrayOf("A", "B", 1), "Test 2" to "123")

        // Act and Assert
        assertThatThrownBy {
            URLFormEncoding.encode(input)
        }.isInstanceOf(URLFormEncoding.URLEncodingException::class.java)
    }

    @Test
    fun testEncode_withNumber_ThrowsException() {
        // Arrange
        val input = mapOf<String, Any>(
            "Test 1" to arrayOf("A", "B", "C"),
            "Test 2" to "123",
            "Failure" to 1
        )

        // Act and Assert
        assertThatThrownBy {
            URLFormEncoding.encode(input)
        }.isInstanceOf(URLFormEncoding.URLEncodingException::class.java)
    }

    @Test
    fun testEncode_withNumberInArray_ThrowsException() {
        // Arrange
        val input = mapOf<String, Any>(
            "Test 1" to arrayOf("A", "B", "C", 1),
            "Test 2" to "123"
        )

        // Act and Assert
        assertThatThrownBy {
            URLFormEncoding.encode(input)
        }.isInstanceOf(URLFormEncoding.URLEncodingException::class.java)
    }

<<<<<<< HEAD
    @Test
    fun testEncode_withPreAuthTokenRequest_ReturnsEncodedString() {
        // Arrange
        val input =
            mapOf("grant_type" to "grant type", "pre-authorized_code" to "code", "tx_code" to "pin")

        // Act
        val actual = URLFormEncoding.encode(input)

        // Assert
        assertThat(actual).isNotEmpty()
        val actualString = actual.toString(Charsets.UTF_8)
        assertThat(actualString).isNotEmpty()
        assertThat(actualString).isEqualTo("grant_type=grant+type&pre-authorized_code=code&tx_code=pin")
    }
=======
>>>>>>> logirvin/facecheck-v2
}