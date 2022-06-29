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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.egrobots.grassanalysis2.R;
import com.egrobots.grassanalysis2.models.Request;
import com.egrobots.grassanalysis2.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    @BindView(R.id.add_new_request_fab)
    FloatingActionButton addNewRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        ButterKnife.bind(this);

        RequestsAdapter requestsAdapter = new RequestsAdapter(this);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(RequestsActivity.this));
        requestRecyclerView.setAdapter(requestsAdapter);

        DatabaseReference requestsRef;

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals("kieMMnm6OpTglSgTYjy3bwY7Q4a2")) {
            addNewRequest.setVisibility(View.GONE);
            requestsRef = FirebaseDatabase.getInstance()
                    .getReference(Constants.REQUESTS_NODE);

            requestsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    requestList = new ArrayList<>();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        for (DataSnapshot requestSnapshot : userSnapshot.getChildren()) {
                            Request requestItem = requestSnapshot.getValue(Request.class);
                            requestItem.setId(requestSnapshot.getKey());
                            requestItem.setUserId(userSnapshot.getKey());
                            requestList.add(requestItem);
                        }
                    }
                    requestsAdapter.setItems(requestList);
                    requestsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            addNewRequest.setVisibility(View.VISIBLE);
            requestsRef = FirebaseDatabase.getInstance()
                    .getReference(Constants.REQUESTS_NODE)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            requestsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    requestList = new ArrayList<>();
                    for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                        Request requestItem = requestSnapshot.getValue(Request.class);
                        requestItem.setId(requestSnapshot.getKey());
                        requestItem.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
    }

    @OnClick(R.id.add_new_request_fab)
    public void onAddNewRequestClicked() {
        startActivity(new Intent(this, CaptureImagesActivity.class));
    }

    @Override
    public void onRequestClicked(Request request) {
        Intent intent = new Intent(this, RequestViewActivity.class);
        intent.putExtra(Constants.REQUEST_ID, request.getId());
        intent.putExtra(Constants.REQUEST_USER_ID, request.getUserId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.signout_action) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(this, SignInActivity.class));
        }
        return true;
    }
}