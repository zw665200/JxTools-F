package com.recording.trans.callback

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/11/21 14:50
 */
interface TaskCallback {
    fun onSuccess(msg: String)
    fun onFailed(msg: String)
}