package com.recording.trans.callback

interface PayCallback {
    fun success()
    fun progress(orderId: String)
    fun failed(msg: String)
}