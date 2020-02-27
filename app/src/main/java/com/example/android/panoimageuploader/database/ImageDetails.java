package com.example.android.panoimageuploader.database;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Arrays;
import java.util.Date;

@Entity(tableName = "ImageDetails")
public class ImageDetails {

    public final static int UPLOADING = 0;
    public final static int PROCESSING = 1;
    public final static int COMPLETED = 2;
    public final static int MISSING = 3;
    public final static int UPLOAD_FAILED = 4;
    public final static int PROCESSING_FAILED = 5;

    private final static int[] deletableStatuses = {UPLOADING, COMPLETED, MISSING, UPLOAD_FAILED, PROCESSING_FAILED};

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

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] thumbnail;

    public ImageDetails(int id, String imageName, int status, String uploadUID, byte[] thumbnail) {
        this(imageName, status, uploadUID, thumbnail);
        this.id = id;
    }

    @Ignore
    public ImageDetails(String imageName, int status, String uploadUID, byte[] thumbnail) {
        this(imageName, status, thumbnail);
        this.uploadUID = uploadUID;
    }

    @Ignore
    public ImageDetails(String imageName, int status, byte[] thumbnail) {
        this.imageName = imageName;
        this.status = status;
        this.dateTimeOfUpload = new Date();
        this.thumbnail = thumbnail;
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

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isDeletable() {
        for (int statuses : deletableStatuses) {
            if (statuses == this.status) {
                return true;
            }
        }
        return false;

    }

    @NonNull
    @Override
    public String toString() {
        return imageName + " status: " + status + " uuid: " + uploadUID;
    }
}
