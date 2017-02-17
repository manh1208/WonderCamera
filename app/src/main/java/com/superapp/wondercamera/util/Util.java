package com.superapp.wondercamera.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.widget.Toast;

import com.superapp.wondercamera.activity.CameraActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by ManhNV on 10/30/16.
 */
public class Util {

    // Orientation hysteresis amount used in rounding, in degrees
    private static final int ORIENTATION_HYSTERESIS = 5;

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static int getDisplayOrientation(int degrees, int cameraId) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
                                     int viewWidth, int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }

    public static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }

    public static File saveImageFileFromUri(Context context, Uri uri, File file, int maxPixel,
                                            Bitmap.CompressFormat type, int quality) throws IOException {
        InputStream imageStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        return saveImageFileFromBitmap(bitmap, file, maxPixel, type, quality);
    }

    public static File compressImageFileFromFile(File file, File file2, int maxPixel,
                                                 Bitmap.CompressFormat type, int quality) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        return saveImageFileFromBitmap(bitmap, file2, maxPixel, type, quality);
    }

    public static File saveImageFileFromBitmap(Bitmap bitmap, File file, int maxPixel,
                                               Bitmap.CompressFormat type, int quality) throws IOException {

        if (maxPixel > 0) {
            bitmap = scaleDown(bitmap, maxPixel, false);
        }
        File f = file;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(type, quality, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return f;

    }


    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width,
                height, filter);
    }

    public static File getPhotoFile(String rootPath, String name, String folderName) throws IOException {
        String state = Environment.getExternalStorageState();

        File folder;
        if (state.contains(Environment.MEDIA_MOUNTED)) {
            folder = new File(rootPath + (folderName!=null && folderName.trim().length() > 0 ? "/" + folderName : ""));
        } else {
            folder = new File(rootPath + (folderName!=null && folderName.trim().length() > 0 ? "/" + folderName : ""));
        }

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        File imageFile;
        if (success) {
            imageFile = new File(folder.getAbsolutePath()
                    + File.separator
                    + name
                    + System.currentTimeMillis() % 100000
                    + ".jpg");

            imageFile.createNewFile();
            Log.d("camera", imageFile.getAbsolutePath());
            return imageFile;
        } else {
            return null;
        }

    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

}