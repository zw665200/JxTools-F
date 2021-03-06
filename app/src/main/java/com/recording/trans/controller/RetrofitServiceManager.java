package com.recording.trans.controller;

import com.picfix.tools.http.request.LoginService;
import com.picfix.tools.http.request.ReportService;
import com.recording.trans.http.ApiConfig;
import com.recording.trans.http.request.AccountDeleteService;
import com.recording.trans.http.request.AliPayService;
import com.recording.trans.http.request.CheckPayService;
import com.recording.trans.http.request.ComplaintService;
import com.recording.trans.http.request.ConfigService;
import com.recording.trans.http.request.FastPayService;
import com.recording.trans.http.request.OrderCancelService;
import com.recording.trans.http.request.OrderDetailService;
import com.recording.trans.http.request.OrderService;
import com.recording.trans.http.request.OssService;
import com.recording.trans.http.request.PayStatusService;
import com.recording.trans.http.request.ServiceListService;
import com.recording.trans.http.request.SinglePayStatusService;
import com.recording.trans.http.request.TokenService;
import com.recording.trans.http.request.UseTimesReportService;
import com.recording.trans.http.request.UserReportService;
import com.recording.trans.http.request.WechatPayService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitServiceManager {

    private static final int DEFAULT_TIME_OUT = 10;
    private static final int DEFAULT_READ_TIME_OUT = 10;
    private Retrofit mRetrofit;
    private static RetrofitServiceManager mInstance;
    private static volatile LoginService userInfo = null;
    private static volatile TokenService token = null;
    private static volatile OrderService orderService = null;
    private static volatile ConfigService configService = null;
    private static volatile AliPayService aliPayService = null;
    private static volatile FastPayService fastPayService = null;
    private static volatile ServiceListService priceService = null;
    private static volatile OrderDetailService orderDetailService = null;
    private static volatile OrderCancelService orderCancelService = null;
    private static volatile PayStatusService payStatusService = null;
    private static volatile SinglePayStatusService singlePayStatusService = null;
    private static volatile OssService ossService = null;
    private static volatile ComplaintService complaintService = null;
    private static volatile WechatPayService wechatPayService = null;
    private static volatile CheckPayService checkPayService = null;
    private static volatile ReportService reportService = null;
    private static volatile UserReportService userReportService = null;
    private static volatile AccountDeleteService accountDeleteService = null;
    private static volatile UseTimesReportService useTimesReportService = null;

    public static RetrofitServiceManager getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitServiceManager.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitServiceManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * ?????????retrofit
     */
    public void initRetrofitService() {
        // ?????? OKHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);//??????????????????
        builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//????????? ????????????
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//?????????????????????
//        builder.addInterceptor(new BaseUrlInterceptor());

        //????????????????????????
        if (Constant.isDebug) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(httpLoggingInterceptor);
        }

        // ???????????????????????????
//        HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor.Builder()
//                .addHeaderParams("paltform", "android")
//                .addHeaderParams("userToken", "1234343434dfdfd3434")
//                .addHeaderParams("userId", "123445")
//                .build();
//        builder.addInterceptor(commonInterceptor);

        // ??????Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ApiConfig.BASE_URL_1)
                .build();
    }

    public LoginService getUserInfo() {
        if (userInfo == null) {
            synchronized (LoginService.class) {
                userInfo = mRetrofit.create(LoginService.class);
            }
        }
        return userInfo;
    }

    public TokenService getToken() {
        if (token == null) {
            synchronized (TokenService.class) {
                token = mRetrofit.create(TokenService.class);
            }
        }
        return token;
    }

    public ConfigService getConfig() {
        if (configService == null) {
            synchronized (ConfigService.class) {
                configService = mRetrofit.create(ConfigService.class);
            }
        }
        return configService;
    }

    public OrderService getOrders() {
        if (orderService == null) {
            synchronized (OrderService.class) {
                orderService = mRetrofit.create(OrderService.class);
            }
        }
        return orderService;
    }

    public AliPayService getAliPayParam() {
        if (aliPayService == null) {
            synchronized (AliPayService.class) {
                aliPayService = mRetrofit.create(AliPayService.class);
            }
        }
        return aliPayService;
    }

    public FastPayService getFastPayParam() {
        if (fastPayService == null) {
            synchronized (FastPayService.class) {
                fastPayService = mRetrofit.create(FastPayService.class);
            }
        }
        return fastPayService;
    }

    public ServiceListService getPrice() {
        if (priceService == null) {
            synchronized (ServiceListService.class) {
                priceService = mRetrofit.create(ServiceListService.class);
            }
        }
        return priceService;
    }

    public OrderDetailService getOrderDetail() {
        if (orderDetailService == null) {
            synchronized (OrderDetailService.class) {
                orderDetailService = mRetrofit.create(OrderDetailService.class);
            }
        }
        return orderDetailService;
    }

    public OrderCancelService orderCancel() {
        if (orderDetailService == null) {
            synchronized (OrderCancelService.class) {
                orderCancelService = mRetrofit.create(OrderCancelService.class);
            }
        }
        return orderCancelService;
    }

    public PayStatusService getPayStatus() {
        if (payStatusService == null) {
            synchronized (PayStatusService.class) {
                payStatusService = mRetrofit.create(PayStatusService.class);
            }
        }
        return payStatusService;
    }

    public WechatPayService getWechatPayStatus() {
        if (wechatPayService == null) {
            synchronized (PayStatusService.class) {
                wechatPayService = mRetrofit.create(WechatPayService.class);
            }
        }
        return wechatPayService;
    }

    public SinglePayStatusService getSinglePayStatus() {
        if (singlePayStatusService == null) {
            synchronized (SinglePayStatusService.class) {
                singlePayStatusService = mRetrofit.create(SinglePayStatusService.class);
            }
        }
        return singlePayStatusService;
    }

    public CheckPayService checkPayService() {
        if (checkPayService == null) {
            synchronized (CheckPayService.class) {
                checkPayService = mRetrofit.create(CheckPayService.class);
            }
        }
        return checkPayService;
    }

    public OssService getOssToken() {
        if (ossService == null) {
            synchronized (OssService.class) {
                ossService = mRetrofit.create(OssService.class);
            }
        }
        return ossService;
    }

    public ComplaintService reportComplaint() {
        if (complaintService == null) {
            synchronized (ComplaintService.class) {
                complaintService = mRetrofit.create(ComplaintService.class);
            }
        }
        return complaintService;
    }

    public ReportService report() {
        if (reportService == null) {
            synchronized (ReportService.class) {
                reportService = mRetrofit.create(ReportService.class);
            }
        }
        return reportService;
    }

    public UserReportService userReport() {
        if (userReportService == null) {
            synchronized (UserReportService.class) {
                userReportService = mRetrofit.create(UserReportService.class);
            }
        }
        return userReportService;
    }


    public AccountDeleteService accountDelete() {
        if (accountDeleteService == null) {
            synchronized (AccountDeleteService.class) {
                accountDeleteService = mRetrofit.create(AccountDeleteService.class);
            }
        }
        return accountDeleteService;
    }

    public UseTimesReportService useTimesReport() {
        if (useTimesReportService == null) {
            synchronized (UseTimesReportService.class) {
                useTimesReportService = mRetrofit.create(UseTimesReportService.class);
            }
        }
        return useTimesReportService;
    }


    /**
     * ???????????????Service
     *
     * @param service Service ??? class
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

}
