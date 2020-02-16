package com.example.android.panoimageuploader;

import android.content.Context;
import android.util.Log;

import com.example.android.panoimageuploader.database.AppDatabase;
import com.example.android.panoimageuploader.database.ImageDetails;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

public class ImageUploadBroadcastReceiver extends UploadServiceBroadcastReceiver {

    private static final String TAG = "UploadBroadRec";

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
        super.onError(context, uploadInfo, serverResponse, exception);

        Log.e(TAG, "Upload image failed");

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
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        super.onCompleted(context, uploadInfo, serverResponse);

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
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        super.onCancelled(context, uploadInfo);

        // Maybe one day I shall implement a cancel upload function...
    }

}
