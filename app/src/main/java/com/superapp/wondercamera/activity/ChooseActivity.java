package com.superapp.wondercamera.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.superapp.wondercamera.R;
import com.superapp.wondercamera.model.ResponseModel;
import com.superapp.wondercamera.service.RestService;
import com.superapp.wondercamera.util.DataUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseActivity extends AppCompatActivity {

    private String filePath;
    private ImageView ivImage;
    private ProgressDialog progressDialog;
    private RequestBody requestFile;
    private MultipartBody.Part body;
    private RestService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        filePath = getIntent().getStringExtra("filePath");
        ivImage = (ImageView) findViewById(R.id.iv_image_choose);
        File imgFile = new File(filePath);
        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ivImage.setImageBitmap(myBitmap);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(DataUtils.getINSTANCE(this).getLanguage().getAnalyze());
            progressDialog.show();
            sendImage(imgFile);
        }else{
            onBackPressed();
        }

    }
    private void sendImage(final File imgFile){
        requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);
        body =
                MultipartBody.Part.createFormData("image", imgFile.getName(), requestFile);
        RequestBody language =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), DataUtils.getINSTANCE(this).getLanguage().getKey());
        service = new RestService();
//        progressDialog.setMessage("Sending image to server...");
        Call<ResponseModel> call = service.getImageService().sentImage(language,body);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                progressDialog.dismiss();
                if (response.body().getStatus()==200) {
                    Intent intent = new Intent(ChooseActivity.this,ResultActivity.class);
                    intent.putExtra("filePath",imgFile.getAbsolutePath());
                    intent.putExtra("result",response.body().getMessage());
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.right_in,R.anim.left_out);
                }else{
//                    openCamera(cameraId);
                    Toast.makeText(ChooseActivity.this, response.body().getError().getMessage(), Toast.LENGTH_SHORT).show();
//                    txtResult.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
//                openCamera(cameraId);
                progressDialog.dismiss();
                ChooseActivity.this.onBackPressed();
            }
        });
    }

}
