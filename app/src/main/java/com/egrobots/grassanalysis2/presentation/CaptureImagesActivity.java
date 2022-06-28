package com.egrobots.grassanalysis2.presentation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.egrobots.grassanalysis2.R;
import com.egrobots.grassanalysis2.managers.AudioRecorder;
import com.egrobots.grassanalysis2.managers.CameraXRecorder;
import com.egrobots.grassanalysis2.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis2.managers.OpenGalleryActivityResultCallback;
import com.egrobots.grassanalysis2.models.Image;
import com.egrobots.grassanalysis2.utils.Constants;
import com.egrobots.grassanalysis2.utils.GuideDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.ui.PlayerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class CaptureImagesActivity extends AppCompatActivity implements GuideDialog.GuidelineCallback
        , CameraXRecorder.CameraXCallback, LocationListener {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA
            , Manifest.permission.RECORD_AUDIO
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE
            , ACCESS_FINE_LOCATION};

    private GuideDialog guideDialog = new GuideDialog(this);

    @BindView(R.id.viewFinder)
    PreviewView previewView;
    @BindView(R.id.videoView)
    PlayerView playerView;
    @BindView(R.id.capture_button)
    ImageButton captureButton;
    @BindView(R.id.recorded_time_tv)
    TextView recordedSecondsTV;
    @BindView(R.id.review_video_view)
    View reviewView;
    @BindView(R.id.multiple_images_view)
    View multipleImagesView;
    @BindView(R.id.image_switcher)
    ImageSwitcher imageSwitcher;
    @BindView(R.id.prevImageButton)
    ImageButton prevImageButton;
    @BindView(R.id.nextImageButton)
    ImageButton nextImageButton;
    @BindView(R.id.add_image_button)
    ImageButton addImageButton;
    @BindView(R.id.delete_image_button)
    ImageButton deleteImageButton;
    private Uri fileUri;
    private List<Uri> imagesUris = new ArrayList<>();
    private List<Image> uploadedImagesUris = new ArrayList<>();
    private ActivityResultLauncher openGalleryLauncher;
    private CameraXRecorder cameraXRecorder;
    private boolean isAddingNewImage = true;
    private boolean isAudioRecordingStarted;
    private int selectedImagePosition;
    private File audioRecordedFile;
    private AudioRecorder audioRecorder = new AudioRecorder();
    ;
    private Runnable updateEverySecRunnable;
    private int recordedSeconds;
    private Handler handler = new Handler();
    private ExoPlayerVideoManager exoPlayerManager;
    private int uploadedImageIndex;
    private ProgressDialog progressDialog;
    private LocationManager locationManager;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_images);
        ButterKnife.bind(this);
        guideDialog.show(getSupportFragmentManager(), null);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        registerForActivityResult();
        registerClickListenersForPrevNextButtons();
        initializeImageSwitcher();
    }

    private void initializeCameraX() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            cameraXRecorder = new CameraXRecorder(this, previewView, this);
            cameraXRecorder.setupCameraX();
        } else if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        boolean gpsEnabled = checkEnableGpsLocationAccess();
        if (!gpsEnabled) {
            //show dialog
            showGPSDisabledAlertToUser();
            //enable gps location before capturing images
//            Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(gpsIntent);
        }
    }

    private void initializeImageSwitcher() {
        imageSwitcher.setFactory(() -> {
            FrameLayout.LayoutParams layoutParams
                    = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            ImageView imgView = new ImageView(this);
            imgView.setLayoutParams(layoutParams);
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imgView;
        });
    }

    private void registerClickListenersForPrevNextButtons() {
        nextImageButton.setOnClickListener(v -> {
            ++selectedImagePosition;
            prevImageButton.setVisibility(View.VISIBLE);
            if (selectedImagePosition == imagesUris.size() - 1) {
                nextImageButton.setVisibility(View.GONE);
            }
            imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));
        });

        prevImageButton.setOnClickListener(v -> {
            --selectedImagePosition;
            nextImageButton.setVisibility(View.VISIBLE);
            if (selectedImagePosition == 0) {
                prevImageButton.setVisibility(View.GONE);
            }
            imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));
        });
    }

    @Override
    public void onStartNowClicked() {
        initializeCameraX();
    }

    @OnClick(R.id.capture_button)
    public void onCaptureButtonClicked() {
        if (isAddingNewImage) {
            //no image captured yet
            cameraXRecorder.captureImage();
        } else {
            if (!isAudioRecordingStarted) {
                //recording audio is not started yet, so start it
                onStartRecordingAudio();
            } else {
                onStopRecordingAudio();
            }
        }

    }

    private boolean checkEnableGpsLocationAccess() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onCaptureImage(Uri imageUri) {
        isAddingNewImage = false;
        previewView.setVisibility(View.GONE);
        multipleImagesView.setVisibility(View.VISIBLE);
        addImageButton.setVisibility(View.VISIBLE);
        imagesUris.add(imageUri);
        selectedImagePosition = imagesUris.size() - 1;
        imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));

        //if user capture images from 4 angles, change icon to record audio
//        if (imagesUris.size() == 4) {
        captureButton.setEnabled(true);
        captureButton.setImageDrawable(ContextCompat.getDrawable(CaptureImagesActivity.this, R.drawable.recording_audio));
//        }

        if (imagesUris.size() > 1) {
            deleteImageButton.setVisibility(View.VISIBLE);
        } else {
            deleteImageButton.setVisibility(View.GONE);
            prevImageButton.setVisibility(View.GONE);
            return;
        }

        if (selectedImagePosition == 0) {
            nextImageButton.setVisibility(View.VISIBLE);
            prevImageButton.setVisibility(View.GONE);
        } else if (selectedImagePosition == imagesUris.size() - 1) {
            nextImageButton.setVisibility(View.GONE);
            prevImageButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.done_button)
    public void onDoneClicked() {
        Toast.makeText(this, "images will be uploaded", Toast.LENGTH_SHORT).show();
        progressDialog.show();
        uploadImageToFirebaseStorage();
        //release exoplayer
        if (exoPlayerManager != null) {
            exoPlayerManager.releasePlayer();
        }
    }

    @OnClick(R.id.cancel_button)
    public void onCancelButton() {
        if (exoPlayerManager != null) {
            //release exoplayer
            exoPlayerManager.releasePlayer();
        }
        recordedSeconds = 0;
        recordedSecondsTV.setVisibility(View.GONE);
        //show camerax view
        previewView.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.GONE);
        multipleImagesView.setVisibility(View.GONE);
        //hide review view
        reviewView.setVisibility(View.GONE);
        //show record button
        captureButton.setVisibility(View.VISIBLE);
        deleteRecordedAudio();
        fileUri = null;

        //reinitialize camera
        initializeCameraX();
    }

    private void deleteRecordedAudio() {
        if (audioRecordedFile != null && audioRecordedFile.exists()) {
            audioRecordedFile.delete();
        }
    }

    @OnClick(R.id.add_image_button)
    public void onAddImageClicked() {
        initializeCameraX();
        fileUri = null; //to capture new image
        previewView.setVisibility(View.VISIBLE);
        multipleImagesView.setVisibility(View.GONE);
        captureButton.setEnabled(true);
        captureButton.setImageDrawable(ContextCompat.getDrawable(CaptureImagesActivity.this, R.drawable.start_record));
        isAddingNewImage = true;
    }

    @OnClick(R.id.delete_image_button)
    public void onDeleteImageClicked() {
        imagesUris.remove(selectedImagePosition);
        if (selectedImagePosition != 0) {
            --selectedImagePosition;
        }
        imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));
        if (imagesUris.size() > 1) {
            deleteImageButton.setVisibility(View.VISIBLE);
        } else if (imagesUris.size() == 1) {
            deleteImageButton.setVisibility(View.GONE);
            prevImageButton.setVisibility(View.GONE);
            nextImageButton.setVisibility(View.GONE);
            return;
        }
        if (selectedImagePosition == 0) {
            nextImageButton.setVisibility(View.VISIBLE);
            prevImageButton.setVisibility(View.GONE);
        } else if (selectedImagePosition == imagesUris.size() - 1) {
            nextImageButton.setVisibility(View.GONE);
            prevImageButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPreparingRecording() {

    }

    @Override
    public void onStartRecording() {

    }

    @Override
    public void onStopRecording(Uri videoUri) {

    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onStartRecordingAudio() {
        isAudioRecordingStarted = true;
        //set button as stop recording
        captureButton.setEnabled(true);
        captureButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stop_record));
        //start recording audio
        audioRecordedFile = new File(getFilesDir().getPath(), UUID.randomUUID().toString() + Constants.AUDIO_FILE_TYPE);
        try {
            audioRecorder.start(this, audioRecordedFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        recordedSecondsTV.setVisibility(View.VISIBLE);
        recordedSecondsTV.setText("00:00");
        updateEverySecRunnable = new Runnable() {
            @Override
            public void run() {
                ++recordedSeconds;
                String seconds = recordedSeconds < 10 ? "0" + recordedSeconds : recordedSeconds + "";
                recordedSecondsTV.setText(String.format("00:%s", seconds));
                //start recording question audio
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateEverySecRunnable, 1000);
    }

    @Override
    public void onStopRecordingAudio() {
        isAudioRecordingStarted = false;
        handler.removeCallbacks(updateEverySecRunnable);
        captureButton.setEnabled(true);
        captureButton.setImageDrawable(ContextCompat.getDrawable(CaptureImagesActivity.this, R.drawable.start_record));
        addImageButton.setVisibility(View.GONE);
        deleteImageButton.setVisibility(View.GONE);
        //stop recorded audio
        audioRecorder.stop();
        reviewView.setVisibility(View.VISIBLE);
        //hide record button
        captureButton.setVisibility(View.GONE);
        //show exoplayer audio
        previewView.setVisibility(View.GONE);
        playerView.setVisibility(View.GONE);
        //show image with audio
        exoPlayerManager = new ExoPlayerVideoManager();
        exoPlayerManager.initializeAudioExoPlayer(this, audioRecordedFile.getPath(), true);
        exoPlayerManager.initializePlayer(playerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(updateEverySecRunnable);
        }
        if (exoPlayerManager != null) {
            exoPlayerManager.releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacks(updateEverySecRunnable);
        }
        if (exoPlayerManager != null) {
            exoPlayerManager.releasePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacks(updateEverySecRunnable);
        }
        if (exoPlayerManager != null) {
            exoPlayerManager.releasePlayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraXRecorder = new CameraXRecorder(this, previewView, this);
                cameraXRecorder.setupCameraX();
            } else {
                Toast.makeText(this, "Permissions not granted by the user..", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && REQUIRED_PERMISSIONS != null) {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void registerForActivityResult() {
        openGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new OpenGalleryActivityResultCallback(new OpenGalleryActivityResultCallback.OpenGalleryCallback() {

                    @Override
                    public void onSingleImageOrVideoSelected(Uri fileUri) {

                    }

                    @Override
                    public void onMultipleImagesSelected(ArrayList<Uri> uris) {

                    }

                    @Override
                    public void onError(String error) {

                    }
                }));
    }

    private void uploadImageToFirebaseStorage() {
//        uploadedImageIndex = 0;
        if (uploadedImageIndex < imagesUris.size()) {
            Uri imageUri = imagesUris.get(uploadedImageIndex);
            final StorageReference reference = FirebaseStorage.getInstance().getReference()
                    .child(Constants.REQUESTS_REF + System.currentTimeMillis() + ".jpg");
            UploadTask uploadFileTask = reference.putFile(imageUri);
            uploadFileTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the fileTask to get the download URL
                reference.getDownloadUrl().addOnCompleteListener(task1 -> {
                    String downloadUrl = task1.getResult().toString();
                    Image img = new Image();
                    double[] latlong = getLatLongImage(imageUri);
                    img.setUrl(downloadUrl);
                    if (latlong != null) {
                        img.setLatitude(latlong[0]);
                        img.setLongitude(latlong[1]);
                    }
                    uploadedImagesUris.add(img);
                    uploadedImageIndex++;
                    uploadImageToFirebaseStorage();
                });
                return reference.getDownloadUrl();
            });
        } else {
            //save images info in the database
            Toast.makeText(this, "Finish uploading files", Toast.LENGTH_SHORT).show();
            DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference(Constants.REQUESTS_NODE);
            HashMap<String, Object> requestData = new HashMap<>();
            requestData.put("images", uploadedImagesUris);
            requestData.put("status", "in progress");
            requestsRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().updateChildren(requestData).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                Toast.makeText(CaptureImagesActivity.this, "Images are uploaded successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(
                        "GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });

        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @SuppressLint("MissingPermission")
    private double[] getLatLongImage(Uri imageUri) {
        double[] latlong = new double[2];
        Criteria criteria = new Criteria();
        LocationManager locationmanager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            if (locationManager != null) {
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (location != null) {
                    latlong[0] = location.getLatitude();
                    latlong[1] = location.getLongitude();
                }
            }
            return latlong;
        }
        return null;
//        InputStream in = null;
//        String[] latlong = new String[2];
//        try {
//            in = getContentResolver().openInputStream(imageUri);
//            ExifInterface exifInterface = new ExifInterface(in);
//            String lat = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
//            String lng = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
//            latlong[0] = lat;
//            latlong[1] = lng;
//            Toast.makeText(CaptureImagesActivity.this, "Latitude: " + lat + ", Longitude: " + lng, Toast.LENGTH_SHORT).show();
//            return latlong;
//        } catch (IOException e) {
//            // Handle any errors
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException ignored) {}
//            }
//            return latlong;
//        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {

    }

    @Override
    public void onFlushComplete(int requestCode) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}