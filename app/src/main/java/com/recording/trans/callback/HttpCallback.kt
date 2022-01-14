package com.recording.trans.callback

interface HttpCallback {
    fun onSuccess()
    fun onFailed(msg: String)
}