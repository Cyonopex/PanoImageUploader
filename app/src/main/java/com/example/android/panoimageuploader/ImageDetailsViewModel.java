package com.example.android.panoimageuploader;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.android.panoimageuploader.database.AppDatabase;
import com.example.android.panoimageuploader.database.ImageDetails;

import java.util.List;

public class ImageDetailsViewModel extends AndroidViewModel {

    private static final String TAG = ImageDetailsViewModel.class.getSimpleName();

    private LiveData<List<ImageDetails>> imageDetails;

    public ImageDetailsViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Retrieving tasks from Database");
        imageDetails = db.taskDao().loadAllDetails();
    }

    public LiveData<List<ImageDetails>> getImageDetails() {
        return imageDetails;
    }
}
