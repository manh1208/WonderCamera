package com.superapp.wondercamera.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.superapp.wondercamera.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CamerasActivity extends AppCompatActivity {

    private static final int TAKE_PICTURE = 1000;
    private File imageFile;
    private boolean isOpenedCamera;
    private boolean isComeBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameras);
        isOpenedCamera = false;
        takePhoto();

//        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);

    }


    private void takePhoto() {
        isOpenedCamera = true;
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
                        + System.currentTimeMillis() % 100000
                        + "Image.jpg");

                imageFile.createNewFile();
                Log.d("camera", imageFile.getAbsolutePath());
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile.getAbsoluteFile());
                startActivityForResult(cameraIntent, TAKE_PICTURE);

            } else {
                Toast.makeText(getBaseContext(), "Image Not saved",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            File f = getImageFileFromUri(this, data.getData(), imageFile.getAbsolutePath(), 1000, Bitmap.CompressFormat.JPEG, 50);

            Intent intent = new Intent(CamerasActivity.this,ResultActivity.class);
            intent.putExtra("filePath",imageFile.getAbsolutePath());
            startActivity(intent);
            finish();
            isOpenedCamera = false;
        }
    }

    public static File getImageFileFromUri(Context context, Uri imageUri, String imagePath, int maxPicel, Bitmap.CompressFormat imageType, int imageQuality) {

        File f = new File(imagePath);
        try {
            f.createNewFile();
            InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            Matrix rotateMatrix = new Matrix();

            rotateMatrix.postRotate(-90);
            bitmap = Bitmap.createBitmap(bitmap, 0,
                    0, bitmap.getWidth(), bitmap.getHeight(),
                    rotateMatrix, false);
            bitmap = scaleDown(bitmap, maxPicel, false);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(imageType, imageQuality /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
    public static String getPicturePath(String name) {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + name;
        return dir;
    }

    @Override
    protected void onResume() {
//        if (!isOpenedCamera){
//            takePhoto();
//        }
//        takePhoto();
        super.onResume();
    }
}
