package com.example.se183138.api;
import com.example.se183138.activity.lab8.model.LoginResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @FormUrlEncoded
    @POST("login_signup_api.php")
    Call<LoginResponse> login(
            @Field("action") String action,
            @Field("acc_kh") String username,
            @Field("pass_kh") String password,
            @Field("remember") boolean remember
    );

    @FormUrlEncoded
    @POST("login_signup_api.php")
    Call<ApiResponse> signup(
        @Field("action") String action,
        @Field("acc_kh") String username,
        @Field("pass_kh") String password,
        @Field("repass_kh") String repassword
    );

    @FormUrlEncoded
    @POST("login_signup_api.php")
    Call<ApiResponse> forgotPassword(
        @Field("action") String action,
        @Field("acc_kh") String username,
        @Field("pass_kh") String password,
        @Field("repass_kh") String repassword
    );

    @FormUrlEncoded
    @POST("login_signup_api.php")
    Call<LoginResponse> checkSession(@Field("action") String action);

    @FormUrlEncoded
    @POST("login_signup_api.php")
    Call<ApiResponse> logout(@Field("action") String action);

    @Multipart
    @POST("upload_avatar_api.php")
    Call<LoginResponse> uploadAvatar(
            @Part("username") RequestBody username,
            @Part MultipartBody.Part avatar
    );
}
