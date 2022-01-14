package com.recording.trans.bean

data class GetToken(
    var questTime: String,
    var questToken: String,
    var questFrom: String,
    var device: String,
    var deviceId: String,
    var brand: String,
    var mode: String,
    var productId: String
)
