package com.recording.trans.http.loader

import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object UserReportLoader {

    fun report(path: String, content: String, logType: String, duration: Int): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().userReport().report(Constant.CLIENT_TOKEN, path, content, logType, duration)
    }
}