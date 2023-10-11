package com.weewa.lib

import android.os.Build
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import java.net.Socket

class FileSender(socket: Socket, files: List<String>, remoteDir: String = "", encrypt: Boolean = false) :
    FileSession(socket, remoteDir, encrypt) {
    private var _files = mutableListOf<RandomAccessFile>()
    private var _fileBuf = ByteArray(Const.TRANSFER_QUANTA)
    private lateinit var _sendThread: Thread

    init {
        // create file list
        for (filepath in files) {
            val file = File(filepath)
            if (file.exists() && file.length() > 0) {
                val size = file.length()
                _totalSize += size
                _files.add(RandomAccessFile(file, "r"))
                _transferQ.add(
                    FileMetadata(
                        file.name,
                        size,
                        0,
                        0,
                        0,
                        0
                    )
                )
            }
        }
    }

    override fun isSender(): Boolean {
        return true
    }

    override fun handshake1Finished() {
        // check empty
        if (_files.isEmpty()) {
            _delegate?.onSenderEnded(this)
            return
        }

        // build file list
        val files = _transferQ.map {
            MiniFileMeta(it.filename, it.size)
        }
        var packet = Handshake2Packet(
            Build.MODEL,
            "Android",
            _encryption,
            files,
            false,
            _remoteDir
        )

        // send file list
        Log.d(tag, "sender start sending file list")
        val jsonData = Json.encodeToString(Handshake2Packet.serializer(), packet)
        encryptAndSend(jsonData.toByteArray())
    }

    override fun processReceivedData(data: ByteArray) {
        if(_state == State.HANDSHAKE2) {
            try {
                // receiver should return a json response
                val resp = Json.decodeFromString<Handshake2Response>(String(data))

                // response should be ok, otherwise failed
                if(resp.r != "ok") {
                    _delegate?.onErrorOccurred(this, "Handshake failed.")
                    return
                }

                // check files empty
                if(resp.files.isNullOrEmpty()) {
                    _delegate?.onErrorOccurred(this, "Handshake failed.")
                    return
                }

                // check read start
                resp.files!!.forEach {
                    // find file meta by name
                    var idx = -1
                    _transferQ.forEachIndexed { index, meta ->
                        if(meta.filename == it.name) {
                            idx = index
                            return@forEachIndexed
                        }
                    }

                    // if found, set start
                    if(idx != -1) {
                        _transferQ[idx].start = it.start
                        _transferQ[idx].pos = it.start
                        _transferQ[idx].startTime = System.currentTimeMillis()
                        _files[idx].seek(it.start)
                        _transferredSize += it.start
                    }
                }
            } catch(e: Exception) {
                _delegate?.onErrorOccurred(this, e.localizedMessage)
                return
            }

            // enter transfer state
            _state = State.TRANSFERRING
            Log.d(tag, "[$_id] receiver accepted file list, start transferring")

            // start to send file bytes
            startSendThread()
        } else if(_state == State.TRANSFERRING) {
            // receiver should return a json response for final confirm after receive all data
            try {
                Json.decodeFromString<TransferCompleteResponse>(String(data))

                // finished
                _state = State.FINISHED
                Log.d(tag, "[$_id] all file sent, done")
                teardown()
                _delegate?.onSenderEnded(this)
            } catch(e: Exception) {
                _delegate?.onErrorOccurred(this, e.localizedMessage)
                return
            }
        }
    }

    override fun teardown() {
        super.teardown()
        _files.forEach { it.close() }
    }

    private fun startSendThread() {
        _sendThread = Thread(Runnable {
            sendFileChunk()
        })
        _sendThread.start()
    }

    private fun sendFileChunk() {
        while(_running && _state == State.TRANSFERRING) {
            // clear sent file
            while(_transferQ.isNotEmpty()) {
                var meta = _transferQ.first()
                if(meta.pos >= meta.size) {
                    meta.endTime = System.currentTimeMillis()
                    Log.d(
                        tag,
                        String.format(
                            "[$_id] file ${meta.filename} sent(%.03fs - %.02fMB/s), check next file",
                            (meta.endTime - meta.startTime) / 1000.0,
                            (meta.size - meta.start) * 1000.0 / (meta.endTime - meta.startTime) / 1024.0 / 1024.0
                        )
                    )
                    _transferQ.removeFirst()
                    _files.first().close()
                    _files.removeFirst()
                }
            }

            // if no more file, set state finished
            if(_transferQ.isEmpty())
                return

            // send current file data
            val curFile = _files.first()
            var curMetadata = _transferQ.first()
            val read = curFile.read(_fileBuf, 0, Const.TRANSFER_QUANTA)
            encryptAndSend(_fileBuf.copyOfRange(0, read))
            _transferredSize += read
            curMetadata.pos += read

            // update progress
            val doubleProgress = _transferredSize.toDouble() / _totalSize.toDouble()
            val intProgress = (doubleProgress * 100).toInt()
            if(intProgress != _progress) {
                _progress = intProgress
                Log.d(tag, "[$_id] transfer progress: $_progress%")
            }
        }
    }
}