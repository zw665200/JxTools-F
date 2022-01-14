package com.recording.trans.http.request

import com.recording.trans.bean.GetToken
import com.recording.trans.bean.Token
import com.recording.trans.http.response.Response
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {

    @POST("getQuestToken")
    fun getToken(@Body getToken: GetToken): Observable<Response<Token>>
}