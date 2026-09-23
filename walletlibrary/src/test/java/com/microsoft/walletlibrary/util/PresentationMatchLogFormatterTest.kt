package com.microsoft.walletlibrary.util

import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.GroupConstraintOperator
import com.microsoft.walletlibrary.requests.requirements.constraints.VcPathRegexConstraint
import com.microsoft.walletlibrary.requests.requirements.constraints.VcTypeConstraint
import com.microsoft.walletlibrary.verifiedid.VerifiedId
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PresentationMatchLogFormatterTest {
    @Test
    fun format_NoTypeMatch_ReportsRequestedTypeAndCounts() {
        val candidate = mockk<VerifiedId>()
        every { candidate.types } returns listOf("OtherCredential")

        val result = PresentationMatchLogFormatter.format(
            VcTypeConstraint("FastFaceCheck"),
            listOf(candidate)
        )

        assertThat(result).contains(
            "\"event\":\"presentation_match_not_found\"",
            "\"candidate_count\":1",
            "\"kind\":\"type\"",
            "\"requested_type\":\"FastFaceCheck\"",
            "\"matched_count\":0"
        )
        assertThat(result).doesNotContain("OtherCredential")
    }

    @Test
    fun format_ClaimGroup_ReportsPathsWithoutFilterValue() {
        val secretPattern = "secret-issuer"
        val constraint = GroupConstraint(
            listOf(
                VcTypeConstraint("FastFaceCheck"),
                VcPathRegexConstraint(listOf("$.vc.credentialSubject.photo"), secretPattern)
            ),
            GroupConstraintOperator.ALL
        )

        val result = PresentationMatchLogFormatter.format(constraint, emptyList())

        assertThat(result).contains(
            "\"kind\":\"group\"",
            "\"operator\":\"ALL\"",
            "\"requested_type\":\"FastFaceCheck\"",
            "$.vc.credentialSubject.photo",
            "\"filter_present\":true"
        )
        assertThat(result).doesNotContain(secretPattern)
    }
}