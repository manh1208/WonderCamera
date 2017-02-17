package com.superapp.wondercamera.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.superapp.wondercamera.model.Language;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by ManhNV on 3/8/2016.
 */
public class DataUtils {
    private static DataUtils INSTANCE = null;
    private int mCurrentLanguage;
    private HashMap<Integer, Language> mListLanguage;
    private List<String> mListLanguageName;
    private Context mContext;
    private HashMap<Integer, String> mCards;
    private MediaPlayer mp;


    private DataUtils(Context context) {
        mContext = context;
        CreateListLanguage(getJsonObject());
        mCurrentLanguage = 1;
//        CreateCards();
//        mp = MediaPlayer.create(context,R.raw.click);


    }

    public static synchronized DataUtils getINSTANCE(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DataUtils(context);
        }
        return INSTANCE;
    }

    public static int random(int min, int max) {
        Random r = new Random();
        int ran = r.nextInt();
        ran = ran > 0 ? ran : ran * -1;
        ran = min + ran % (max - min + 1);
        return ran;
    }

    private String getJsonObject() {
        String json = "";

        Log.d("DataUtils", "Get JSON data language");
        json = loadJSONFromAsset("json/language.json");
        return json;
    }

    private String loadJSONFromAsset(String jsonFile) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open(jsonFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void CreateListLanguage(String json) {
        mListLanguage = new HashMap<Integer, Language>();
        mListLanguageName = new ArrayList<String>();
        List<Language> result = new ArrayList<Language>();
        try {
            JSONObject jsonArrObject = new JSONObject(json);
            JSONArray jsonArr = jsonArrObject.getJSONArray("languageList");
            JSONObject jsonObj = null;
            Gson gson = new Gson();
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonObj = jsonArr.getJSONObject(i);
                result.add(gson.fromJson(jsonObj.toString(), Language.class));
            }

            for (Language item : result) {
                mListLanguageName.add(item.getName());
                mListLanguage.put(item.getId(), item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public Language getLanguage() {
        return mListLanguage.get(mCurrentLanguage);
    }

    public void setmCurrentLanguage(int languageId) {
        this.mCurrentLanguage = languageId;
    }

    public HashMap<Integer, Language> getListLanguage() {
        return mListLanguage;
    }

    public List<String> getListLanguageName() {
        return mListLanguageName;
    }



}
