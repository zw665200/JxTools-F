package com.recording.trans.http.request

import com.recording.trans.bean.CheckPayParam
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SelfieService {

    @POST("cspayStatus")
    @FormUrlEncoded
    fun checkPay(
        @Field("clientToken") clientToken: String,
    ): Observable<Response<CheckPayParam>>
}