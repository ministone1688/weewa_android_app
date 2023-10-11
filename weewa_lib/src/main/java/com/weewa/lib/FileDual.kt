package com.weewa.lib

import android.os.Build
import android.util.Log
import com.weewa.lib.Prefs.downloadPath
import kotlinx.serialization.json.Json
import java.io.File
import java.io.RandomAccessFile
import java.net.Socket
import kotlin.math.min

class FileDual(socket: Socket, remoteDir: String = ""): FileSession(socket, remoteDir) {
    private var _files = mutableListOf<RandomAccessFile>()
    private var _recvFiles = mutableListOf<String>()
    private var _sendMode = false
    private var _requestSend = false
    private lateinit var _sendThread: Thread
    private var _fileBuf = ByteArray(Const.TRANSFER_QUANTA)

    constructor(socket: Socket, files: List<String>, remoteDir: String = "", encrypt: Boolean = false): this(socket, remoteDir) {
        _recvFiles.addAll(files)
        _encryption = encrypt
        _requestSend = true
    }

    override fun response(errMsg: String) {
        var resp = Handshake2Response(errMsg)
        if(errMsg == "ok") {
            // put files array tell sender resume point
            resp.files = _transferQ.map {
                FileResume(it.filename, it.start)
            }

            // set state
            _state = State.TRANSFERRING
            Log.d(tag, "[$_id] file list accepted, start transferring")
            openNextFile()
        } else {
            Log.d(tag, "[$_id] file list rejected, end transfer")
            _delegate?.onDualEnded(this)
        }

        // send
        val jsonData = Json.encodeToString(Handshake2Response.serializer(), resp)
        encryptAndSend(jsonData.toByteArray())
    }

    override fun handshake1Finished() {
        // if request send flag is set, request remote to send file, remote will become a sender
        if(_requestSend) {
            // build file list
            val req = Handshake2Packet(
                Build.MODEL,
                "Android",
                _encryption,
                _recvFiles.map { MiniFileMeta(it, 0) },
                true,
                _remoteDir
            )
            val jsonData = Json.encodeToString(Handshake2Packet.serializer(), req)
            encryptAndSend(jsonData.toByteArray())
        }
    }

    private fun shakeAgainAsSender() {
        // build file list
        val files = _transferQ.map {
            MiniFileMeta(it.filename, it.size)
        }
        var packet = Handshake2Packet(
            Build.MODEL,
            "Android",
            _encryption,
            files
        )

        // send file list
        Log.d(tag, "[$_id] dual as sender, start sending file list")
        val jsonData = Json.encodeToString(Handshake2Packet.serializer(), packet)
        encryptAndSend(jsonData.toByteArray())
    }

    private fun handleShake2AsSender(data: ByteArray) {
        try {
            // receiver should return a json response
            val resp = Json.decodeFromString<Handshake2Response>(String(data))

            // response should be 1, otherwise failed
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
            _delegate?.onErrorOccurred(this, "Handshake failed.")
        }

        // enter transfer state
        _state = State.TRANSFERRING
        Log.d(tag, "[$_id] receiver accepted file list, start transferring")

        // start to send file bytes
        startSendThread()
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

    private fun handleShake2ToDecideRole(data: ByteArray) {
        try {
            // handshake phase 2, expect a json describing file list
            val packet = Json.decodeFromString<Handshake2Packet>(String(data))

            // check device name
            if(packet.device_name.isEmpty()) {
                _delegate?.onDualEnded(this)
                return
            }

            // check encrypt
            _encryption = packet.encrypt

            // check request send flag
            if(packet.request_send != null && packet.request_send) {
                Log.d(tag, "[$_id] peer request send file, promote self as sender")
                _sendMode = true
            }

            // check if file list is empty
            if(packet.files.isEmpty()) {
                _delegate?.onDualEnded(this)
                return
            }

            // check if set directory
            val downloadPath = if(packet.dir == null) Prefs.defaultPreference(WeewaLib.shared().getContext()).downloadPath else packet.dir!!.ifEmpty { Prefs.defaultPreference(WeewaLib.shared().getContext()).downloadPath }

            // check download path
            val downloadPathDir = File(downloadPath)
            if(!downloadPathDir.exists() && !downloadPathDir.mkdirs()) {
                response("Cannot create download path")
                return
            }
            if(!downloadPathDir.canWrite()) {
                response("Download path is not writable")
                return
            }

            // if send mode, we will send file list to peer
            // if not, response with file resume
            if(_sendMode) {
                packet.files.forEach {
                    // check if file exist
                    val file = if(it.name.startsWith("/")) File(it.name) else File(downloadPathDir, it.name)
                    if(file.exists() && file.length() > 0) {
                        val size = file.length()
                        _totalSize += size
                        _transferQ.add(FileMetadata(if(it.name.startsWith("/")) it.name.substring(1) else it.name, size, 0, 0, 0, 0))
                        _files.add(RandomAccessFile(file, "r"))
                    }
                }

                // check empty
                if(_files.isEmpty()) {
                    _delegate?.onDualEnded(this)
                    return
                }

                // redo handshake2 as sender
                shakeAgainAsSender()
            } else {
                // check every file name and size
                packet.files.forEach {
                    // check file
                    val file = File(downloadPathDir, it.name)
                    if(!file.parentFile.exists()) {
                        file.parentFile.mkdirs()
                    }
                    val start: Long = if(file.exists()) file.length() else 0
                    _transferredSize += start
                    _files.add(RandomAccessFile(file, "rw"))

                    // save file meta info
                    _totalSize += it.size
                    _transferQ.add(FileMetadata(it.name, it.size, start, start, 0, 0))
                }

                // trigger event
                Log.d(tag, "[$_id] file list received, file count: ${_transferQ.size}, total size: $_totalSize, enable encrypt: ${if(_encryption) "true" else "false"}")

                // auto accept
                response("ok")
            }
        } catch(e: Exception) {
            Log.d(tag, "[$_id] error when handleShake2ToDecideRole: ${e.localizedMessage}")
            _delegate?.onErrorOccurred(this, e.localizedMessage)
        }
    }

    override fun processReceivedData(data: ByteArray) {
        if (_state == State.HANDSHAKE2) {
            if(_sendMode) {
                handleShake2AsSender(data);
            } else {
                handleShake2ToDecideRole(data);
            }

        } else if (_state == State.TRANSFERRING) {
            if(_sendMode) {
                handleTransferAsSender(data);
            } else {
                handleTransferAsReceiver(data);
            }
        }
    }

    private fun handleTransferAsSender(data: ByteArray) {
        try {
            // receiver should return a json response for final confirm after receive all data
            val resp = Json.decodeFromString<TransferCompleteResponse>(data.toString())

            // finished
            _state = State.FINISHED
            Log.d(tag, "[$_id] all file sent, done")
            teardown()
            _delegate?.onDualEnded(this)
        } catch(e: Exception) {
            _delegate?.onErrorOccurred(this, e.localizedMessage)
        }
    }

    private fun handleTransferAsReceiver(data: ByteArray) {
        // accumulate transferred size
        _transferredSize += data.size

        // update progress
        val doubleProgress = _transferredSize.toDouble() / _totalSize.toDouble()
        val intProgress = (doubleProgress * 100).toInt()
        if(intProgress != _progress) {
            _progress = intProgress
            Log.d(tag, "[$_id] transfer progress: $_progress%")
        }
        _delegate?.onUpdateProgress(this, doubleProgress)

        // process this data
        var off: Long = 0
        while(off < data.size) {
            // write data into current file
            val meta = _transferQ.first()
            val curFile = _files.first()
            val writeSize = min(meta.size - meta.pos, data.size.toLong() - off)
            curFile.write(data, 0, writeSize.toInt())

            // reduce written bytes
            meta.pos += writeSize
            off += writeSize

            // if this file finished, receive next file
            if (meta.pos >= meta.size) {
                meta.endTime = System.currentTimeMillis()
                Log.d(
                    tag,
                    String.format(
                        "[$_id] file ${meta.filename} finished(%.03fs - %.02fMB/s), check next file",
                        (meta.endTime - meta.startTime) / 1000.0,
                        (meta.size - meta.start) * 1000.0 / (meta.endTime - meta.startTime) / 1024.0 / 1024.0
                    )
                )
                curFile.close()
                _transferQ.removeFirst()
                _files.removeFirst()
                openNextFile()
            }
        }
    }

    override fun isSender(): Boolean {
        return _sendMode
    }

    private fun openNextFile() {
        // if transfer queue is not empty
        while(_transferQ.isNotEmpty()) {
            // get current file
            val meta = _transferQ.first()
            val downloadPath = Prefs.defaultPreference(WeewaLib.shared().getContext()).downloadPath
            val file = File(downloadPath, meta.filename)
            Log.d(tag, "[$_id] receive next file to ${file.absolutePath}, resume from ${meta.start} bytes")
            _files.first().seek(meta.start)

            // set start time
            meta.startTime = System.currentTimeMillis()

            // if current file is done, pop from queue
            // if not done, break loop
            if (meta.pos < meta.size) {
                break;
            } else {
                _files.first().close()
                _transferQ.removeFirst()
                _files.removeFirst()
            }
        }

        // if queue is empty
        if (_transferQ.isEmpty()) {
            val resp = TransferCompleteResponse("ok")
            Log.d(tag, "[$_id] no more file to received, confirm end of transfer")
            val jsonData = Json.encodeToString(TransferCompleteResponse.serializer(), resp)
            encryptAndSend(jsonData.toByteArray())
            _state = State.FINISHED
            _delegate?.onDualEnded(this)
        }
    }
}