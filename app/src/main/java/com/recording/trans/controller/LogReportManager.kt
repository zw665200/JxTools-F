package com.recording.trans.controller

import com.recording.trans.http.loader.UseTimesReportLoader
import com.recording.trans.http.loader.UserReportLoader
import com.recording.trans.http.response.ResponseTransformer
import com.recording.trans.http.schedulers.SchedulerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/8/18 10:35
 */
object LogReportManager : CoroutineScope by MainScope() {

    fun logReport(name: String, content: String, type: LogType) {
        if (Constant.CLIENT_TOKEN == "") return

        val t = when (type) {
            LogType.LOGIN -> "login"
            LogType.ORDER -> "order"
            LogType.OPERATION -> "operation"
        }

        launch(Dispatchers.IO) {
            UserReportLoader.report(name, content, t, 0)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({

                }, {})
        }
    }

    fun logReport(name: String, content: String, type: LogType, duration: Int) {
        if (Constant.CLIENT_TOKEN == "") return

        val t = when (type) {
            LogType.LOGIN -> "login"
            LogType.ORDER -> "order"
            LogType.OPERATION -> "operation"
        }

        launch(Dispatchers.IO) {
            UserReportLoader.report(name, content, t, duration)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({

                }, {})
        }
    }

    fun useTimesReport(time: Int) {
        if (Constant.CLIENT_TOKEN == "") return

        launch(Dispatchers.IO) {
            UseTimesReportLoader.report(time)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({

                }, {})
        }
    }

    enum class LogType {
        LOGIN, OPERATION, ORDER
    }
}