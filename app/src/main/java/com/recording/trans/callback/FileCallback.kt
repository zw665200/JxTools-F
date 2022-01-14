package com.recording.trans.callback

interface FileCallback {
    fun onSuccess(filePath: String)
    fun onFailed(message: String)
}