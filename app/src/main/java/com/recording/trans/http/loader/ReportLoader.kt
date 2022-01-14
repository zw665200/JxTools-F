package com.recording.trans.http.loader

import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object ReportLoader {

    fun report(): Observable<Response<String?>> {
        return RetrofitServiceManager.getInstance().report().report(Constant.CLIENT_TOKEN)
    }
}