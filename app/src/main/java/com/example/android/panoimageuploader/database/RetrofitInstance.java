package com.example.android.panoimageuploader.database;

import com.example.android.panoimageuploader.PanoApplication;
import com.example.android.panoimageuploader.util.NetworkUtils;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class RetrofitInstance {

    public interface RestApiService {

        @GET("allimages")
        Call<ImageUpdateWrapper> getImageUpdate();
    }

    private static Retrofit retrofit = null;
    private static String currIP = NetworkUtils.getBaseUri(PanoApplication.getContext()).toString();

    public static RestApiService getApiService() {

        String newIP = NetworkUtils.getBaseUri(PanoApplication.getContext()).toString();

        if (retrofit == null || !currIP.equals(newIP)) {
            retrofit = new Retrofit
                    .Builder()
                    .baseUrl(NetworkUtils.getBaseUri(PanoApplication.getContext()).toString() + '/')
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(RestApiService.class);
    }
}
