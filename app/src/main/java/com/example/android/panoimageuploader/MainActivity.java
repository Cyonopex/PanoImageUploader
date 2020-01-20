package com.example.android.panoimageuploader;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.android.panoimageuploader.util.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>  {

    private TextView tv;
    private ProgressBar imageSendProgressBar;
    private int PermissionRequest = 1;
    private int IntentRequest = 2;
    private Bitmap bitmap;
    private ImageView imageView;

    private static final String TAG = "MainActivity";

    private static final int LOADERID = 10;

    private static final String URI_STRING_KEY = "uristring";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tv = (TextView) findViewById(R.id.mainTextView);
        imageSendProgressBar = (ProgressBar) findViewById(R.id.sendingImageLoadingBar);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void uploadImage() {
        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionRequest);
            } else {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                photoPickerIntent.setType("image/");
                startActivityForResult(photoPickerIntent, IntentRequest);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionRequest) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                photoPickerIntent.setType("image/");
                startActivityForResult(photoPickerIntent, IntentRequest);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IntentRequest) {

            Log.e(TAG, "Intent Request");

            ArrayList<Uri> imageUris = new ArrayList<>();

            if(resultCode == Activity.RESULT_OK) {

                Log.e(TAG, "Activity REsult OK");

                if(data.getClipData() != null) {
                    //int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.

                    //for(int i = 0; i < count; i++) {
                    //Uri imageUri = data.getClipData().getItemAt(i).getUri();

                    //   Log.e(TAG, "MultiImage URI = " + imageUri.toString());
                    //   imageUris.add(imageUri);

                    ClipData.Item item = data.getClipData().getItemAt(0);

                    Uri uri = item.getUri();
                    Log.e(TAG, "MultiImage URI = " + uri.toString());
                    imageUris.add(uri);


                } else if(data.getData() != null) {
                    Uri imagePath = data.getData();

                    Log.e(TAG, "Single Image URI = " + imagePath.toString());
                    imageUris.add(imagePath);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                        imageView.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Image cannot be found");
                    }

                }

            } else {

                Log.e(TAG, "No data found");
            }
            //ArrayList<String> uristr = new ArrayList<>();
            //for (Uri uri : imageUris) {
            //    uristr.add(uri.toString());
            //}

            //Bundle images = new Bundle();
            //images.putStringArrayList(URI_STRING_KEY, uristr);

            //getSupportLoaderManager().initLoader(LOADERID, images, this);

            Uri fileUrl = imageUris.get(0);
            Log.e(TAG, "File URL is" + fileUrl);

            uploadImages(imageUris);
        }
    }

    public interface FileUploadService {

        @Multipart
        @POST("YOUR_URL/image_uploader.php")
        Call<Response> uploadImages( @Part List<MultipartBody.Part> images);
    }

    public class Response {
        private String error;
        private String message;

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }
    }

    void uploadImages(List<Uri> paths) {
        Log.e(TAG, "point 1");
        List<MultipartBody.Part> list = new ArrayList<>();
        int i = 0;
        for (Uri uri : paths) {
            String fileName = new File(uri.getPath()).getName();
            //very important files[]
            MultipartBody.Part imageRequest = prepareFilePart("files[]", uri);
            list.add(imageRequest);
        }
        Log.e(TAG, "point 2");
        setLoading();
        Retrofit builder = new Retrofit.Builder().baseUrl(NetworkUtils.LOCALHOST).addConverterFactory(GsonConverterFactory.create()).build();
        FileUploadService fileUploadService  = builder.create(FileUploadService.class);
        Call<Response> call = fileUploadService.uploadImages(list);
        Log.e(TAG, "point 5");
        call.enqueue(new Callback<Response>() {

            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Log.e("main", "the message is ----> " + response.body().getMessage());
                Log.e("main", "the error is ----> " + response.body().getError());
                tv.setText(response.body().getMessage());
                setFinished();
            }

            @Override
            public void onFailure(Call<Response> call, Throwable throwable) {
                Log.e("main", "on error is called and the error is  ----> " + throwable.getMessage());
                tv.setText(throwable.getMessage());
                setFinished();
            }
        });
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = new File(fileUri.getPath());

        if (file == null) {
            Log.e(TAG, "File is NULL");
        }

        Log.e(TAG, "point 3");
        //compress the image using Compressor lib
        //Log.d(TAG,"size of image before compression --> " + file.getTotalSpace());
        //compressedImageFile = new Compressor(this).compressToFile(file);
        //Log.d(TAG, "size of image after compression --> " + compressedImageFile.getTotalSpace());
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file);
        Log.e(TAG, "point 4");
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    public void setLoading() {
        imageSendProgressBar.setVisibility(View.VISIBLE);
        tv.setVisibility(View.INVISIBLE);
    }

    public void setFinished() {
        imageSendProgressBar.setVisibility(View.INVISIBLE);
        tv.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String response;

            @Override
            protected void onStartLoading() {
                //if (args == null) {
                //    return;
                //}

                setLoading();

                if (response != null) {
                    deliverResult(response);
                } else {

                    forceLoad();
                }

            }

            @Nullable
            @Override
            public String loadInBackground() {
                String url = NetworkUtils.LOCALHOST;
                List<String> uristr = args.getStringArrayList(URI_STRING_KEY);
                List<Uri> uris = new ArrayList<>();
                for (String str : uristr) {
                    uris.add(Uri.parse(str));
                }
                uploadImages(uris);
                return null;
//                try {
//                    Uri builtUri = Uri.parse(NetworkUtils.LOCALHOST).buildUpon()
//                            .appendPath("test")
//                            .build();
//                    URL serverURL = new URL(builtUri.toString());
//                    String response = NetworkUtils.getResponseFromHttpUrl(serverURL);
//                    return response;
//
//                } catch (MalformedURLException e) {
//
//                    e.printStackTrace();
//                    Log.e(TAG, "URL given is invalid");
//                    return null;
//
//                } catch (IOException e) {
//
//                    e.printStackTrace();
//                    Log.e(TAG, "Unable to connect to server");
//                    return null;
//                }

            }

            @Override
            public void deliverResult(@Nullable String data) {
                response = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {

        setFinished();
        if (data == null) {
            //show an error message
            tv.setText("Unable to get response");
        } else {

            tv.setText(data);

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
