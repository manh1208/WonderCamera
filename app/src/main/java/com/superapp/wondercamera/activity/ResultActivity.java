package com.superapp.wondercamera.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.banner.RevMobBanner;
import com.superapp.wondercamera.R;
import com.superapp.wondercamera.model.ResponseModel;
import com.superapp.wondercamera.service.RestService;
import com.superapp.wondercamera.util.DataUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {
    private String filePath;
    private String result;
    private TextView txtResult;
    private Button btnSave;
    private Button btnShare;
    private Button btnBack;
    private ImageView ivImage;
    private Bitmap resultBitmap;
    private File imageFile;
    private RevMob revmob;
    private RevMobBanner banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        findView();
        filePath = getIntent().getStringExtra("filePath");
        result = getIntent().getStringExtra("result");
        File imgFile = new File(filePath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ivImage.setImageBitmap(myBitmap);
            txtResult.setText(result);
            btnSave.setOnClickListener(this);
            btnShare.setOnClickListener(this);
            btnBack.setOnClickListener(this);

        }
        startRevMobSession();



    }

    private void findView() {
        txtResult = (TextView) findViewById(R.id.txt_result);
        ivImage = (ImageView) findViewById(R.id.iv_image_result);
        btnBack = (Button) findViewById(R.id.btn_back_camera);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnShare = (Button) findViewById(R.id.btn_share_facebook);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                resultBitmap = takeScreenshot();
save();
                Toast.makeText(this, DataUtils.getINSTANCE(this).getLanguage().getSave(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_share_facebook:
                btnBack.setVisibility(View.INVISIBLE);
                btnShare.setVisibility(View.INVISIBLE);
                btnSave.setVisibility(View.INVISIBLE);
                Bitmap watermark = BitmapFactory.decodeResource(this.getResources(),R.drawable.icon);
                resultBitmap = takeScreenshot();
                Bitmap bitmap = resultBitmap;
                putOverlay(bitmap,watermark);
                FacebookSdk.sdkInitialize(getApplicationContext());
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                ShareDialog shareDialog = new ShareDialog(ResultActivity.this);
                shareDialog.show(ResultActivity.this, content);
                btnBack.setVisibility(View.VISIBLE);
                btnShare.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_back_camera:
                onBackPressed();
                break;
        }
    }

    public Bitmap takeScreenshot() {
        View rootView = findViewById(R.id.layout_save);
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return bitmap;
    }
    public void putOverlay(Bitmap bitmap, Bitmap overlay) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(overlay, bitmap.getWidth()-overlay.getWidth()-30,30, paint);
    }

    private void save(){
        try {
        String state = Environment.getExternalStorageState();
        File folder = null;
        if (state.contains(Environment.MEDIA_MOUNTED)) {
            folder = new File(Environment
                    .getExternalStorageDirectory() + "/WonderCamera");
        } else {
            folder = new File(Environment
                    .getExternalStorageDirectory() + "/WonderCamera");
        }

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            java.util.Date date = new java.util.Date();
            imageFile = new File(folder.getAbsolutePath()
                    + File.separator
                    + "ImageSaved"
                    + System.currentTimeMillis() % 100000
                    +".jpg");

            imageFile.createNewFile();
            Log.d("camera", imageFile.getAbsolutePath());
        } else {
            Toast.makeText(getBaseContext(), "Image Not saved",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

        // save image into gallery
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);


        FileOutputStream fout = null;

            fout = new FileOutputStream(imageFile);

        fout.write(ostream.toByteArray());
        fout.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATE_TAKEN,
                    System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA,
                    imageFile.getAbsolutePath());
            ResultActivity.this.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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