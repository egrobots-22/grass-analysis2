package com.egrobots.grassanalysis2.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.egrobots.grassanalysis2.R;
import com.egrobots.grassanalysis2.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis2.models.Request;
import com.egrobots.grassanalysis2.utils.Constants;
import com.egrobots.grassanalysis2.utils.NotificationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.ui.PlayerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.questions_analysis_layout)
    View questionsAnalysisLayout;
    @BindView(R.id.user_question_layout)
    View userQuestionLayout;
    @BindView(R.id.analysis_question_text_view)
    TextView analysisQuestionTextView;
    @BindView(R.id.analysis_answer_edit_text)
    EditText analysisAnswerEditText;
    @BindView(R.id.next_question_button)
    Button nextQuestionButton;
    private int selectedImagePosition;
    private boolean isAdmin;
    private int currentQuestionIndex;
    private ExoPlayerVideoManager exoPlayerManager;
    private String requestId;
    private String requestUserId;
    private Request request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_view);
        ButterKnife.bind(this);

        requestId = getIntent().getStringExtra(Constants.REQUEST_ID);
        requestUserId = getIntent().getStringExtra(Constants.REQUEST_USER_ID);
        isAdmin = FirebaseAuth.getInstance().getCurrentUser().getUid().equals("kieMMnm6OpTglSgTYjy3bwY7Q4a2");
        if (isAdmin) {
            questionsAnalysisLayout.setVisibility(View.VISIBLE);
            setQuestions();
        } else {
            questionsAnalysisLayout.setVisibility(View.GONE);
        }

        //get request
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference(Constants.REQUESTS_NODE)
                .child(requestUserId)
                .child(requestId);
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                request = snapshot.getValue(Request.class);
                Glide.with(getApplicationContext())
                        .load(request.getImages().get(selectedImagePosition).getUrl())
                        .into(imageView);
                if (request.getAudioQuestion() != null) {
                    playerView.setVisibility(View.VISIBLE);
                    exoPlayerManager = new ExoPlayerVideoManager();
                    exoPlayerManager.initializeAudioExoPlayer(RequestViewActivity.this, request.getAudioQuestion(), true);
                    exoPlayerManager.initializePlayer(playerView);
                    playerView.showController();
                } else {
                    playerView.setVisibility(View.GONE);
                }
                if (request.getTextQuestion() != null && !request.getTextQuestion().isEmpty()) {
                    textQuestionTextView.setText(request.getTextQuestion());
                    textQuestionTextView.setVisibility(View.VISIBLE);
                } else {
                    textQuestionTextView.setVisibility(View.GONE);
                }

                if (request.getAnswer() != null && !isAdmin) {
                    questionsAnalysisLayout.setVisibility(View.VISIBLE);
                    analysisAnswerEditText.setEnabled(false);
                    setRequestAnswers();
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

    private List<String> questionsList;
    List<String> answersList = new ArrayList<>();

    private void setQuestions() {
        DatabaseReference questionRef = FirebaseDatabase.getInstance().getReference("analysis-questions");
        questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionsList = (List<String>) snapshot.getValue();
                analysisQuestionTextView.setText(questionsList.get(currentQuestionIndex));
                analysisAnswerEditText.setText("");
                analysisAnswerEditText.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    HashMap<String, String> requestAnswers;

    private void setRequestAnswers() {
        DatabaseReference requestAnswersRef = FirebaseDatabase.getInstance()
                .getReference(Constants.REQUESTS_NODE)
                .child(requestUserId)
                .child(requestId)
                .child("answer");

        requestAnswersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestAnswers = (HashMap<String, String>) snapshot.getValue();
                String question1 = (String) requestAnswers.keySet().toArray()[currentQuestionIndex];
                analysisQuestionTextView.setText(question1);
                analysisAnswerEditText.setText(requestAnswers.get(question1));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @OnClick(R.id.next_question_button)
    public void onNextQuestionClicked() {
        if (isAdmin) {
            String answer = analysisAnswerEditText.getText().toString();
            if (answer.isEmpty()) {
                Toast.makeText(this, "???? ???????? ?????? ????????????", Toast.LENGTH_SHORT).show();
            } else {
                answersList.add(answer);
                if (++currentQuestionIndex < questionsList.size()) {
                    analysisQuestionTextView.setText(questionsList.get(currentQuestionIndex));
                    analysisAnswerEditText.setText("");
                }
                if (answersList.size() == questionsList.size() - 1) {
                    nextQuestionButton.setText("??????????");
                } else if (answersList.size() == questionsList.size()) {
                    //send answer
                    sendAnswer();
                }
            }
        } else {
            if (requestAnswers != null) {
                if (++currentQuestionIndex < requestAnswers.size()) {
                    String question = (String) requestAnswers.keySet().toArray()[currentQuestionIndex];
                    analysisQuestionTextView.setText(question);
                    analysisAnswerEditText.setText(requestAnswers.get(question));
                }
                if (currentQuestionIndex == requestAnswers.size() - 1) {
                    nextQuestionButton.setText("??????????");
                    nextQuestionButton.setEnabled(false);
                }
            }
        }
    }

    public void sendAnswer() {
        DatabaseReference requestAnswersRef = FirebaseDatabase.getInstance()
                .getReference(Constants.REQUESTS_NODE)
                .child(requestUserId)
                .child(requestId);


        HashMap<String, Object> updates = new HashMap<>();
        HashMap<String, Object> answers = new HashMap<>();
        int i = 0;
        for (String question : questionsList) {
            answers.put(question, answersList.get(i));
            i++;
        }
        updates.put("answer", answers);
        updates.put("status", "answered");
        requestAnswersRef.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RequestViewActivity.this, "Send answer", Toast.LENGTH_SHORT).show();
                    //send notification to the request user
                    if (request != null) {
                        //get device token to the request user firstly
                        FirebaseDatabase.getInstance().getReference("users").child(requestUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        HashMap<String, Object> user = (HashMap<String, Object>) snapshot.getValue();
                                        String token = (String) user.get("token");
                                        NotificationRequest notificationRequest = new NotificationRequest(RequestViewActivity.this);
                                        notificationRequest.sendNotificationRequest(token);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                    finish();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayerManager != null) {
            exoPlayerManager.stopPlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayerManager != null) {
            exoPlayerManager.stopPlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayerManager != null) {
            exoPlayerManager.stopPlayer();
        }
    }
}