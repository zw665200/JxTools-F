package com.recording.trans.http.request

import com.recording.trans.bean.FastPayParam
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FastPayService {

    @POST("fourthPay")
    @FormUrlEncoded
    fun getOrderParam(
        @Field("serviceId") serviceId: Int,
        @Field("clientToken") clientToken: String,
        @Field("productId") productId: String,
        @Field("channelCode") channelCode: String
    ): Observable<Response<FastPayParam>>
}
