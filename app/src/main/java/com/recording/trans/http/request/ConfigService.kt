package com.recording.trans.http.request

import com.recording.trans.bean.Config
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface ConfigService {

    @GET("siteInfo")
    fun getConfig(@Query("serverCode") serviceCode: String): Observable<Response<Config>>
}