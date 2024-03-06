package com.microsoft.walletlibrary.util.http

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.fail
import org.junit.Test

internal class URLFormEncodingTest {
    @Test
    fun testEncode_withStrings_shouldEncode() {
        val actual = URLFormEncoding.encode(mapOf(
            "Test 1" to "abc",
            "Test 2" to "123"
        ))
        assertThat(actual).isNotEmpty()
        val actualString = actual.toString(Charsets.UTF_8)
        assertThat(actualString).isNotEmpty()
        assertThat(actualString).isEqualTo("Test+1=abc&Test+2=123")
    }

    @Test
    fun testEncode_withArray_shouldEncodeValueMultipleTimes() {
        val actual = URLFormEncoding.encode(mapOf<String, Any>(
            "Test 1" to arrayOf("A", "B", "C"),
            "Test 2" to "123"
        ))
        assertThat(actual).isNotEmpty()
        val actualString = actual.toString(Charsets.UTF_8)
        assertThat(actualString).isNotEmpty()
        assertThat(actualString).isEqualTo("Test+1=A&Test+1=B&Test+1=C&Test+2=123")
    }

    @Test
    fun testEncode_withNumber_shouldFail() {
        try {
            URLFormEncoding.encode(mapOf<String, Any>(
                "Test 1" to arrayOf("A", "B", "C"),
                "Test 2" to "123",
                "Failure" to 1
            ))
            fail("Should have failed encoding.")
        } catch (exception: URLFormEncoding.URLEncodingException) {
            assertThat(exception.keyName).isEqualTo("Failure")
        } catch (exception: Exception) {
            fail("Did not throw the expected error")
        }
    }

    @Test
    fun testEncode_withNumberInArray_shouldFail() {
        try {
            URLFormEncoding.encode(mapOf<String, Any>(
                "Test 1" to arrayOf("A", "B", "C", 1),
                "Test 2" to "123"
            ))
            fail("Should have failed encoding.")
        } catch (exception: URLFormEncoding.URLEncodingException) {
            assertThat(exception.keyName).isEqualTo("Test 1")
        } catch (exception: Exception) {
            fail("Did not throw the expected error")
        }
    }

}