package com.recording.trans.bean

data class CheckPayParam(
    var status: String,
    var msg: String,
    var orders: Order?
)
