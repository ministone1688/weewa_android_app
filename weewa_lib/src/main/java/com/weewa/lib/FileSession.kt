package com.weewa.lib

import android.util.Log
import com.weewa.lib.Prefs.downloadPath
import kotlinx.serialization.Serializable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.Timer
import java.util.TimerTask

interface IFileSessionDelegate {
    fun onErrorOccurred(session: FileSession, errMsg: String)
    fun onSenderEnded(session: FileSession)
    fun onDualEnded(session: FileSession)
    fun onUpdateProgress(session: FileSession, progress: Double)
}

data class FileMetadata(
    val filename: String,
    val size: Long,
    var start: Long,
    var pos: Long,
    var startTime: Long,
    var endTime: Long
)

@Serializable
data class MiniFileMeta(
    val name: String,
    val size: Long
)

@Serializable
data class FileResume(
    val name: String,
    val start: Long
)

@Serializable
data class Handshake2Packet(
    val device_name: String,
    val device_type: String,
    val encrypt: Boolean,
    val files: List<MiniFileMeta>,
    val request_send: Boolean? = null,
    val dir: String? = null
)

@Serializable
data class Handshake2Response(
    val r: String,
    var files: List<FileResume>? = null
)

@Serializable
data class TransferCompleteResponse(
    val r: String
)

open class FileSession(socket: Socket, remoteDir: String = "", encrypt: Boolean = false) {
    companion object {
        var nextId: Int = 1
    }

    enum class State {
        HANDSHAKE1,
        HANDSHAKE2,
        TRANSFERRING,
        FINISHED
    }

    protected val _crypto = Crypto()
    protected var _encryption = false
    protected var _socket: Socket? = null
    protected var _state: State = State.HANDSHAKE1
    private var _reader: DataInputStream? = null
    private var _writer: DataOutputStream? = null
    protected var _delegate: IFileSessionDelegate? = null
    private lateinit var _thread: Thread
    protected var _running = false
    private var _readBuffer = ByteArray(Const.TRANSFER_QUANTA * 2)
    private var _readBufLen = 0
    protected val tag = javaClass.simpleName
    private var _handshakeTimeoutTimer: Timer? = null
    protected var _progress = 0
    protected var _transferQ = mutableListOf<FileMetadata>()
    protected var _totalSize: Long = 0
    protected var _transferredSize: Long = 0
    protected var _id: Int = 0
    protected var _remoteDir = ""
    protected var _downloadPath = Prefs.defaultPreference(WeewaLib.shared().getContext()).downloadPath

    init {
        _encryption = encrypt
        _socket = socket
        _remoteDir = remoteDir
        _reader = DataInputStream(_socket?.getInputStream())
        _writer = DataOutputStream(_socket?.getOutputStream())
    }

    fun abort() {
        teardown()
    }

    fun setDelegate(d: IFileSessionDelegate) {
        _delegate = d
    }

    fun getId(): Int {
        return _id
    }

    fun setId(id: Int) {
        _id = id
    }

    open fun isSender(): Boolean {
        return false
    }

    protected open fun teardown() {
        try {
            _reader?.close()
            _writer?.close()
            _socket?.close()
            _reader = null
            _writer = null
            _socket = null
        } catch(e: Exception) {
        }
    }

    fun start() {
        Log.d(tag, "new transfer session start, sending public key")
        _writer?.write(_crypto.localPublicKey())
        _handshakeTimeoutTimer = Timer()
        _handshakeTimeoutTimer?.schedule(object : TimerTask() {
            override fun run() {
                handshakeTimeout()
            }
        }, 5000)
        startSocketTread()
    }

    private fun startSocketTread() {
        _thread = Thread(Runnable {
            loop()
        })
        _running = true
        _thread.start()
    }

    private fun loop() {
        while(_running) {
            // read bytes
            try {
                val read = _reader!!.read(_readBuffer, _readBufLen, _readBuffer.size - _readBufLen)
                if(read >= 0) {
                    _readBufLen += read
                } else {
                    Log.d(tag, "[$_id] socket read data EOF")
                    _delegate?.onErrorOccurred(this, "socket EOF")
                    return
                }
            } catch(e: Throwable) {
                if(_state != State.FINISHED) {
                    val msg = if(e.localizedMessage != null) e.localizedMessage else "unknown reason"
                    Log.d(tag, "[$_id] socket read data failed: $msg")
                    _delegate?.onErrorOccurred(this, msg)
                    return
                }
            }

            // if in handshake1, check key
            if(_state == State.HANDSHAKE1) {
                // check if buffer has public key, if not, failed
                if(_readBufLen < _crypto.publicKeySize()) {
                    Log.d(tag, "[$_id] transfer handshake failed, no public key detected")
                    _delegate?.onErrorOccurred(this, "transfer handshake failed, no public key detected")
                    return
                }

                // read remote public key
                val keySize: Int = _crypto.publicKeySize().toInt()
                val publicKey = _readBuffer.slice(0 until keySize).toByteArray()

                // cut read content
                _readBuffer = _readBuffer.copyInto(_readBuffer, 0, keySize, _readBufLen)
                _readBufLen -= keySize

                // save public key
                try {
                    _crypto.setRemotePublicKey(publicKey)
                } catch (e: Exception) {
                    _delegate?.onErrorOccurred(this, e.localizedMessage)
                    return
                }

                // change state
                _state = State.HANDSHAKE2
                _handshakeTimeoutTimer?.cancel()
                _handshakeTimeoutTimer = null

                // notify finish
                handshake1Finished()
            }

            // read buffer
            while(_readBufLen > 0) {
                // first two bytes are size
                if (_readBufLen < 2)
                    break

                // check if buffer has enough bytes
                var size = (_readBuffer[0].toInt() and 0xff) shl 8
                size = size or (_readBuffer[1].toInt() and 0xff)
                if(_readBufLen < size + 2)
                    break

                // read content, skip size bytes
                var data = _readBuffer.slice(2..(size + 1)).toByteArray()

                // cut read content
                _readBuffer = _readBuffer.copyInto(_readBuffer, 0, size + 2, _readBufLen)
                _readBufLen -= size + 2

                // decrypt data
                if(_encryption && _state == State.TRANSFERRING) {
                    try {
                        data = _crypto.decrypt(data)
                    } catch (e: Exception) {
                        _delegate?.onErrorOccurred(this, e.localizedMessage)
                        return
                    }
                }

                // process decrypted data
                processReceivedData(data)
            }
        }
    }

    protected open fun processReceivedData(data: ByteArray) {
        // subclass can implement
    }

    private fun handshakeTimeout() {
        teardown()
        _delegate?.onErrorOccurred(this, "session handshake timeout, abort")
    }

    protected open fun response(errMsg: String) {
        // subclass should implement
    }

    protected fun encryptAndSend(data: ByteArray) {
        val sendData = if(_encryption && _state == State.TRANSFERRING) _crypto.encrypt(data) else data
        val lenData = ByteArray(2)
        val size = sendData.size
        lenData[0] = ((size ushr 8) and 0xff).toByte()
        lenData[1] = (size and 0xff).toByte()
        _writer?.write(lenData)
        _writer?.write(sendData)
    }

    protected open fun handshake1Finished() {
        // subclass can implement
    }
}