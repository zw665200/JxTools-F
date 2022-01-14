package com.recording.trans.http.loader

import com.recording.trans.bean.CheckPayParam
import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object CheckPayLoader {

    fun checkPay(): Observable<Response<CheckPayParam>> {
        return RetrofitServiceManager.getInstance().checkPayService().checkPay(Constant.PRODUCT_ID, Constant.CLIENT_TOKEN)
    }
}