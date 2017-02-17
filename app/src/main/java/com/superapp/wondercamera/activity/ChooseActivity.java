package com.superapp.wondercamera.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.banner.RevMobBanner;
import com.superapp.wondercamera.R;
import com.superapp.wondercamera.model.ImageResponseModel;
import com.superapp.wondercamera.service.RestService;
import com.superapp.wondercamera.util.DataUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseActivity extends AppCompatActivity implements View.OnClickListener{

    private ProgressDialog progressDialog;
    private CropImageView cropImageView;
    private Button btnSelect;
    private Button btnUnSelect;
    private RevMob revmob;
    private RevMobBanner banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        String filePath = getIntent().getStringExtra("filePath");
                File imgFile = new File(filePath);
        startRevMobSession();
        btnSelect = (Button) findViewById(R.id.btn_select);
        btnUnSelect = (Button) findViewById(R.id.btn_un_select);
        btnUnSelect.setOnClickListener(this);
        btnSelect.setOnClickListener(this);
        if (imgFile.exists()) {
            cropImageView = (CropImageView) findViewById(R.id.cropImageView);
            cropImageView.setCropMode(CropImageView.CropMode.RATIO_3_4);
            cropImageView.setCompressFormat(Bitmap.CompressFormat.JPEG);
            cropImageView.setCompressQuality(100);
            cropImageView.startLoad(Uri.fromFile(imgFile), new LoadCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                }
            });
        }else{
            onBackPressed();
        }

    }

    private   Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                               boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width,
                height, filter);
    }

    private File compress(File file) throws IOException {
        File targetFile = new File(ChooseActivity.this.getCacheDir(),"ScaleImage");
        targetFile.createNewFile();
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        myBitmap = scaleDown(myBitmap,500,false);
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        // save image into gallery
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 20, ostream);
        FileOutputStream fout = new FileOutputStream(targetFile);
        fout.write(ostream.toByteArray());
        fout.close();
        return targetFile;

    }

    private void sendImage(File imgFile){
        btnSelect.setVisibility(View.INVISIBLE);
        btnUnSelect.setVisibility(View.INVISIBLE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(DataUtils.getINSTANCE(this).getLanguage().getAnalyze());
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setCancelable(false);
        try {
            imgFile = compress(imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imgFile.getName(), requestFile);
        RequestBody language =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), DataUtils.getINSTANCE(this).getLanguage().getKey());
        RestService service = new RestService();
        Call<ImageResponseModel> call = service.getImageService().sentImage(language, body);
        final File finalImgFile = imgFile;
        call.enqueue(new Callback<ImageResponseModel>() {
            @Override
            public void onResponse(Call<ImageResponseModel> call, Response<ImageResponseModel> response) {

                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getStatus() == 200) {
                        Intent intent = new Intent(ChooseActivity.this, ResultActivity.class);
                        intent.putExtra("filePath", finalImgFile.getAbsolutePath());
                        intent.putExtra("result", response.body().getMessage());
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    } else {
                        Toast.makeText(ChooseActivity.this, response.body().getError().getMessage(), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }else{
                    Toast.makeText(ChooseActivity.this, DataUtils.getINSTANCE(ChooseActivity.this)
                            .getLanguage().getServerError()+": "+ response.code()+": " +response.message(), Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }

            @Override
            public void onFailure(Call<ImageResponseModel> call, Throwable t) {
                Toast.makeText(ChooseActivity.this, DataUtils.getINSTANCE(ChooseActivity.this).getLanguage()
                        .getConnectionFail(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                ChooseActivity.this.onBackPressed();
            }
        });
    }

    public Uri createSaveUri() {
        return Uri.fromFile(new File(this.getCacheDir(), "cropped"));
    }

    private final CropCallback mCropCallback = new CropCallback() {
        @Override
        public void onSuccess(Bitmap cropped) {
        }

        @Override
        public void onError() {
        }
    };

    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override
        public void onSuccess(Uri outputUri) {
            progressDialog.dismiss();
            sendImage(new File(outputUri.getPath()));
        }

        @Override
        public void onError() {

        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_select:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Croping...");
                progressDialog.show();
                cropImageView.startCrop(createSaveUri(), mCropCallback, mSaveCallback);
                break;
            case R.id.btn_un_select:
                onBackPressed();
                break;
        }
    }

    public void startRevMobSession() {
        //RevMob's Start Session method:
        revmob = RevMob.startWithListener(this, new RevMobAdsListener() {
            @Override
            public void onRevMobSessionStarted() {
                loadBanner(); // Cache the banner once the session is started
                Log.i("RevMob", "Session Started");
            }

            @Override
            public void onRevMobSessionNotStarted(String message) {
                //If the session Fails to start, no ads can be displayed.
                Log.i("RevMob", "Session Failed to Start");
            }
        }, "58989ff659acc9620beeea7b");
        if (revmob != null) {
            loadBanner();
        }
        Log.i("Revmob", "ahihi");
    }

    public void loadBanner() {
        banner = revmob.preLoadBanner(this, new RevMobAdsListener() {
            @Override
            public void onRevMobAdReceived() {
                showBanner();
                Log.i("RevMob", "Banner Ready to be Displayed"); //At this point, the banner is ready to be displayed.
            }

            @Override
            public void onRevMobAdNotReceived(String message) {
                Log.i("RevMob", "Banner Not Failed to Load");
            }

            @Override
            public void onRevMobAdDisplayed() {
                Log.i("RevMob", "Banner Displayed");
            }
        });
    }

    public void showBanner() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banner.getParent() == null) {
                    ViewGroup view = (ViewGroup) findViewById(R.id.bannerLayout);
                    view.addView(banner);
                }
                banner.show(); //This method must be called in order to display the ad.

            }
        });
    }
}
