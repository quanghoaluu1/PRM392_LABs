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

/**
 * Khai báo các endpoint API cho chức năng Lab 8.
 * Lưu ý: Tên trường form phải khớp với backend PHP.
 */
public interface ApiService {

    @FormUrlEncoded
    @POST("login_signup_api.php")
    /** Đăng nhập với tuỳ chọn ghi nhớ phiên. */
    Call<LoginResponse> login(
            @Field("action") String action,
            @Field("acc_kh") String username,
            @Field("pass_kh") String password,
            @Field("remember") boolean remember
    );

    @FormUrlEncoded
    @POST("login_signup_api.php")
    /** Đăng ký tài khoản mới. */
    Call<ApiResponse> signup(
        @Field("action") String action,
        @Field("acc_kh") String username,
        @Field("pass_kh") String password,
        @Field("repass_kh") String repassword
    );

    @FormUrlEncoded
    @POST("login_signup_api.php")
    /** Đặt lại mật khẩu theo tài khoản. */
    Call<ApiResponse> forgotPassword(
        @Field("action") String action,
        @Field("acc_kh") String username,
        @Field("pass_kh") String password,
        @Field("repass_kh") String repassword
    );

    @FormUrlEncoded
    @POST("login_signup_api.php")
    /** Kiểm tra phiên đăng nhập hiện tại. */
    Call<LoginResponse> checkSession(@Field("action") String action);

    @FormUrlEncoded
    @POST("login_signup_api.php")
    /** Đăng xuất khỏi phiên hiện tại. */
    Call<ApiResponse> logout(@Field("action") String action);

    @Multipart
    @POST("upload_avatar_api.php")
    /** Tải lên ảnh đại diện dạng multipart. */
    Call<LoginResponse> uploadAvatar(
            @Part("username") RequestBody username,
            @Part MultipartBody.Part avatar
    );
}
