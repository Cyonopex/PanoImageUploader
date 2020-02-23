package com.example.android.panoimageuploader.database;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.android.panoimageuploader.util.AppExecutors;
import com.example.android.panoimageuploader.util.DataUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageDetailsRepository {

    // Time in Minutes that image is allowed to process before timing out
    private static final int PROCESSING_TIMEOUT = 5;

    private static final String TAG = ImageDetailsRepository.class.getSimpleName();
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

                            Date currentDate = new Date();

                            if (fileNamesInUpdate.contains(imageNameWithoutExtension)) {
                                details.setStatus(ImageDetails.COMPLETED);
                                updatedDetails.add(details);
                            } else if (getTimeElapsed(details.getDateTimeOfUpload()) > PROCESSING_TIMEOUT) {
                                details.setStatus(ImageDetails.PROCESSING_FAILED);
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

    private long getTimeElapsed(Date date) {
        Date currentDate = new Date();
        long diffInMillisec = currentDate.getTime() - date.getTime();
        long diff = TimeUnit.MINUTES.convert(diffInMillisec, TimeUnit.MILLISECONDS);
        return diff;
    }

    public void createNewImageDetail(final Uri imageUri, final String uploadUuid) {

        final String fileName = imageUri.getLastPathSegment();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = DataUtils.createThumbnail(imageUri);
                byte[] bytes;
                if (bitmap != null) {
                    bytes = DataUtils.getBytesFromBitmap(bitmap);
                } else {
                    bytes = null;
                }

                ImageDetails details = new ImageDetails(fileName, ImageDetails.UPLOADING, uploadUuid, bytes);
                db.imageDetailsDao().insertImageDetails(details);
            }
        });
    }

    public void removeImageDetails(final ImageDetails details) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.imageDetailsDao().deleteImageDetails(details);
            }
        });
    }

    public void addImageDetails(final ImageDetails details) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.imageDetailsDao().insertImageDetails(details);
            }
        });
    }

}
