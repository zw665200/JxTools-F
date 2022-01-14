package com.recording.trans.http.loader

import com.recording.trans.bean.Order
import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object OrderLoader {

    fun getOrders(): Observable<Response<List<Order>>> {
        return RetrofitServiceManager.getInstance().orders.getOrders(Constant.PRODUCT_ID, Constant.CLIENT_TOKEN)
    }
}