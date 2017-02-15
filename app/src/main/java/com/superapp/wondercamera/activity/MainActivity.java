package com.superapp.wondercamera.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.superapp.wondercamera.R;
import com.superapp.wondercamera.model.Language;
import com.superapp.wondercamera.util.DataUtils;
import com.superapp.wondercamera.util.RequestPermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener,RequestPermissionListener {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1000;
    private static final int IMAGE_PICK = 1001;
    private Button btnTakePhoto;
    private Button btnChoosePhoto;
    private Button btnSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(this);
        btnTakePhoto.setText(DataUtils.getINSTANCE(this).getLanguage().getTakePhoto());
        btnChoosePhoto = (Button) findViewById(R.id.btn_choose_photo);
        btnChoosePhoto.setOnClickListener(this);
        btnChoosePhoto.setText(DataUtils.getINSTANCE(this).getLanguage().getChoosePhoto());
        btnSetting = (Button) findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(this);
        requestCamera();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_take_photo:
                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_setting:
                 intent = new Intent(MainActivity.this,LanguageActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_choose_photo:
                 intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_PICK);

        }
    }

    @Override
    public void requestCamera() {
        int permsRequestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(perms, permsRequestCode);
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(perms, permsRequestCode);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                String[] perms = {Manifest.permission.CAMERA};
                requestPermissions(perms, permsRequestCode);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            File f = DataUtils.getImageFileFromUri(this, data.getData(), DataUtils.getPicturePath( System.currentTimeMillis()%100000
                    + "Image.jpg"), 1000, Bitmap.CompressFormat.JPEG, 50);

            Intent intent = new Intent(MainActivity.this,ChooseActivity.class);
            intent.putExtra("filePath",f.getAbsolutePath());
            startActivity(intent);
//            finish();
//            isOpenedCamera = false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        btnTakePhoto.setText(DataUtils.getINSTANCE(this).getLanguage().getTakePhoto());
        btnChoosePhoto.setText(DataUtils.getINSTANCE(this).getLanguage().getChoosePhoto());
    }
}
