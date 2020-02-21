package com.example.android.panoimageuploader.database;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class ImageUpdate {

    @SerializedName("filename")
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @NonNull
    @Override
    public String toString() {
        return "Image Update object with filename: " + fileName;
    }
}
