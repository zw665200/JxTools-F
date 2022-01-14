package com.recording.trans.controller

import com.alibaba.fastjson.JSON
import com.recording.trans.callback.TaskCallback
import com.recording.trans.controller.AsrManager
import com.recording.trans.utils.DraftWithOrigin
import com.recording.trans.utils.EncryptUtil
import com.recording.trans.utils.JLog
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ServerHandshake
import java.io.*
import java.net.URI
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.system.exitProcess

/**
 * 实时转写调用demo
 * 此demo只是一个简单的调用示例，不适合用到实际生产环境中
 *
 * @author white
 */
class AsrManager private constructor() {

    companion object {
        // appid
        private const val APPID = "93b4e152"

        // appid对应的secret_key
        private const val SECRET_KEY = "935236cae40550e42c7c0835f2acec88"

        // 请求地址
        private const val HOST = "rtasr.xfyun.cn/v1/ws"
        private const val BASE_URL = "wss://$HOST"
        private const val ORIGIN = "https://$HOST"

        // 每次发送的数据大小 1280 字节
        private const val CHUNCKED_SIZE = 1280
        private val sdf = SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
        private var instance: AsrManager? = null

        fun get(): AsrManager {
            if (instance == null) {
                synchronized(AsrManager::class) {
                    if (instance == null) {
                        instance = AsrManager()
                    }
                }
            }

            return instance!!
        }
    }

    @Throws(Exception::class)
    fun doTask(path: String, callback: TaskCallback) {
        while (true) {
            val url = URI(BASE_URL + getHandShakeParams(APPID, SECRET_KEY))
            val draft = DraftWithOrigin(ORIGIN)
            val handshakeSuccess = CountDownLatch(1)
            val connectClose = CountDownLatch(1)
            val client = MyWebSocketClient(url, draft, handshakeSuccess, connectClose, callback)
            client.connect()
            while (client.readyState != WebSocket.READYSTATE.OPEN) {
                JLog.i("$currentTimeStr\t连接中")
                Thread.sleep(1000)
            }

            // 等待握手成功
            handshakeSuccess.await()
            JLog.i(sdf.format(Date()) + " 开始发送音频数据")
            // 发送音频
            var bytes = ByteArray(CHUNCKED_SIZE)
            try {
                RandomAccessFile(path, "r").use { raf ->
                    var len = -1
                    var lastTs: Long = 0
                    while (raf.read(bytes).also { len = it } != -1) {
                        if (len < CHUNCKED_SIZE) {
                            send(client, bytes.copyOfRange(0, len).also { bytes = it })
                            break
                        }
                        val curTs = System.currentTimeMillis()
                        if (lastTs == 0L) {
                            lastTs = System.currentTimeMillis()
                        } else {
                            val s = curTs - lastTs
                            if (s < 40) {
                                JLog.i("error time interval: $s ms")
                            }
                        }
                        send(client, bytes)
                        // 每隔40毫秒发送一次数据
                        Thread.sleep(40)
                    }

                    // 发送结束标识
                    send(client, "{\"end\": true}".toByteArray())
                    JLog.i("$currentTimeStr\t发送结束标识完成")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 等待连接关闭
            connectClose.await()
            break
        }
    }

    @Throws(Exception::class)
    fun doTask(data: ByteArray, callback: TaskCallback) {
        while (true) {
            val url = URI(BASE_URL + getHandShakeParams(APPID, SECRET_KEY))
            val draft = DraftWithOrigin(ORIGIN)
            val handshakeSuccess = CountDownLatch(1)
            val connectClose = CountDownLatch(1)
            val client = MyWebSocketClient(url, draft, handshakeSuccess, connectClose, callback)
            client.connect()
            while (client.readyState != WebSocket.READYSTATE.OPEN) {
                JLog.i("$currentTimeStr\t连接中")
                Thread.sleep(1000)
            }

            // 等待握手成功
            handshakeSuccess.await()
            JLog.i(sdf.format(Date()) + " 开始发送音频数据")
            // 发送音频
            var bytes = ByteArray(CHUNCKED_SIZE)
            try {
                var len = -1
                var lastTs: Long = 0
                val inputStream = ByteArrayInputStream(data)
                while (inputStream.read(bytes).also { len = it } != -1) {
                    if (len < CHUNCKED_SIZE) {
                        send(client, bytes.copyOfRange(0, len).also { bytes = it })
                        break
                    }
                    val curTs = System.currentTimeMillis()
                    if (lastTs == 0L) {
                        lastTs = System.currentTimeMillis()
                    } else {
                        val s = curTs - lastTs
                        if (s < 40) {
                            JLog.i("error time interval: $s ms")
                        }
                    }
                    send(client, bytes)
                    // 每隔40毫秒发送一次数据
                    Thread.sleep(40)
                }

                // 发送结束标识
                send(client, "{\"end\": true}".toByteArray())
                JLog.i("$currentTimeStr\t发送结束标识完成")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 等待连接关闭
            connectClose.await()
            break
        }
    }

    fun disconnection() {

    }

    // 生成握手参数
    private fun getHandShakeParams(appId: String, secretKey: String?): String {
        val ts = "${System.currentTimeMillis() / 1000}"
        val signa: String
        try {
            signa = EncryptUtil.HmacSHA1Encrypt(EncryptUtil.MD5(appId + ts), secretKey)
            return "?appid=" + appId + "&ts=" + ts + "&signa=" + URLEncoder.encode(signa, "UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun send(client: WebSocketClient, bytes: ByteArray?) {
        if (client.isClosed) {
            throw RuntimeException("client connect closed!")
        }
        client.send(bytes)
    }

    val currentTimeStr: String
        get() = sdf.format(Date())

    // 把转写结果解析为句子
    fun getContent(message: String): String {
        val resultBuilder = StringBuffer()
        try {
            val messageObj = JSON.parseObject(message)
            val cn = messageObj.getJSONObject("cn")
            val st = cn.getJSONObject("st")
            val rtArr = st.getJSONArray("rt")
            for (i in rtArr.indices) {
                val rtArrObj = rtArr.getJSONObject(i)
                val wsArr = rtArrObj.getJSONArray("ws")
                for (j in wsArr.indices) {
                    val wsArrObj = wsArr.getJSONObject(j)
                    val cwArr = wsArrObj.getJSONArray("cw")
                    for (k in cwArr.indices) {
                        val cwArrObj = cwArr.getJSONObject(k)
                        val wStr = cwArrObj.getString("w")
                        resultBuilder.append(wStr)
                    }
                }
            }
        } catch (e: Exception) {
            return message
        }
        return resultBuilder.toString()
    }

    inner class MyWebSocketClient(
        serverUri: URI,
        protocolDraft: Draft?,
        private val handshakeSuccess: CountDownLatch,
        private val connectClose: CountDownLatch,
        private val callback: TaskCallback
    ) : WebSocketClient(serverUri, protocolDraft) {

        init {
            if (serverUri.toString().contains("wss")) {
                trustAllHosts(this)
            }
        }

        override fun onOpen(handshake: ServerHandshake) {
            JLog.i("$currentTimeStr\t连接建立成功！")
        }

        override fun onMessage(msg: String) {
            val msgObj = JSON.parseObject(msg)
            val action = msgObj.getString("action")
            if ("started" == action) {
                // 握手成功
                JLog.i(currentTimeStr + "\t握手成功！sid: " + msgObj.getString("sid"))
                handshakeSuccess.countDown()
            } else if ("result" == action) {
                // 转写结果
                JLog.i(currentTimeStr + "\tresult: " + getContent(msgObj.getString("data")))
                callback.onSuccess(getContent(msgObj.getString("data")))
            } else if ("error" == action) {
                // 连接发生错误
                JLog.i("Error: $msg")
                exitProcess(0)
            }
        }

        override fun onError(e: Exception) {
            JLog.i(currentTimeStr + "\t连接发生错误：" + e.message + ", " + Date())
            e.printStackTrace()
            exitProcess(0)
        }

        override fun onClose(arg0: Int, arg1: String, arg2: Boolean) {
            JLog.i("$currentTimeStr\t连接关闭")
            connectClose.countDown()
        }

        override fun onMessage(bytes: ByteBuffer) {
            try {
                JLog.i(currentTimeStr + "\t服务端返回：" + String(bytes.array(), Charsets.UTF_8))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }

        private fun trustAllHosts(appClient: MyWebSocketClient) {
            JLog.i("wss")
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(arg0: Array<X509Certificate>, arg1: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(arg0: Array<X509Certificate>, arg1: String) {
                }
            })

            try {
                val sc = SSLContext.getInstance("TLS")
                sc.init(null, trustAllCerts, SecureRandom())
                appClient.socket = sc.socketFactory.createSocket()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}