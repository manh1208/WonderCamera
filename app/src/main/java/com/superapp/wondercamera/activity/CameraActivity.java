package com.superapp.wondercamera.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.banner.RevMobBanner;
import com.superapp.wondercamera.R;
import com.superapp.wondercamera.custom.CameraViewCustom;
import com.superapp.wondercamera.model.ImageResponseModel;
import com.superapp.wondercamera.service.RestService;
import com.superapp.wondercamera.util.DataUtils;
import com.superapp.wondercamera.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.superapp.wondercamera.util.Util.getOptimalPreviewSize;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private CameraViewCustom surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Button captureImage;
    private int cameraId;
    private ProgressDialog progressDialog;
    private RevMob revmob;
    private RevMobBanner banner;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        captureImage = (Button) findViewById(R.id.btn_captureImage);
        surfaceView = (CameraViewCustom) findViewById(R.id.surfaceView);
        startRevMobSession();
        Button changeCamera = (Button) findViewById(R.id.btn_change_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        captureImage.setOnClickListener(this);
        changeCamera.setOnClickListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Camera.getNumberOfCameras() > 1) {
            changeCamera.setVisibility(View.VISIBLE);
        } else {
            changeCamera.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try {


            if (Camera.getNumberOfCameras() > 1) {
                openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        }catch (Exception e){
            openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_captureImage:
                takeImage();
                break;
            case R.id.btn_change_camera:
                flipCamera();
                break;
        }

    }

    private void flipCamera() {
        int id = (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? Camera.CameraInfo.CAMERA_FACING_FRONT
                : Camera.CameraInfo.CAMERA_FACING_BACK);
        openCamera(id);
    }

    private boolean openCamera(int id) {
        boolean result = false;
        cameraId = id;
        releaseCamera();
        try {
            camera = Camera.open(cameraId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (camera != null) {
            try {
                List<Camera.Size> mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
                setUpCamera(camera);
                camera.setErrorCallback(new Camera.ErrorCallback() {

                    @Override
                    public void onError(int error, Camera camera) {
//to show the error message.
                    }
                });
                camera.setPreviewDisplay(surfaceHolder);
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, surfaceView.getWidth(), surfaceView.getHeight());
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                camera.setParameters(parameters);
                camera.startPreview();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
                releaseCamera();
            }
        }
        return result;
    }

    private void releaseCamera() {
        try {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.setErrorCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("error", e.toString());
            camera = null;
        }
    }

    private void setUpCamera(Camera c) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
            default:
                break;
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // frontFacing
            rotation = (info.orientation + degree) % 330;
            rotation = (360 - rotation) % 360;
            Log.e("Rotation", rotation + "");
//            rotation = (info.orientation - degree + 360) % 360;
        } else {
            // Back-facing
            rotation = (info.orientation - degree + 360) % 360;
        }
        c.setDisplayOrientation(rotation);
        Camera.Parameters params = c.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        List<String> focusModes = params.getSupportedFlashModes();
        if (focusModes != null) {
            if (focusModes
                    .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFlashMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }

        params.setRotation(rotation);
    }




    private void takeImage() {

        captureImage.setEnabled(false);
        camera.takePicture(null, null, new Camera.PictureCallback() {



            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    // convert byte array into bitmap
                    Bitmap loadedImage = BitmapFactory.decodeByteArray(data, 0,
                            data.length);
                    // rotate Image
                    Matrix rotateMatrix = new Matrix();
                    if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        rotateMatrix.postRotate(-90);
                    } else {
                        rotateMatrix.postRotate(90);
                    }
                    Bitmap rotatedBitmap = Bitmap.createBitmap(loadedImage, 0,
                            0, loadedImage.getWidth(), loadedImage.getHeight(),
                            rotateMatrix, false);
                    imageFile = Util.saveImageFileFromBitmap(rotatedBitmap,
                            Util.getPhotoFile(CameraActivity.this.getCacheDir().getPath(),"Image",null),-1, Bitmap.CompressFormat.JPEG,100);

                    Log.e("Camera ACtivity","Fil real size: "+imageFile.length()/1024);
                    sendImage(Util.compressImageFileFromFile(imageFile,
                            Util.getPhotoFile(CameraActivity.this.getCacheDir().getPath(),"CatchImage",null),
                            500, Bitmap.CompressFormat.JPEG,20));
                    captureImage.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendImage(final File imgFile) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(DataUtils.getINSTANCE(this).getLanguage().getAnalyze());
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setCancelable(false);
        Log.e("Camera ACtivity","Fil send size: "+imgFile.length()/1024);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imgFile.getName(), requestFile);
        RequestBody language =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), DataUtils.getINSTANCE(this).getLanguage().getKey());
        RestService service = new RestService();
        Call<ImageResponseModel> call = service.getImageService().sentImage(language, body);
        call.enqueue(new Callback<ImageResponseModel>() {
            @Override
            public void onResponse(Call<ImageResponseModel> call, Response<ImageResponseModel> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getStatus() == 200) {
                        Intent intent = new Intent(CameraActivity.this, ResultActivity.class);
                        intent.putExtra("filePath", imageFile.getAbsolutePath());
                        intent.putExtra("result", response.body().getMessage());
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    } else {
                        openCamera(cameraId);
                        Toast.makeText(CameraActivity.this,
                                response.body().getError().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    openCamera(cameraId);
                    Toast.makeText(CameraActivity.this,
                            DataUtils.getINSTANCE(CameraActivity.this).
                                    getLanguage().
                                    getServerError() + ": " + response.code() + ": " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ImageResponseModel> call, Throwable t) {
                openCamera(cameraId);
                progressDialog.dismiss();
                Toast.makeText(CameraActivity.this,
                        DataUtils.getINSTANCE(CameraActivity.this).getLanguage().getConnectionFail(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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
