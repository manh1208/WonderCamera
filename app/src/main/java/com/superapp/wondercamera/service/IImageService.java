package com.superapp.wondercamera.service;

import com.superapp.wondercamera.model.ImageResponseModel;
import com.superapp.wondercamera.model.ResponseModel;
import com.superapp.wondercamera.model.StoreVersion;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by ManhNV on 2/11/17.
 */

public interface IImageService {

    @Multipart
    @POST("/emotion/file")
    Call<ImageResponseModel> sentImage(@Part("language") RequestBody language,
                                       @Part MultipartBody.Part file
    );

    @GET("/setting/version")
    Call<ResponseModel<StoreVersion>> getVersion(@Query("bundle") String bundleId,
                                                 @Query("platform") String platform);

}
