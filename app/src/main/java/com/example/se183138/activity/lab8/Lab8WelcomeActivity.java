package com.example.se183138.activity.lab8;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.se183138.MainActivity;
import com.example.se183138.R;
import com.example.se183138.activity.lab8.model.LoginResponse;
import com.example.se183138.api.ApiResponse;
import com.example.se183138.api.ApiService;
import com.example.se183138.api.RetrofitClient;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Lab8WelcomeActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int REQUEST_PERMISSION_STORAGE = 101;
    private static final int REQUEST_GALLERY = 300;
    private SharedPreferences prefs;
    private ApiService apiService;
    private Button btnLogout;

    private Button btnUpload;
    private Uri imageUri;
    private Uri selectedImageUri;
    private ImageView avatarImageView;
    String username;
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null){
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    if (photo != null){
                        avatarImageView.setImageBitmap(photo);
                    }else{
                        Toast.makeText(this, "Không thể lấy ảnh từ camera", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    // Đăng ký launcher để chọn ảnh từ thư viện
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    Log.d("WelcomeActivity", "Selected image URI: " + imageUri);

                    if (imageUri != null) {
                        try {
                            // Copy ảnh từ Google Photos về cache
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            File tempFile = new File(getCacheDir(), "temp_image.jpg");
                            FileOutputStream outputStream = new FileOutputStream(tempFile);

                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, len);
                            }

                            inputStream.close();
                            outputStream.close();

                            // Lấy URI thực tế từ file cục bộ
                            Uri localUri = Uri.fromFile(tempFile);

                            // Cập nhật ImageView
                            avatarImageView.setImageURI(localUri);

                            // Lưu lại để upload sau
                            selectedImageUri = localUri;
                            Log.d("WelcomeActivity", "Local cached image URI: " + localUri);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Không thể lấy ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Không thể lấy ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab8_welcome);

        TextView textWelcomeMessage = findViewById(R.id.text_welcome_message);
        username = getIntent().getStringExtra("username");
        textWelcomeMessage.setText("Xin chào " + username + "!");
        avatarImageView = findViewById(R.id.avatar_image);
        ImageView insertImgView = findViewById(R.id.insert_img);

        btnLogout = findViewById(R.id.btnLogout);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        btnUpload = findViewById(R.id.btnUpload);
        String avatar = getIntent().getStringExtra("avatar");

        if (avatar != null && !avatar.isEmpty()) {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(avatarImageView);
        }

        btnLogout.setOnClickListener(v -> {
            apiService.logout("logout").enqueue(new Callback<ApiResponse>(){

                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        prefs.edit().clear().apply();
                        new SharedPrefsCookiePersistor(getApplicationContext()).clear();
                        Toast.makeText(Lab8WelcomeActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Lab8WelcomeActivity.this, Lab8LoginActivity.class));
                        finish();
                    }else{
                        Log.d("WelcomeActivity", "Logout failed: " + response.message());
                        Toast.makeText(Lab8WelcomeActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(Lab8WelcomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        // Khi nhấn vào insert_img, mở lựa chọn chụp ảnh hoặc chọn ảnh từ thư viện
        insertImgView.setOnClickListener(v -> showImageOptions());
        btnUpload.setOnClickListener(v -> uploadImage());
    }

    // Hiển thị lựa chọn chụp ảnh hoặc chọn ảnh từ thư viện
    private void showImageOptions() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn hình ảnh");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Kiểm tra và yêu cầu quyền camera
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "Ứng dụng cần quyền truy cập camera để chụp ảnh.", Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                } else {
                    openCamera();
                }
            } else if (which == 1) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                                != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                            },
                            REQUEST_PERMISSION_STORAGE);
                } else {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    // Mở camera để chụp ảnh
    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        }
    }

    // Mở thư viện ảnh
    private void openGallery() {
//        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        galleryLauncher.launch(pickPhotoIntent);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_STORAGE);
        }else{
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Chọn ảnh trước đã!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Copy ảnh về file tạm trong cache
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            File tempFile = new File(getCacheDir(), "upload_avatar.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Tạo MultipartBody
            RequestBody requestFile = RequestBody.Companion.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", tempFile.getName(), requestFile);
            RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);

            // Gọi API
            apiService.uploadAvatar(usernameBody, body).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(Lab8WelcomeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Lab8WelcomeActivity.this, "Upload thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(Lab8WelcomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi đọc file ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    // Xử lý yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Quyền camera bị từ chối", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            avatarImageView.setImageURI(imageUri);
        }
    }

}
