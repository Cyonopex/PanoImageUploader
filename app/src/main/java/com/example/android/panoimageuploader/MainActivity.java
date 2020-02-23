package com.example.android.panoimageuploader;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.panoimageuploader.database.ImageDetails;
import com.example.android.panoimageuploader.util.DataUtils;
import com.example.android.panoimageuploader.util.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements ImageDetailsAdapter.ImageDetailsAdapterOnClickHandler, ImageDetailTouchHelper.ImageDetailTouchHelperListener {

    private static final int PermissionRequest = 1;
    private static final int pickReqCode = 2;
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ImageDetailsAdapter mAdapter;
    private View mainActivityParentView;
    private ImageDetailsViewModel viewModel;
    private AppCompatActivity mainActivity = this;

    private Thread refresherThread;

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

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration div = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(div);
        mAdapter = new ImageDetailsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ImageDetailTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        setupViewModel();

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof ImageDetailsAdapter.DetailsViewHolder) {

            List<ImageDetails> imageDetails = mAdapter.getData();

            final int itemPosition = viewHolder.getAdapterPosition();
            final ImageDetails detailToBeDeleted = imageDetails.get(itemPosition);

            // cancel upload
            String uuid = detailToBeDeleted.getUploadUID();
            UploadService.stopUpload(uuid);

            viewModel.removeImageDetails(detailToBeDeleted);

            String snackBarString;
            if (detailToBeDeleted.getStatus() == ImageDetails.UPLOADING) {
                snackBarString = getString(R.string.upload_canceled);
            } else {
                snackBarString = getString(R.string.image_removed);
            }


            Snackbar snackbar = Snackbar.make(mainActivityParentView, snackBarString, Snackbar.LENGTH_LONG);

            if (detailToBeDeleted.getStatus() != ImageDetails.UPLOADING) {
                snackbar.setAction(getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.addImageDetail(detailToBeDeleted);
                    }
                });
            }

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresherThread = new Thread() {
            @Override
            public void run() {
                try {
                    while(!refresherThread.isInterrupted()) {
                        Thread.sleep(2000);
                        viewModel.getImageUpdatesFromServer();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Thread Interrupted");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        };

        // Refresher thread must be in OnResume/OnPause so that it won't run when activity is minimised
        refresherThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        refresherThread.interrupt();
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

                    //Multiple image data is sent to the activity in ClipData
                    int count = data.getClipData().getItemCount();
                    for (int i=0; i < count; i++) {
                        ClipData.Item item = data.getClipData().getItemAt(i);

                        Uri uri = item.getUri();
                        Log.d(TAG, "MultiImage URI = " + uri.toString());

                        String path;
                        try {
                            path = DataUtils.getFilePath(this, uri);
                        } catch (Exception e) {
                            Log.e(TAG, "Content provider URI given is unsupported");
                            Toast.makeText(this, getString(R.string.bad_contentprovider),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Log.d(TAG, path);

                        imageUris.add(Uri.parse(path));

                    }

                } else if(data.getData() != null) {

                    //Single image data is sent to activity in normal .getData method
                    Uri imagePath = data.getData();

                    Log.e(TAG, "Single Image URI = " + imagePath.toString());

                    String path;
                    try {
                        path = DataUtils.getFilePath(this, imagePath);
                    } catch (Exception e) {
                        Log.e(TAG, "Content provider URI given is unsupported");
                        Toast.makeText(this, getString(R.string.bad_contentprovider),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

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



    private void uploadImage(List<Uri> imagePath) {

        Log.d(TAG, "Uploading to " + NetworkUtils.getUploadUri(this));

        String uploadUuid = UUID.randomUUID().toString(); // Keep track of uploads

        String fileName = imagePath.get(0).getLastPathSegment();

        try {
            UploadNotificationConfig config = new UploadNotificationConfig();
            config.getCompleted().autoClear = true;

            MultipartUploadRequest req = new MultipartUploadRequest(this,uploadUuid,
                    NetworkUtils.getUploadUri(this).toString())
                    .setMethod("POST")
                    .setNotificationConfig(config)
                    .setMaxRetries(1)
                    .addParameter("fileName", fileName);


            for (Uri uri : imagePath) {
                Log.d(TAG, "Adding file: " + uri.getPath());
                req.addFileToUpload(uri.getPath(), "files[]");
            }

            req.startUpload();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "URL invalid");
            Toast.makeText(this, getString(R.string.bad_url), Toast.LENGTH_LONG).show();
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "File not found");
            Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_LONG).show();
            return;
        }

        viewModel.createNewImageDetail(imagePath.get(0), uploadUuid);
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
