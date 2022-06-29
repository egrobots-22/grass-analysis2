package com.egrobots.grassanalysis2.presentation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;

import com.egrobots.grassanalysis2.R;
import com.egrobots.grassanalysis2.models.Request;
import com.egrobots.grassanalysis2.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity implements RequestsAdapter.OnRequestClickedCallback {

    private List<Request> requestList;

    @BindView(R.id.requests_recycler_view)
    RecyclerView requestRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        ButterKnife.bind(this);

        RequestsAdapter requestsAdapter = new RequestsAdapter(this);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(RequestsActivity.this));
        requestRecyclerView.setAdapter(requestsAdapter);

        DatabaseReference requestsRef = FirebaseDatabase.getInstance()
                .getReference(Constants.REQUESTS_NODE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList = new ArrayList<>();
                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    Request requestItem = requestSnapshot.getValue(Request.class);
                    requestItem.setId(requestSnapshot.getKey());
                    requestList.add(requestItem);
                }
                requestsAdapter.setItems(requestList);
                requestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @OnClick(R.id.add_new_request_fab)
    public void onAddNewRequestClicked() {
        startActivity(new Intent(this, CaptureImagesActivity.class));
    }

    @Override
    public void onRequestClicked(Request request) {
        Intent intent = new Intent(this, RequestViewActivity.class);
        intent.putExtra(Constants.REQUEST_ID, request.getId());
        startActivity(intent);
    }
}