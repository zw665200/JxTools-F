package com.recording.trans.callback

interface FileDialogCallback {
    fun onSuccess(str: String)
    fun onCancel()
}