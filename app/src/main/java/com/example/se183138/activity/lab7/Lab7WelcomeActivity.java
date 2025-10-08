package com.example.se183138.activity.lab7;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.se183138.R;

public class Lab7WelcomeActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int REQUEST_PERMISSION_STORAGE = 101;

    private ImageView avatarImageView;

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
                    if (imageUri != null) {
                        avatarImageView.setImageURI(imageUri);
                    } else {
                        Toast.makeText(this, "Không thể lấy ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab7_welcome);

        TextView textWelcomeMessage = findViewById(R.id.text_welcome_message);
        textWelcomeMessage.setText("Welcome to FPT Mobile!");

        avatarImageView = findViewById(R.id.avatar_image);
        ImageView insertImgView = findViewById(R.id.insert_img);

        // Khi nhấn vào insert_img, mở lựa chọn chụp ảnh hoặc chọn ảnh từ thư viện
        insertImgView.setOnClickListener(v -> showImageOptions());
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
                // Kiểm tra và yêu cầu quyền đọc bộ nhớ
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, "Ứng dụng cần quyền truy cập bộ nhớ để chọn ảnh.", Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
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
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhotoIntent);
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

}
