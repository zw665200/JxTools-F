package com.recording.trans.http.request

import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UseTimesReportService {

    @POST("useTimesReport")
    @FormUrlEncoded
    fun report(
        @Field("clientToken") token: String,
        @Field("durations") durations: Int
    ): Observable<Response<String?>>
}