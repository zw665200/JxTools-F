package com.recording.trans.callback

import com.recording.trans.bean.FileStatus
import com.recording.trans.bean.FileWithType

interface PicCallback {
    fun onSuccess(step: Enum<FileStatus>)
    fun onProgress(step: Enum<FileStatus>, index: Int)
    fun onProgress(step: Enum<FileStatus>, file: FileWithType)
    fun onFailed(step: Enum<FileStatus>, message: String)
}