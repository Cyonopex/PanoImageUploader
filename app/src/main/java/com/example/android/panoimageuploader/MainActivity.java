package com.example.android.panoimageuploader;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.android.panoimageuploader.database.AppDatabase;
import com.example.android.panoimageuploader.database.ImageDetails;
import com.example.android.panoimageuploader.util.AppExecutors;
import com.example.android.panoimageuploader.util.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest;

import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements ImageDetailsAdapter.ImageDetailsAdapterOnClickHandler {


    private TextView tv;
    private ProgressBar imageSendProgressBar;
    private int PermissionRequest = 1;
    private int IntentRequest = 2;
    private int pickReqCode = 3;
    private Bitmap bitmap;
    private ImageView imageViews[];
    private RecyclerView mRecyclerView;
    private ImageDetailsAdapter mAdapter;
    private static final String TAG = "MainActivity";
    private AppDatabase mDb;
    ImageDetailsViewModel viewModel;

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
                viewModel.getImageUpdatesFromServer();
                //startImageIntentRequest();
                //Snackbar.make(view, NetworkUtils.getImagesUri(MainActivity.this).toString(), Snackbar.LENGTH_LONG)
                //       .setAction("Action", null).show();
            }
        });

        tv = (TextView) findViewById(R.id.mainTextView);
        imageSendProgressBar = (ProgressBar) findViewById(R.id.sendingImageLoadingBar);
        imageViews = new ImageView[3];
        //imageViews[0] = (ImageView) findViewById(R.id.imageView1);
        //imageViews[1] = (ImageView) findViewById(R.id.imageView2);
        //imageViews[2] = (ImageView) findViewById(R.id.imageView3);

        mRecyclerView = (RecyclerView) findViewById(R.id.image_list_rv);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ImageDetailsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        //dummyData = DataUtils.getDummyData();
        //mAdapter.setImageData(dummyData);
        mDb = AppDatabase.getInstance(getApplicationContext());

        setupViewModel();
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
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startImageIntentRequest() {
        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionRequest);
            } else {
                //Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                //photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                //photoPickerIntent.setType("image/");
                //startActivityForResult(photoPickerIntent, IntentRequest);
                pickFile();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionRequest) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFile();
            }
        }
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, pickReqCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == pickReqCode) {

            Log.d(TAG, "User has made Intent Request for photo picker");

            ArrayList<Uri> imageUris = new ArrayList<>();

            if(resultCode == Activity.RESULT_OK) {

                Log.d(TAG, "Result of photopicker intent Result OK");

                if(data.getClipData() != null) {

                    int count = data.getClipData().getItemCount();
                    for (int i=0; i < count; i++) {
                        ClipData.Item item = data.getClipData().getItemAt(i);

                        Uri uri = item.getUri();
                        Log.d(TAG, "MultiImage URI = " + uri.toString());

                        String path = getPath(uri);
                        Log.d(TAG, path);

                        imageUris.add(Uri.parse(path));

                    }

                } else if(data.getData() != null) {
                    Uri imagePath = data.getData();

                    Log.d(TAG, "Single Image URI = " + imagePath.toString());
                    String path = getPath(imagePath);
                    Log.d(TAG, path);

                    imageUris.add(Uri.parse(path));
                }
            } else {
                Log.d(TAG, "No data found");
            }
            if (imageUris.size() > 0) {
                uploadImage(imageUris);
            }
            //else do nothing if user does not select any files
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void uploadImage(List<Uri> imagePath) {

        Log.d(TAG, "Uploading to " + NetworkUtils.getUploadUri(this));

        final String fileName = imagePath.get(0).getLastPathSegment();
        final String uploadUuid = UUID.randomUUID().toString(); // Keep track of uploads

        try {
            MultipartUploadRequest req = new MultipartUploadRequest(this,
                    NetworkUtils.getUploadUri(this).toString())
                    .setMethod("POST")
                    .setUploadID(uploadUuid)
                    .setMaxRetries(1);

            for (Uri uri : imagePath) {
                Log.d(TAG, "Adding file: " + uri.getPath());
                req.addFileToUpload(uri.getPath(), "files[]");
            }

            req.startUpload();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Unable to upload file");
            Toast.makeText(this, "Unable to upload file", Toast.LENGTH_LONG).show();
        }

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                ImageDetails details = new ImageDetails(fileName, ImageDetails.UPLOADING, uploadUuid);
                mDb.imageDetailsDao().insertImageDetails(details);
            }
        });
    }


    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    @Override
    public void onClick(String imageName) {

    }

    private void setupViewModel() {

        viewModel = ViewModelProviders.of(this)
                .get(ImageDetailsViewModel.class);

        viewModel.getImageDetails().observe(this, new Observer<List<ImageDetails>>() {
            @Override
            public void onChanged(List<ImageDetails> imageDetails) {
                Log.d(TAG, "Updating image details from LiveData in ViewModel");
                mAdapter.setImageData(imageDetails);
            }
        });
    }
}
