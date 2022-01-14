package com.recording.trans.http.loader

import com.recording.trans.bean.Config
import com.recording.trans.controller.RetrofitServiceManager
import com.recording.trans.http.response.Response
import com.recording.trans.utils.AppUtil
import io.reactivex.Observable

object ConfigLoader {

    fun getConfig(): Observable<Response<Config>> {
        return RetrofitServiceManager.getInstance().config.getConfig(AppUtil.getChannelId())
    }
}