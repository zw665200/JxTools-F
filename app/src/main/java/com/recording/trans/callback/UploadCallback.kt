package com.recording.trans.callback

interface UploadCallback {
    fun onSuccess(path: String)
    fun onFailed(msg: String)
}