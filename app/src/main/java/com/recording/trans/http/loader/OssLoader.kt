package com.recording.trans.http.loader

import com.recording.trans.bean.OssParam
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object OssLoader : ObjectLoader() {

    fun getOssToken(token: String): Observable<Response<OssParam>> {
        return RetrofitServiceManager.getInstance().ossToken.getOssToken(token)
    }
}