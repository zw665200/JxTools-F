package com.recording.trans.http.loader

import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object UseTimesReportLoader {

    fun report(durations: Int): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().useTimesReport().report(Constant.CLIENT_TOKEN, durations)
    }
}