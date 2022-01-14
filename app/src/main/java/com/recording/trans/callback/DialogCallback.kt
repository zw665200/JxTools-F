package com.recording.trans.callback

import com.recording.trans.bean.FileBean

interface DialogCallback {
    fun onSuccess(file: FileBean)
    fun onCancel()
}