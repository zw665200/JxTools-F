package com.recording.trans.callback

import com.recording.trans.bean.FileWithType

interface VideoCallback {
    fun onSuccess()
    fun onProgress(file: FileWithType)
    fun onFailed(message: String)
}