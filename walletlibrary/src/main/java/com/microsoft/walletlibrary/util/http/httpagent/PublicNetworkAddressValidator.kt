package com.microsoft.walletlibrary.util.http.httpagent

import java.net.InetAddress

internal object PublicNetworkAddressValidator {
    fun isPublic(address: InetAddress): Boolean {
        if (address.isAnyLocalAddress ||
            address.isLoopbackAddress ||
            address.isLinkLocalAddress ||
            address.isSiteLocalAddress ||
            address.isMulticastAddress
        ) {
            return false
        }

        val bytes = address.address
        return when (bytes.size) {
            4 -> isPublicIpv4(bytes)
            16 -> {
                if (isIpv4MappedIpv6(bytes)) {
                    isPublicIpv4(bytes.copyOfRange(12, 16))
                } else {
                    isPublicIpv6(bytes)
                }
            }
            else -> false
        }
    }

    private fun isPublicIpv4(bytes: ByteArray): Boolean {
        val first = bytes[0].unsigned()
        val second = bytes[1].unsigned()
        val third = bytes[2].unsigned()

        return when {
            first == 0 || first == 10 || first == 127 -> false
            first == 100 && second in 64..127 -> false
            first == 169 && second == 254 -> false
            first == 172 && second in 16..31 -> false
            first == 192 && second == 0 && third == 0 -> false
            first == 192 && second == 0 && third == 2 -> false
            first == 192 && second == 168 -> false
            first == 198 && second in 18..19 -> false
            first == 198 && second == 51 && third == 100 -> false
            first == 203 && second == 0 && third == 113 -> false
            first >= 224 -> false
            else -> true
        }
    }

    private fun isPublicIpv6(bytes: ByteArray): Boolean {
        val first = bytes[0].unsigned()
        val second = bytes[1].unsigned()
        return when {
            first and 0xfe == 0xfc -> false
            first == 0xfe && second and 0xc0 == 0x80 -> false
            first == 0xff -> false
            first == 0x20 &&
                second == 0x01 &&
                bytes[2].unsigned() == 0x0d &&
                bytes[3].unsigned() == 0xb8 -> false
            else -> true
        }
    }

    private fun isIpv4MappedIpv6(bytes: ByteArray): Boolean {
        return bytes.take(10).all { it == 0.toByte() } &&
            bytes[10] == 0xff.toByte() &&
            bytes[11] == 0xff.toByte()
    }

    private fun Byte.unsigned(): Int = toInt() and 0xff
}
