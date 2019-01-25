package com.example.mayukh.firebasesocialmediaapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtUsername,edtEmail,edtPassword;
    private Button btnSignUp,btnSignIn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        mAuth = FirebaseAuth.getInstance();
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //Transition to next Activity
            transitionToSocialMediaActivity();
        }
    }
    private void signUp(){
        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Signing Up successful",Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.
                            getInstance().
                            getReference().
                            child("my_users").
                            child(task.getResult().
                                    getUser().
                                    getUid()).
                            child("username").
                            setValue(edtUsername.getText().toString());
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(edtUsername.getText().toString())
                            .build();
                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(MainActivity.this,"Display name updated successfully",Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                    transitionToSocialMediaActivity();

                }else{
                    Toast.makeText(MainActivity.this,"Signing up failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignUp:
                signUp();
                break;
            case R.id.btnSignIn:
                signIn();
                break;
        }
    }

    private void signIn(){
        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Signing In successful",Toast.LENGTH_SHORT).show();
                    transitionToSocialMediaActivity();

                }else{
                    Toast.makeText(MainActivity.this,"Signing In  failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void transitionToSocialMediaActivity(){
        startActivity(new Intent(MainActivity.this,SocialMediaActivity.class));
    }
}
