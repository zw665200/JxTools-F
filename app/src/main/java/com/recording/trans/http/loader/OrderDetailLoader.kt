package com.recording.trans.http.loader

import com.recording.trans.bean.OrderDetail
import com.recording.trans.controller.Constant
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import io.reactivex.Observable

object OrderDetailLoader {

    fun getOrderStatus(orderSn: String): Observable<Response<OrderDetail>> {
        return RetrofitServiceManager.getInstance().orderDetail.getOrderDetail(orderSn, Constant.CLIENT_TOKEN)
    }
}