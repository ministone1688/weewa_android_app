package com.weewa.lib

import java.lang.Exception

class Crypto {
    @Throws(Exception::class)
    external fun encrypt(data: ByteArray): ByteArray

    @Throws(Exception::class)
    external fun decrypt(data: ByteArray): ByteArray

    external fun localPublicKey(): ByteArray
    external fun publicKeySize(): Long

    @Throws(Exception::class)
    external fun setRemotePublicKey(key: ByteArray)
}