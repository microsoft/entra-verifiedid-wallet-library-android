package android.util

import com.microsoft.walletlibrary.identifier.HolderIdentifier
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk

object MockHolderIdentifier {

    /**
     * Makes a mock holder identifier
     */
    fun make(): HolderIdentifier {
        val holder: HolderIdentifier = mockk()
        every { holder.id } returns "did:example:test"
        every { holder.algorithm } returns "none"
        every { holder.method } returns "did:example"
        every { holder.keyReference } returns "key"
        val slot: CapturingSlot<ByteArray> = CapturingSlot()
        every { holder.sign(capture(slot)) } answers {
            slot.captured
        }
        return holder
    }
}