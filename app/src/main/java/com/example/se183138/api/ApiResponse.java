package com.example.se183138.api;

/**
 * Mô hình hoá phản hồi chung từ API (trạng thái, thông điệp, avatar).
 */
public class ApiResponse {
    private String status;
    private String message;
    private String avatar_url;

    /** Khởi tạo đối tượng phản hồi. */
    public ApiResponse(String message, String status, String avatar_url) {
        this.message = message;
        this.status = status;
        this.avatar_url = avatar_url;
    }

    /** Trạng thái phản hồi: success/failed/... */
    public String getStatus() {
        return status;
    }

    /** Thông điệp mô tả chi tiết từ server. */
    public String getMessage() {
        return message;
    }

    /** URL ảnh đại diện (nếu có). */
    public String getAvatar_url(){
        return avatar_url;
    }
}

