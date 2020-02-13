package com.example.android.panoimageuploader.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "ImageDetails")
public class ImageDetails {

    public final static int UPLOADING = 0;
    public final static int PROCESSING = 1;
    public final static int COMPLETED = 2;

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

    @Ignore
    public ImageDetails(String imageName, int status) {
        this.imageName = imageName;
        this.status = status;
    }


    public ImageDetails(int id,String imageName, int status) {
        this(imageName, status);
        this.id = id;
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
}
