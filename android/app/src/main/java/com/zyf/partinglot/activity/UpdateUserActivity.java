package com.zyf.partinglot.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.zyf.partinglot.R;
import com.zyf.partinglot.message.MyWebSocketService;
import com.zyf.partinglot.utils.EmptyJSON;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

/**
 * 用户信息更新界面
 * 实现头像更换功能，支持拍照和从相册选择图片
 */
public class UpdateUserActivity extends AppCompatActivity implements MyWebSocketService.MyCallback {
    private static final Logger log = LoggerFactory.getLogger(UpdateUserActivity.class);
    private final String TAG = "UpdateUserActivity";
    // 界面组件
    private ImageView avatarImageView;
    // 拍照时的图片URI
    private Uri photoUri;
    // 权限请求码
    private static final int PERMISSION_REQUEST_CODE = 100;
    private MyWebSocketService myService;
    private boolean isBound = false;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyWebSocketService.LocalBinder binder = (MyWebSocketService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            myService.registerCallback(UpdateUserActivity.this); // 注册回调
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
    /**
     * 拍照结果处理器
     * 当拍照完成后，使用photoUri加载图片
     */
    private final ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadImage(photoUri);
                }
            }
    );

    /**
     * 相册选择结果处理器
     * 当从相册选择图片后，获取选中图片的URI并加载
     */
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    loadImage(selectedImageUri);
                }
            }
    );
    private String base64Image;
    private EditText et_name;
    private EditText et_licensePlate;
    private Button bt_submit;
    private int userId;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        Intent intent = new Intent(this, MyWebSocketService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        // 初始化头像ImageView并设置点击监听
        avatarImageView = findViewById(R.id.avatarImageView);
        avatarImageView.setOnClickListener(v -> checkPermissionsAndShowDialog());
        et_name = findViewById(R.id.et_name);
        et_licensePlate = findViewById(R.id.et_licensePlate);
        bt_submit = findViewById(R.id.bt_submit);
        progress = findViewById(R.id.progress);
        findViewById(R.id.ibt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackHomeWithResult(0);
            }
        });
        bt_submit.setOnClickListener(v -> {
            String name = et_name.getText().toString();
            String licensePlate = et_licensePlate.getText().toString();
            try {
                bt_submit.setEnabled(false);
                JSONObject command = EmptyJSON.getForCommand();
                JSONObject data = new JSONObject();
                data.put("describe","UpdateUserInformation");
                data.put("userName",name);
                data.put("licensePlate",licensePlate);
                data.put("image",base64Image);
                data.put("userId",userId);
                command.put("data",data);
                myService.sendStringToServer(command.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }
    //0表示未修改，1表示成功，-1表示异常失败
    private void BackHomeWithResult(int resultId){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultId", resultId);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId",0);
        et_name.setText(intent.getStringExtra("userName"));
        et_licensePlate.setText(intent.getStringExtra("licensePlate"));
        String image64 = intent.getStringExtra("image");
        base64Image = image64;
        byte[] image = Base64.getDecoder().decode(image64);
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        avatarImageView.setImageBitmap(bitmap);
    }

    /**
     * 检查必要权限并显示选择对话框
     * 需要相机和存储权限
     */
    private void checkPermissionsAndShowDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            
            // 请求所需权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            showImagePickerDialog();
        }
    }

    /**
     * 权限请求结果处理
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults 授权结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                Toast.makeText(this, "需要相机和存储权限才能更换头像", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 显示图片选择对话框
     * 提供拍照和从相册选择两个选项
     */
    private void showImagePickerDialog() {
        String[] options = {"拍照", "从相册选择"};
        new AlertDialog.Builder(this)
                .setTitle("选择头像")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        takePhoto();
                    } else {
                        pickFromGallery();
                        Log.d(TAG, "showImagePickerDialog: 2");
                    }
                })
                .show();
    }

    /**
     * 启动相机拍照
     * 使用FileProvider处理文件URI
     */
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // 添加详细日志
        Log.d(TAG, "takePhoto: 准备启动相机");
        
        // 检查是否有相机应用
        PackageManager packageManager = getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Log.d(TAG, "takePhoto: 设备支持相机");
            
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d(TAG, "takePhoto: 创建图片文件成功: " + photoFile.getAbsolutePath());
                
                photoUri = FileProvider.getUriForFile(this,
                        "com.zyf.partinglot.fileprovider",
                        photoFile);
                Log.d(TAG, "takePhoto: FileProvider URI: " + photoUri);
                
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                
                // 添加必要的 Flag
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                
                takePhotoLauncher.launch(takePictureIntent);
                Log.d(TAG, "takePhoto: 启动相机成功");
                
            } catch (IOException ex) {
                Log.e(TAG, "takePhoto: 创建图片文件失败", ex);
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "takePhoto: 启动相机失败", e);
                Toast.makeText(this, "启动相机失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "takePhoto: 设备不支持相机");
            Toast.makeText(this, "您的设备不支持相机功能", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 启动相册选择图片
     */
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    /**
     * 创建图片文件
     * @return 创建的临时文件
     * @throws IOException 文件创建失败时抛出
     */
    private File createImageFile() throws IOException {
        // 创建唯一的文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Images");
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * 加载并处理图片
     * 将图片调整为64x64大小，并保存为新文件
     * @param imageUri 图片的URI
     */
    private void loadImage(Uri imageUri) {
        if (imageUri != null) {
            try {
                // 加载并压缩图片为64x64像素
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 64, 64, true);
                
                // 保存压缩后的图片到应用私有目录
                File resizedFile = new File(getExternalFilesDir("Images"), "avatar_64x64.jpg");
                FileOutputStream fos = new FileOutputStream(resizedFile);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                
                // 直接使用Bitmap更新ImageView
                avatarImageView.setImageBitmap(resizedBitmap);
                
                // 转换为base64
                base64Image = convertBitmapToBase64(resizedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "图片处理失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 将Bitmap转换为Base64字符串
     * @param bitmap 要转换的位图
     * @return base64编码的字符串
     */
    private String convertBitmapToBase64(Bitmap bitmap) {
        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myService.unregisterCallback(this);
        unbindService(connection);
    }

    @Override
    public void onDataReceived(String message) {
        JSONObject data;
        try {
            JSONObject root = new JSONObject(message);
            data = root.getJSONObject("data");
            int resultId;
            if(data.getString("describe").equals("UpdateUserInformation")){
                if(data.getString("reply").equals("ok")){
                    showToast("修改成功");
                    resultId = 1;
                }else{
                    showToast("出现异常，请通知管理员");
                    resultId = -1;
                }
                BackHomeWithResult(resultId);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

}