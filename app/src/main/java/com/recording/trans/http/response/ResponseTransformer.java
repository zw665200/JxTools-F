package com.recording.trans.http.response;


import com.recording.trans.http.exception.ApiException;
import com.recording.trans.http.exception.CustomException;
import com.recording.trans.utils.JLog;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

public class ResponseTransformer {

    public static <T> ObservableTransformer<Response<T>, T> handleResult() {
        return upstream -> upstream
                .onErrorResumeNext(new ErrorResumeFunction<>())
                .flatMap(new ResponseFunction<>());
    }


    /**
     * 非服务器产生的异常，比如本地无无网络请求，Json数据解析错误等等。
     *
     * @param <T>
     */
    private static class ErrorResumeFunction<T> implements Function<Throwable, ObservableSource<? extends Response<T>>> {

        @Override
        public ObservableSource<? extends Response<T>> apply(@NotNull Throwable throwable) throws Exception {
            return Observable.error(CustomException.handleException(throwable));
        }
    }

    /**
     * 服务其返回的数据解析
     * 正常服务器返回数据和服务器可能返回的exception
     *
     * @param <T>
     */
    private static class ResponseFunction<T> implements Function<Response<T>, ObservableSource<T>> {

        @Override
        public ObservableSource<T> apply(Response<T> tResponse) throws Exception {
            int code = tResponse.getRetCode();
            String message = tResponse.getRetMsg();
            if (code == 2000) {
                T data = tResponse.getRetData();
                return Observable.just(Objects.requireNonNull(data));
            } else {
                JLog.i("error");
                return Observable.error(new ApiException(code, message));
            }
        }
    }
}
