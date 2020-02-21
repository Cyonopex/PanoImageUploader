package com.example.android.panoimageuploader.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.android.panoimageuploader.ImageDetailsViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageDetailsRepository {

    private static final String TAG = ImageDetailsViewModel.class.getSimpleName();
    private LiveData<List<ImageDetails>> imageDetails;
    private Application application;

    public ImageDetailsRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getInstance(application);
        Log.d(TAG, "Retrieving tasks from Database");
        imageDetails = db.imageDetailsDao().loadAllDetails();
    }


    public LiveData<List<ImageDetails>> getImageDetails() {
        return imageDetails;
    }

    public void getImageUpdateFromServer() {


        RetrofitInstance.RestApiService apiService = RetrofitInstance.getApiService();
        Call<ImageUpdateWrapper> call = apiService.getImageUpdate();
        call.enqueue(new Callback<ImageUpdateWrapper>() {
            @Override
            public void onResponse(Call<ImageUpdateWrapper> call, Response<ImageUpdateWrapper> response) {

                ImageUpdateWrapper wrapper = response.body();

                if (wrapper != null) {

                    if (wrapper.getImageUpdate() != null) {

                        List<ImageUpdate> updates = wrapper.getImageUpdate();
                        updateImageStatusInDatabase(updates);

                    } else {
                        Log.d(TAG, "No images found on server");
                    }

                } else {
                    Log.e(TAG, "No response from server");
                }
            }

            @Override
            public void onFailure(Call<ImageUpdateWrapper> call, Throwable t) {
                Log.e(TAG, "Unable to call API on server, is server online?");
            }
        });
    }

    private void updateImageStatusInDatabase(List<ImageUpdate> updates) {

    }
}
