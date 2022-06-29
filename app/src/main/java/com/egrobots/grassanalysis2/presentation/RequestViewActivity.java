package com.egrobots.grassanalysis2.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.egrobots.grassanalysis2.R;
import com.egrobots.grassanalysis2.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis2.models.Request;
import com.egrobots.grassanalysis2.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.ui.PlayerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestViewActivity extends AppCompatActivity {

    @BindView(R.id.multiple_images_view)
    View multipleImagesView;
    @BindView(R.id.image_view)
    ImageView imageView;
    @BindView(R.id.videoView)
    PlayerView playerView;
    @BindView(R.id.prevImageButton)
    ImageButton prevImageButton;
    @BindView(R.id.nextImageButton)
    ImageButton nextImageButton;
    @BindView(R.id.text_question_text_view)
    TextView textQuestionTextView;
    private int selectedImagePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_view);
        ButterKnife.bind(this);

        String requestId = getIntent().getStringExtra(Constants.REQUEST_ID);

        //get request
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference(Constants.REQUESTS_NODE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(requestId);
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Request request = snapshot.getValue(Request.class);
                Glide.with(getApplicationContext())
                        .load(request.getImages().get(selectedImagePosition).getUrl())
                        .into(imageView);
                if (request.getAudioQuestion() != null) {
                    ExoPlayerVideoManager exoPlayerManager = new ExoPlayerVideoManager();
                    exoPlayerManager.initializeAudioExoPlayer(RequestViewActivity.this, request.getAudioQuestion(), true);
                    exoPlayerManager.initializePlayer(playerView);
                }
                if (request.getTextQuestion() != null && !request.getTextQuestion().isEmpty()) {
                    textQuestionTextView.setText(request.getTextQuestion());
                }
                nextImageButton.setVisibility(View.VISIBLE);
                nextImageButton.setOnClickListener(v -> {
                    selectedImagePosition++;
                    Glide.with(getApplicationContext())
                            .load(request.getImages().get(selectedImagePosition).getUrl())
                            .into(imageView);
                    if (selectedImagePosition == 0) {
                        nextImageButton.setVisibility(View.VISIBLE);
                        prevImageButton.setVisibility(View.GONE);
                    } else if (selectedImagePosition == request.getImages().size() - 1) {
                        nextImageButton.setVisibility(View.GONE);
                        prevImageButton.setVisibility(View.VISIBLE);
                    } else {
                        nextImageButton.setVisibility(View.VISIBLE);
                        prevImageButton.setVisibility(View.VISIBLE);
                    }
                });

                prevImageButton.setOnClickListener(v -> {
                    selectedImagePosition--;
                    Glide.with(getApplicationContext())
                            .load(request.getImages().get(selectedImagePosition).getUrl())
                            .into(imageView);
                    if (selectedImagePosition == 0) {
                        nextImageButton.setVisibility(View.VISIBLE);
                        prevImageButton.setVisibility(View.GONE);
                    } else if (selectedImagePosition == request.getImages().size() - 1) {
                        nextImageButton.setVisibility(View.GONE);
                        prevImageButton.setVisibility(View.VISIBLE);
                    } else {
                        nextImageButton.setVisibility(View.VISIBLE);
                        prevImageButton.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}