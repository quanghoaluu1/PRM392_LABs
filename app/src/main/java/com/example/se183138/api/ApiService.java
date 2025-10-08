package com.example.se183138.api;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("login_signup_api.php")
    Call<ApiResponse> login(
            @Field("action") String action,
            @Field("acc_kh") String username,
            @Field("pass_kh") String password
    );

    @FormUrlEncoded
    @POST("login_signup_api.php")
    Call<ApiResponse> signup(
        @Field("action") String action,
        @Field("acc_kh") String username,
        @Field("pass_kh") String password,
        @Field("repass_kh") String repassword
    );
}
