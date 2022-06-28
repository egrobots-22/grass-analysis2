package com.egrobots.grassanalysis2.datasource;

import android.net.Uri;

import com.egrobots.grassanalysis2.models.Image;
import com.egrobots.grassanalysis2.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

public class FirebaseDataSource {

    private final FirebaseCallback firebaseCallback;
    private final StorageReference storageReference;
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseUser currentUser;

    public FirebaseDataSource(FirebaseCallback firebaseCallback) {
        this.firebaseCallback = firebaseCallback;
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
    }

    public void uploadImageToFirebaseStorage(Uri imageUri) {
        StorageReference reference = storageReference.child(Constants.REQUESTS_REF + System.currentTimeMillis() + ".jpg");
        UploadTask uploadFileTask = reference.putFile(imageUri);
        uploadFileTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the fileTask to get the download URL
            reference.getDownloadUrl().addOnCompleteListener(task1 -> {
                String downloadUrl = task1.getResult().toString();
                firebaseCallback.onImageUploaded(downloadUrl);
            });
            return reference.getDownloadUrl();
        });
    }

    public void saveRequestToFirebaseDatabase(List<Image> uploadedImagesUris, String audioUrl, String questionText) {
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference(Constants.REQUESTS_NODE);
        HashMap<String, Object> requestData = new HashMap<>();
        requestData.put("images", uploadedImagesUris);
        requestData.put("status", "in progress");
        requestData.put("audioQuestion", audioUrl);
        requestData.put("textQuestion", questionText);
        requestsRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().updateChildren(requestData).addOnCompleteListener(task -> {
            firebaseCallback.onRequestSaved();
        });
    }

    public void uploadQuestionAudio(File audioRecordedFile) {

    }

    public void addNewRequest(List<Image> uploadedImagesUris, File audioRecordedFile, String questionText) {
        if (audioRecordedFile != null) {
            //upload audio firstly
            Uri audioFile = Uri.fromFile(audioRecordedFile);
            StorageReference audioReference = storageReference.child(Constants.REQUESTS_REF)
                    .child(Constants.AUDIO_ANSWERS + System.currentTimeMillis() + Constants.AUDIO_FILE_TYPE);

            audioReference.putFile(audioFile).addOnSuccessListener(success -> {
                Task<Uri> audioUrl = success.getStorage().getDownloadUrl();
                audioUrl.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String downloadUrl = task.getResult().toString();
                        saveRequestToFirebaseDatabase(uploadedImagesUris, downloadUrl, questionText);
                    }
                });
            });
        } else {
            saveRequestToFirebaseDatabase(uploadedImagesUris, null, questionText);
        }
    }

    public interface FirebaseCallback {
        void onImageUploaded(String downloadUrl);

        void onRequestSaved();
    }
}
