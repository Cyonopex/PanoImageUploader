package com.example.android.panoimageuploader;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.android.panoimageuploader.database.ImageDetails;
import com.example.android.panoimageuploader.database.ImageDetailsRepository;

import java.util.List;

public class ImageDetailsViewModel extends AndroidViewModel {

    private static final String TAG = ImageDetailsViewModel.class.getSimpleName();

    private ImageDetailsRepository idRepo;

    public ImageDetailsViewModel(@NonNull Application application) {
        super(application);
        idRepo = new ImageDetailsRepository(application);
    }

    public LiveData<List<ImageDetails>> getImageDetails() {
        return idRepo.getImageDetails();
    }

    public void getImageUpdatesFromServer() {
        idRepo.getImageUpdateFromServer();
    }
}
