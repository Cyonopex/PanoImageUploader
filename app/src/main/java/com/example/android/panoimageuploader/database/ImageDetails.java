package com.example.android.panoimageuploader.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "ImageDetails")
public class ImageDetails {

    public final static int UPLOADING = 0;
    public final static int PROCESSING = 1;
    public final static int COMPLETED = 2;
    public final static int UPLOAD_FAILED = 3;
    public final static int MISSING = 4;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String imageName;
    private int status;
    private String uploadUID;
    private Date dateTimeOfUpload;

    public ImageDetails(int id, String imageName, int status, String uploadUID) {
        this(imageName, status, uploadUID);
        this.id = id;
    }

    @Ignore
    public ImageDetails(String imageName, int status, String uploadUID) {
        this(imageName, status);
        this.uploadUID = uploadUID;
    }

    @Ignore
    public ImageDetails(String imageName, int status) {
        this.imageName = imageName;
        this.status = status;
        dateTimeOfUpload = new Date();
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUploadUID() {
        return uploadUID;
    }

    public void setUploadUID(String uploadUID) {
        this.uploadUID = uploadUID;
    }

    public Date getDateTimeOfUpload() {
        return dateTimeOfUpload;
    }

    public void setDateTimeOfUpload(Date dateTimeOfUpload) {
        this.dateTimeOfUpload = dateTimeOfUpload;
    }

    @NonNull
    @Override
    public String toString() {
        return imageName + " status: " + status + " uuid: " + uploadUID;
    }
}
