package com.picfix.tools.http.request

import com.recording.trans.bean.UserInfo
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface LoginService {

    @POST("thirdLogin")
    @FormUrlEncoded
    fun getUser(
        @Field("questToken") questToken: String,
        @Field("accessCode") accessCode: String
    ): Observable<Response<List<UserInfo>>>
}