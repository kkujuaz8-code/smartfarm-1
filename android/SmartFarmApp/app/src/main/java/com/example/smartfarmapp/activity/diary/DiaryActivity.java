package com.example.smartfarmapp.activity.diary;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.smartfarmapp.network.ApiClient;
import com.example.smartfarmapp.service.ApiService;
import com.example.smartfarmapp.R;
import com.example.smartfarmapp.model.DiaryEntry;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private TextView tvDate;
    private ImageView ivPhoto;
    private Button btnSave, btnAddPhoto, btnDelete;
    private ImageButton btnBack;

    private ApiService apiService;
    private String currentUserId;
    private String selectedDate;
    private Uri currentImageUri;
    private Uri cameraImageUri;
    private String serverImageUrl = "";

    private Long folderId = null;
    private Long editId = null;
    private boolean isEditMode = false;

    private static final int REQ_CAMERA_PERMISSION = 200;

    // 갤러리 & 카메라 런처
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    startCrop(result.getData().getData());
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    startCrop(cameraImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = sharedPref.getString("userId", "testUser");

        // 뷰 연결
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tvDate = findViewById(R.id.tvDate);
        ivPhoto = findViewById(R.id.ivPhoto);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        // 서버 연결
        apiService = ApiClient.getClient().create(ApiService.class);

        // 카메라 권한 체크/요청 (식물도감과 동일한 방식)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
        }

        // 날짜 기본값 설정
        Calendar calendar = Calendar.getInstance();
        updateDateDisplay(calendar);

        // 수정 모드 체크
        Intent intent = getIntent();
        long receivedFolderId = intent.getLongExtra("folderId", -1);
        if (receivedFolderId != -1) folderId = receivedFolderId;

        if (intent.hasExtra("id")) {
            isEditMode = true;
            editId = intent.getLongExtra("id", -1);
            etTitle.setText(intent.getStringExtra("title"));
            etContent.setText(intent.getStringExtra("content"));

            String oldUrl = intent.getStringExtra("imageUrl");
            if (oldUrl != null && !oldUrl.isEmpty()) {
                serverImageUrl = oldUrl;
                Glide.with(this).load(serverImageUrl).centerCrop().into(ivPhoto);
            }
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // 버튼 리스너
        tvDate.setOnClickListener(v -> showDatePicker());
        btnAddPhoto.setOnClickListener(v -> showImageSourceDialog());
        ivPhoto.setOnClickListener(v -> showImageSourceDialog());
        btnSave.setOnClickListener(v -> processSave());
        btnDelete.setOnClickListener(v -> deleteDiary());
        btnBack.setOnClickListener(v -> finish());
    }

    private void showImageSourceDialog() {
        String[] options = {"카메라로 찍기", "앨범에서 선택"};
        new AlertDialog.Builder(this)
                .setTitle("사진 가져오기")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        // 권한 다시 확인 후 없으면 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "SmartFarm_" + System.currentTimeMillis());
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        cameraLauncher.launch(intent);
    }

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "crop_" + System.currentTimeMillis() + ".jpg"));

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(4, 3)
                .withMaxResultSize(1080, 1080)
                .start(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "카메라 권한이 허용되었습니다!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            currentImageUri = UCrop.getOutput(data);
            Glide.with(this).load(currentImageUri).into(ivPhoto);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "사진 편집 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void processSave() {
        if (currentImageUri != null) {
            uploadImageToFirebase(currentImageUri);
        } else {
            saveDiaryToDB();
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        Toast.makeText(this, "사진 업로드 중...", Toast.LENGTH_SHORT).show();

        StorageReference imgRef = FirebaseStorage.getInstance().getReference()
                .child("images/" + System.currentTimeMillis() + ".jpg");

        imgRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            serverImageUrl = downloadUri.toString();
                            saveDiaryToDB();
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "사진 업로드 실패 (글만 저장)", Toast.LENGTH_SHORT).show();
                    serverImageUrl = "";
                    saveDiaryToDB();
                });
    }

    private void saveDiaryToDB() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (folderId == null) folderId = 1L;

        DiaryEntry entry = new DiaryEntry();
        entry.setUserId(currentUserId);
        entry.setTitle(title);
        entry.setContent(content);
        entry.setDate(selectedDate);
        entry.setImageUrl(serverImageUrl);
        entry.setFolderId(folderId);

        if (isEditMode && editId != null) {
            entry.setId(editId);
        }

        apiService.saveDiary(entry).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DiaryActivity.this, "저장되었습니다!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DiaryActivity.this, "저장 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(DiaryActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteDiary() {
        if (editId == null) return;

        new AlertDialog.Builder(this)
                .setTitle("삭제 확인")
                .setMessage("정말 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    if (!serverImageUrl.isEmpty()) {
                        deleteImageFromFirebase(serverImageUrl);
                    } else {
                        requestDeleteDiaryToDB();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteImageFromFirebase(String imageUrl) {
        try {
            String path = imageUrl.substring(imageUrl.indexOf("images/"));
            FirebaseStorage.getInstance().getReference().child(path).delete()
                    .addOnCompleteListener(task -> requestDeleteDiaryToDB());
        } catch (Exception e) {
            requestDeleteDiaryToDB();
        }
    }

    private void requestDeleteDiaryToDB() {
        apiService.deleteDiary(editId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DiaryActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DiaryActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(DiaryActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDateDisplay(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(calendar.getTime());
        tvDate.setText(selectedDate);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateDateDisplay(calendar);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}