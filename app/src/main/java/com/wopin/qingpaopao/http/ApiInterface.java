package com.wopin.qingpaopao.http;


import com.wopin.qingpaopao.bean.request.LoginReq;
import com.wopin.qingpaopao.bean.request.ThirdReq;
import com.wopin.qingpaopao.bean.response.LoginRsp;
import com.wopin.qingpaopao.bean.response.NormalRsp;
import com.wopin.qingpaopao.bean.response.ThirdBindRsp;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2017/8/17.
 */

public interface ApiInterface {

    @Streaming
    @GET
        //下载文件
    Observable<ResponseBody> downloadFile(@Url String fileUrl);


    @POST("users/login")
    Observable<LoginRsp> login(@Body LoginReq loginReq);

    @POST("users/sendVerifyCode")
    Observable<NormalRsp> sendVerifyCode(@Body LoginReq loginReq);

    @POST("users/register")
    Observable<NormalRsp> register(@Body LoginReq loginReq);

    @POST("users/changePassword")
    Observable<NormalRsp> changePassword(@Body LoginReq loginReq);

    @POST("users/changePhone")
    Observable<NormalRsp> changePhone(@Body LoginReq loginReq);

    @POST("users/thirdRegister")
    Observable<NormalRsp> thirdRegister(@Body ThirdReq loginReq);

    @POST("users/thirdLogin")
    Observable<LoginRsp> thirdLogin(@Body ThirdReq loginReq);

    @POST("users/getThirdBinding")
    Observable<ThirdBindRsp> getThirdBinding();

    @POST("users/thirdBinding")
    Observable<NormalRsp> thirdBinding(@Body ThirdReq loginReq);

    @POST("users/changeIcon")
    Observable<NormalRsp> changeIcon(@Body ThirdReq loginReq);

    @POST("users/changeUserName")
    Observable<NormalRsp> changeUsername(@Body ThirdReq loginReq);

}
