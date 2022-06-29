package com.egrobots.grassanalysis2.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.egrobots.grassanalysis2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.user_name_edit_text)
    EditText userNameEditText;
    @BindView(R.id.email_edit_text)
    EditText emailEditText;
    @BindView(R.id.password_edit_text)
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.signInTextView)
    public void signIn() {
        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
    }

    @OnClick(R.id.sign_up_button)
    public void signUp() {
        String username = userNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "يجب ادخال البريد الالكتروني وكلمة السر", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(SignUpActivity.this, RequestsActivity.class));
                        String userId = task.getResult().getUser().getUid();
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("username", username);
                        userData.put("email", email);

                        usersRef.child(userId).updateChildren(userData);
                    } else {
                        System.out.println(task.getException());
                    }
                }
            });
        }
    }
}