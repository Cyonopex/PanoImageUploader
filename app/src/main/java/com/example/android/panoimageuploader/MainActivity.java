package com.example.android.panoimageuploader;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.panoimageuploader.database.AppDatabase;
import com.example.android.panoimageuploader.database.ImageDetails;
import com.example.android.panoimageuploader.util.AppExecutors;
import com.example.android.panoimageuploader.util.DataUtils;
import com.example.android.panoimageuploader.util.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements ImageDetailsAdapter.ImageDetailsAdapterOnClickHandler {


    private TextView tv;
    private ProgressBar imageSendProgressBar;
    private int PermissionRequest = 1;
    private int pickReqCode = 3;
    private RecyclerView mRecyclerView;
    private ImageDetailsAdapter mAdapter;
    private View mainActivityParentView;
    private static final String TAG = ImageDetailsViewModel.class.getSimpleName();
    private AppDatabase mDb;
    private ImageDetailsViewModel viewModel;
    private ImageView mImageView;

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
                startImageIntentRequest();
            }
        });

        mainActivityParentView = findViewById(R.id.main_activity_parent_view);
        mRecyclerView = findViewById(R.id.image_list_rv);
        mImageView = findViewById(R.id.imageViewTest);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ImageDetailsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

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

        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;

        } else if (id == R.id.refresh_image_details) {
            viewModel.getImageUpdatesFromServer();
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
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.CATEGORY_OPENABLE, true);

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
                        Log.e(TAG, "MultiImage URI = " + uri.toString());

                        String path = DataUtils.getFilePath(this, uri);
                        Log.d(TAG, path);

                        imageUris.add(Uri.parse(path));
                        //imageUris.add(uri);

                    }

                } else if(data.getData() != null) {

                    Uri imagePath = data.getData();

                    Log.e(TAG, "Single Image URI = " + imagePath.toString());
                    String path = DataUtils.getFilePath(this, imagePath);
                    Log.d(TAG, path);

                    Log.d(TAG, "After conversion = " + path.toString());

                    //imageUris.add(Uri.parse(path));
                    Snackbar.make(mainActivityParentView, getString(R.string.error_one_image), Snackbar.LENGTH_LONG)
                            .show();
                    return;
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



    private void uploadImage(final List<Uri> imagePath) {

        Log.d(TAG, "Uploading to " + NetworkUtils.getUploadUri(this));

        final String fileName = imagePath.get(0).getLastPathSegment();

        final String uploadUuid = UUID.randomUUID().toString(); // Keep track of uploads

        Log.e(TAG, NetworkUtils.getUploadUri(this).toString());

        try {
            UploadNotificationConfig config = new UploadNotificationConfig();
            config.getCompleted().autoClear = true;

            MultipartUploadRequest req = new MultipartUploadRequest(this,uploadUuid,
                    NetworkUtils.getUploadUri(this).toString())
                    .setMethod("POST")
                    .setNotificationConfig(config)
                    .setMaxRetries(1);


            for (Uri uri : imagePath) {
                Log.d(TAG, "Adding file: " + uri.getPath());
                req.addFileToUpload(uri.getPath(), "files[]");
            }

            req.startUpload();

        } catch (FileNotFoundException | MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Unable to upload file");
            Toast.makeText(this, "Unable to upload file", Toast.LENGTH_LONG).show();
        }

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = DataUtils.createThumbnail(imagePath.get(0));
                byte[] bytes;
                if (bitmap != null) {
                    bytes = DataUtils.getBytesFromBitmap(bitmap);
                } else {
                    bytes = null;
                }

                ImageDetails details = new ImageDetails(fileName, ImageDetails.UPLOADING, uploadUuid, bytes);
                mDb.imageDetailsDao().insertImageDetails(details);
            }
        });
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
