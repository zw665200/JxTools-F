package com.recording.trans.http.loader

import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object AccountLoader {

    fun delete(): Observable<Response<String>> {
        return RetrofitServiceManager.getInstance().accountDelete().delete(Constant.CLIENT_TOKEN, Constant.PRODUCT_ID)
    }
}