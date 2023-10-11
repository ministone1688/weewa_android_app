package com.weewa.lib

import android.content.Context
import android.util.Log
import com.weewa.lib.Prefs.serverPort
import com.weewa.lib.Prefs.serverPort2
import java.net.InetSocketAddress
import java.net.Socket

interface IWeewaDelegate {
    fun sessionWaiting(id: Int)
    fun sessionConnecting(id: Int)
    fun sessionStarted(id: Int)
    fun sessionProgress(id: Int, progress: Double)
    fun sessionEnded(id: Int)
    fun sessionError(id: Int, errMsg: String)
}

internal data class SendRequest(
    val dstIp: String,
    val dstName: String,
    val files: List<String>,
    val encrypt: Boolean,
    val sessionId: Int
)

internal data class ReceiveRequest(
    val srcIp: String,
    val srcName: String,
    val files: List<String>,
    val encrypt: Boolean,
    val sessionId: Int
)

enum class WeewaMode(val v: Int) {
    MASTER(0),
    SLAVE1(1),
    SLAVE2(2);

    fun shouldUseFirstSet(): Boolean {
        return this == MASTER || this == SLAVE1
    }

    fun shouldUseSecondSet(): Boolean {
        return this == MASTER || this == SLAVE2
    }
}

class WeewaLib private constructor(): IDiscoveryServiceDelegate, IFileServerDelegate, IFileSessionDelegate {
    companion object {
        @Volatile
        private var instance: WeewaLib? = null

        // mode
        var mode: WeewaMode = WeewaMode.SLAVE1

        fun shared(): WeewaLib {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = WeewaLib()
                    }
                }
            }
            return instance!!
        }
    }

    init {
        System.loadLibrary("sodium")
        System.loadLibrary("weewacrypt")
    }

    private var _ds: DiscoveryService? = null
    private var _fs: FileServer? = null
    private val tag = javaClass.simpleName
    private var _appCtx: Context? = null
    private var _delegate: IWeewaDelegate? = null
    private var _sendReqs = mutableListOf<SendRequest>()
    private var _recvReqs = mutableListOf<ReceiveRequest>()
    private var _sessionMap = mutableMapOf<Int, FileSession>()
    private var _remoteDir = ""

    fun launch(ctx: Context) {
        // random port if not set
        _appCtx = ctx.applicationContext
        val prefs = Prefs.defaultPreference(ctx)
        if(prefs.serverPort <= 0) {
            prefs.serverPort = (52768..65500).random()
        }
        if(prefs.serverPort2 <= 0) {
            prefs.serverPort2 = prefs.serverPort + 1
        }

        // start discovery
        _ds = DiscoveryService(ctx)
        _ds?.setDelegate(this)
        _ds?.start()

        // start file server
        _fs = FileServer(ctx);
        _fs?.setDelegate(this)
        _fs?.start()
    }

    fun setDelegate(delegate: IWeewaDelegate?) {
        _delegate = delegate
    }

    fun setRemoteDirectory(remoteDir: String) {
        _remoteDir = remoteDir
    }

    override fun onNewHost(deviceName: String, ip: String, port: Int, httpPort: Int) {
        sendIfCan()
        receiveIfCan()
    }

    fun stop(sessionId: Int) {
        if(_sessionMap.containsKey(sessionId)) {
            _sessionMap[sessionId]?.abort()
            _sessionMap.remove(sessionId)
        } else {
            _sendReqs.removeIf { it.sessionId == sessionId }
            _recvReqs.removeIf { it.sessionId == sessionId }
        }
    }

    fun send(dstIp: String, dstName: String, files: List<String>, encrypt: Boolean = false): Int {
        val id = FileSession.nextId++
        _sendReqs.add(SendRequest(
            dstIp,
            dstName,
            files,
            encrypt,
            id
        ))
        sendIfCan()
        return id
    }

    fun receive(srcIp: String, srcName: String, files: List<String>, encrypt: Boolean = false): Int {
        val id = FileSession.nextId++
        _recvReqs.add(
            ReceiveRequest(
                srcIp,
                srcName,
                files,
                encrypt,
                id
            )
        )
        receiveIfCan()
        return id
    }

    private fun sendIfCan() {
        // if no request return
        if(_sendReqs.isEmpty())
            return

        // iterate requests
        var pending = mutableListOf<SendRequest>()
        _sendReqs.forEach {
            // check is host is online
            var meta = if(it.dstIp.isNotEmpty()) {
                _ds?.getMeta(it.dstIp)
            } else {
                _ds?.getMetaByName(it.dstName)
            }

            // if found, start transfer
            if (meta != null && meta.name.isNotEmpty()) {
                try {
                    // decide port used
                    val mode = WeewaMode.values().first { it.v == meta.mode }
                    val port = if(mode.shouldUseFirstSet()) meta.port else meta.port2
                    Log.d(tag, "connecting to destination ${meta.ip}:${port}...")

                    // connecting event
                    _delegate?.sessionConnecting(it.sessionId)

                    // connect
                    val socket = Socket()
                    socket.connect(InetSocketAddress(meta.ip, port), 5000)
                    if(socket.isConnected) {
                        Log.d(tag, "destination connected, starting transfer session")

                        // start sender
                        val session = FileSender(socket, it.files, _remoteDir, it.encrypt)
                        session.setId(it.sessionId)
                        _sessionMap[it.sessionId] = session
                        session.setDelegate(this)
                        session.start()
                        _delegate?.sessionStarted(it.sessionId)
                    } else {
                        Log.d(tag, "source can not be connected, abort")
                        _delegate?.sessionError(it.sessionId, "source can not be connected")
                    }
                } catch (e: Exception) {
                    Log.d(tag, "source can not be connected, abort: ${e.localizedMessage}")
                    _delegate?.sessionError(it.sessionId, e.localizedMessage)
                }
            } else {
                Log.d(tag, "destination ${it.dstIp} is not online, wait...")
                pending.add(it)
                _delegate?.sessionWaiting(it.sessionId)
            }
        }
        _sendReqs.clear()
        _sendReqs.addAll(pending)
    }

    private fun receiveIfCan() {
        // if no request, return
        if(_recvReqs.isEmpty())
            return

        // iterate requests
        var pending = mutableListOf<ReceiveRequest>()
        _recvReqs.forEach {
            // check is host is online
            var meta = if(it.srcIp.isNotEmpty()) {
                _ds?.getMeta(it.srcIp)
            } else {
                _ds?.getMetaByName(it.srcName)
            }

            // if found, request source send file
            if(meta != null && meta.name.isNotEmpty()) {
                Thread(Runnable {
                    try {
                        // decide port used
                        val mode = WeewaMode.values().first { it.v == meta.mode }
                        val port = if(mode.shouldUseFirstSet()) meta.port else meta.port2
                        Log.d(tag, "connecting to source ${meta.ip}:${port}...")

                        // connecting event
                        _delegate?.sessionConnecting(it.sessionId)

                        // connect
                        val socket = Socket()
                        socket.connect(InetSocketAddress(meta.ip, port), 5000)
                        if(socket.isConnected) {
                            Log.d(tag, "source connected, starting transfer session")

                            // start sender
                            val session = FileDual(socket, it.files, _remoteDir, it.encrypt)
                            session.setId(it.sessionId)
                            _sessionMap[it.sessionId] = session
                            session.setDelegate(this)
                            session.start()
                            _delegate?.sessionStarted(it.sessionId)
                        } else {
                            Log.d(tag, "source can not be connected, abort")
                            _delegate?.sessionError(it.sessionId, "source can not be connected")
                        }
                    } catch (e: Exception) {
                        Log.d(tag, "source can not be connected, abort: ${e.localizedMessage}")
                        _delegate?.sessionError(it.sessionId, e.localizedMessage)
                    }
                }).start()
            } else {
                Log.d(tag, "source ${it.srcIp} is not online, wait...")
                pending.add(it)
                _delegate?.sessionWaiting(it.sessionId)
            }
        }
        _recvReqs.clear()
        _recvReqs.addAll(pending)
    }

    override fun onNewReceiver(socket: Socket) {
        // log
        val peerIp = socket.inetAddress.hostAddress ?: return
        Log.d(tag, "new transfer request from $peerIp:${socket.port}")

        // start receiver
        val session = FileDual(socket)
        session.setId(FileSession.nextId++)
        _sessionMap[session.getId()] = session
        session.setDelegate(this)
        session.start()
        _delegate?.sessionStarted(session.getId())
    }

    override fun onErrorOccurred(session: FileSession, errMsg: String) {
        if(_sessionMap.containsKey(session.getId())) {
            _sessionMap.remove(session.getId())
            Log.d(
                tag,
                "[${session.getId()}] ${if (session.isSender()) "sender" else "receiver"} session error: $errMsg"
            )
            _delegate?.sessionError(session.getId(), errMsg)
        }
    }

    override fun onSenderEnded(session: FileSession) {
        if(_sessionMap.containsKey(session.getId())) {
            _sessionMap.remove(session.getId())
            Log.d(tag, "[${session.getId()}] sender session ended")
            _delegate?.sessionEnded(session.getId())
        }
    }

    override fun onDualEnded(session: FileSession) {
        if(_sessionMap.containsKey(session.getId())) {
            _sessionMap.remove(session.getId())
            Log.d(
                tag,
                "[${session.getId()}] ${if (session.isSender()) "sender" else "receiver"} session end"
            )
            _delegate?.sessionEnded(session.getId())
        }
    }

    override fun onUpdateProgress(session: FileSession, progress: Double) {
//        Log.d(tag, "[${session.getId()}] session progress: $progress")
        _delegate?.sessionProgress(session.getId(), progress)
    }

    fun getFilesDir(): String {
        return _appCtx!!.filesDir.absolutePath
    }

    fun getContext(): Context {
        return _appCtx!!
    }
}