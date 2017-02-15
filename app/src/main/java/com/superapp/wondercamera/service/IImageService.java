package com.superapp.wondercamera.service;

import com.superapp.wondercamera.model.ResponseModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by ManhNV on 2/11/17.
 */

public interface IImageService {

    @Multipart
    @POST("/emotion/file")
    Call<ResponseModel> sentImage(@Part("language") RequestBody language,
                                  @Part MultipartBody.Part file
    );

}
