package com.recording.trans.http.response

data class Response<T>(
    var retCode: Int = 0,
    var retMsg: String? = null,
    var retData: T? = null
)