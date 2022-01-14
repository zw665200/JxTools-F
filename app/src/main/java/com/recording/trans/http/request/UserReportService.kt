package com.recording.trans.http.request

import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserReportService {

    @POST("addUserLog")
    @FormUrlEncoded
    fun report(
        @Field("clientToken") token: String,
        @Field("path") path: String,
        @Field("content") content: String,
        @Field("logType") logType: String,
        @Field("durations") durations: Int
    ): Observable<Response<String?>>
}