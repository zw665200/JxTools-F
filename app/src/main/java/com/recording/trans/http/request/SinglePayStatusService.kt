package com.recording.trans.http.request

import com.recording.trans.bean.PayStatus
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SinglePayStatusService {

    @POST("servicePayStatus")
    @FormUrlEncoded
    fun getPayStatus(
        @Field("serverId") serverId: Int,
        @Field("clientToken") token: String
    ): Observable<Response<List<PayStatus>>>
}