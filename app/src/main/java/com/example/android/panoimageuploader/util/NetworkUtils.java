package com.example.android.panoimageuploader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import com.example.android.panoimageuploader.R;

public class NetworkUtils {

    public static final String LOCALHOST = "http://192.168.43.242:5000/upload";

    public static Uri getBaseUri(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String baseUri = sp.getString(context.getString(R.string.ip_key),
                context.getString(R.string.ip_default));

        Uri.Builder builder = new Uri.Builder();
        Uri finalUri = builder.scheme("http").encodedAuthority(baseUri).build();
        return finalUri;
    }

    public static Uri getUploadUri(Context context) {
        Uri baseUri = getBaseUri(context);
        return baseUri.buildUpon().appendPath("upload").build();
    }

    public static Uri getImagesUri(Context context) {
        Uri baseUri = getBaseUri(context);
        return baseUri.buildUpon().appendPath("images").build();
    }
}
