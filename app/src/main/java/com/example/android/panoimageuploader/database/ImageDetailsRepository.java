package com.example.android.panoimageuploader.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.android.panoimageuploader.ImageDetailsViewModel;
import com.example.android.panoimageuploader.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageDetailsRepository {

    private static final String TAG = ImageDetailsViewModel.class.getSimpleName();
    private LiveData<List<ImageDetails>> imageDetails;
    private Application application;
    private AppDatabase db;

    public ImageDetailsRepository(Application application) {
        this.application = application;
        db = AppDatabase.getInstance(application);
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

    private void updateImageStatusInDatabase(final List<ImageUpdate> updates) {
        // Business Logic:
        // if server has an image that's available but phone says in progress, update to complete
        // if server has image that's missing but phone says complete, update to not on server

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                List<ImageDetails> currentListOfImageDetails = db.imageDetailsDao().LoadAllDetailsNonLive();

                Set<String> fileNamesInUpdate = getFileNameSet(updates);
                //Stores the ImageDetail objects for any updated entry
                List<ImageDetails> updatedDetails = new ArrayList<>();

                for (ImageDetails details : currentListOfImageDetails) {



                    if (details.getStatus() == ImageDetails.PROCESSING ||
                            details.getStatus() == ImageDetails.COMPLETED) {

                        String imageName = details.getImageName();
                        String imageNameWithoutExtension = imageName
                                .substring(0, imageName.lastIndexOf('.'));

                        if (details.getStatus() == ImageDetails.PROCESSING) {

                            if (fileNamesInUpdate.contains(imageNameWithoutExtension)) {
                                details.setStatus(ImageDetails.COMPLETED);
                                updatedDetails.add(details);
                            }

                        } else if (details.getStatus() == ImageDetails.COMPLETED) {

                            if (!fileNamesInUpdate.contains(imageNameWithoutExtension)) {
                                details.setStatus(ImageDetails.MISSING);
                                updatedDetails.add(details);
                            }

                        }

                    }

                }
                for (ImageDetails detail : updatedDetails) {
                    db.imageDetailsDao().updateImageDetails(detail);
                }
            }
        });



    }

    // convert the list of updates to a set of file names for easy searching
    private Set<String> getFileNameSet(List<ImageUpdate> updates) {
        HashSet set = new HashSet();
        for (ImageUpdate update : updates) {
            String str = update.getFileName();
            str = str.substring(0, str.lastIndexOf('.'));
            set.add(str);
        }

        return set;
    }

}
