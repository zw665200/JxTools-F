package com.recording.trans.http.loader

import com.recording.trans.bean.UserInfo
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object UserInfoLoader : ObjectLoader() {

    fun getUser(token: String, code: String): Observable<Response<List<UserInfo>>> {
        return RetrofitServiceManager.getInstance().userInfo.getUser(token,code)
    }
}