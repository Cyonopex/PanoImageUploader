package com.example.android.panoimageuploader;

import android.content.Context;
import android.util.Log;

import com.example.android.panoimageuploader.database.AppDatabase;
import com.example.android.panoimageuploader.database.ImageDetails;
import com.example.android.panoimageuploader.util.AppExecutors;

import net.gotev.uploadservice.data.UploadInfo;
import net.gotev.uploadservice.network.ServerResponse;
import net.gotev.uploadservice.observer.request.RequestObserverDelegate;

public class ImageUploadBroadcastReceiver implements RequestObserverDelegate {

    private static final String TAG = ImageDetailsViewModel.class.getSimpleName();

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {

    }

    @Override
    public void onSuccess(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        Log.d(TAG, "Image successfully uploaded");
        final AppDatabase aDb = AppDatabase.getInstance(context);
        final String uuid = uploadInfo.getUploadId();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ImageDetails details = aDb.imageDetailsDao().loadImageDetailsByUid(uuid);

                //update details to SUCCESS
                details.setStatus(ImageDetails.PROCESSING);

                aDb.imageDetailsDao().updateImageDetails(details);
            }
        });
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, Throwable throwable) {

        Log.e(TAG, "Upload error");
        final AppDatabase aDb = AppDatabase.getInstance(context);
        final String uuid = uploadInfo.getUploadId();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ImageDetails details = aDb.imageDetailsDao().loadImageDetailsByUid(uuid);

                //update details to SUCCESS
                details.setStatus(ImageDetails.UPLOAD_FAILED);

                aDb.imageDetailsDao().updateImageDetails(details);
            }
        });
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo) {
        Log.d(TAG, "Upload process complete");
    }

    @Override
    public void onCompletedWhileNotObserving() {
        Log.d(TAG, "Upload process complete while not observing");
    }

}
