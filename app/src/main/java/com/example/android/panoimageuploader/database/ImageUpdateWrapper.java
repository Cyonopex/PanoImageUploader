package com.example.android.panoimageuploader.database;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class ImageUpdateWrapper {

    @SerializedName("data")
    private List<ImageUpdate> imgUpdate;

    public List<ImageUpdate> getImageUpdate() {
        return imgUpdate;
    }

    public void setImageUpdate(List<ImageUpdate> imgUpdate) {
        this.imgUpdate = imgUpdate;
    }
}
