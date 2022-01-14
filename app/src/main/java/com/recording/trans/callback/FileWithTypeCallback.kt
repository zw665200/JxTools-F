package com.recording.trans.callback

import com.recording.trans.bean.FileStatus
import com.recording.trans.bean.FileWithType

interface FileWithTypeCallback {
    fun onSuccess(step: Enum<FileStatus>)
    fun onProgress(step: Enum<FileStatus>, file: FileWithType)
    fun onFailed(step: Enum<FileStatus>, message: String)
}