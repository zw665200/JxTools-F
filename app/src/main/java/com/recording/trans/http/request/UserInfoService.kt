package com.recording.trans.http.request

import com.recording.trans.bean.UserInfo
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.*

interface UserInfoService {

    @POST("visit")
    @FormUrlEncoded
    fun getUser(@Field("questToken") token: String): Observable<Response<List<UserInfo>>>
}