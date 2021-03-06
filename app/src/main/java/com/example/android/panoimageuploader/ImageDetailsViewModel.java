package com.example.android.panoimageuploader;

import android.app.Application;
import android.net.Uri;

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

    public void createNewImageDetail(final Uri imageUri, final String uploadUuid) {
        idRepo.createNewImageDetail(imageUri, uploadUuid);
    }

    public void addImageDetail(ImageDetails detail) {
        idRepo.addImageDetails(detail);
    }

    public void removeImageDetails(ImageDetails detail) {
        idRepo.removeImageDetails(detail);
    }

}
