package com.superapp.wondercamera.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.ads.banner.RevMobBanner;
import com.superapp.wondercamera.R;
import com.superapp.wondercamera.model.ResponseModel;
import com.superapp.wondercamera.model.StoreVersion;
import com.superapp.wondercamera.service.RestService;
import com.superapp.wondercamera.util.DataUtils;
import com.superapp.wondercamera.util.RequestPermissionListener;
import com.superapp.wondercamera.util.Util;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RequestPermissionListener {
    private static final int IMAGE_PICK = 1001;
    private Button btnTakePhoto;
    private Button btnChoosePhoto;
    private ProgressDialog progressDialog;
    private RevMob revmob;
    private RevMobBanner banner;

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
        Button btnSetting = (Button) findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(this);
        requestCamera();
        startRevMobSession();

    }

    private void checkVersion() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);
        String packageName;
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager()
                    .getPackageInfo(
                            getPackageName(), 0);
            packageName = pInfo.packageName;

            if (packageName != null && !packageName.trim().equals("")) {
                RestService service = new RestService();
                Call<ResponseModel<StoreVersion>> call = service.getImageService().getVersion(packageName, "android");
                final PackageInfo finalPInfo = pInfo;
                call.enqueue(new Callback<ResponseModel<StoreVersion>>() {
                    @Override
                    public void onResponse(Call<ResponseModel<StoreVersion>> call, Response<ResponseModel<StoreVersion>> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body().getSuccess() == 1) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(
                                        MainActivity.this);


                                alert.setTitle("Version info")
                                        .setMessage("Your version is: " + finalPInfo.versionCode + "\nNewest version is: " + response.body().getResult().getVersionCode().getVersionNumber())
                                        .setPositiveButton("CHPlay", null)
                                        .setNegativeButton("Close", null);

                                final AlertDialog dialog = alert.create();
                                dialog.show();

                                Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                btnOk.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        String packageName;
                                        packageName = finalPInfo.packageName;
                                        if (packageName != null && !packageName.equals("")) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("market://details?id="
                                                    + packageName));
                                            startActivity(intent);
                                        }
                                        dialog.dismiss();
                                    }
                                });
                            } else {
                                Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, DataUtils.getINSTANCE(MainActivity.this).getLanguage().getServerError() + ": " + response.code() + ": " + response.message(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseModel<StoreVersion>> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, DataUtils.getINSTANCE(MainActivity.this).getLanguage().getConnectionFail(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_take_photo:
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_setting:
                showPopupMenu(view);
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
            File f = null;
            try {
                f = Util.saveImageFileFromUri(this, data.getData(), Util.getPhotoFile(getCacheDir().getAbsolutePath(),"ChooseImage",null) , -1, Bitmap.CompressFormat.JPEG, 100);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(MainActivity.this, ChooseActivity.class);
            intent.putExtra("filePath", f.getAbsolutePath());
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

    public void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        final MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_setting, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.getMenu().getItem(0).setTitle(DataUtils.getINSTANCE(this).getLanguage().getLanguage());
        popupMenu.getMenu().getItem(1).setTitle(DataUtils.getINSTANCE(this).getLanguage().getCheckUpdate());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.mn_langguage:
                        Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.mn_checkUpdate:
                        checkVersion();
                        return true;

                }
                return false;
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
