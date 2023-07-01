package com.acenkzproject.projectcrud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.acenkzproject.projectcrud.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Silahkan Tunggu");
        progressDialog.setCancelable(false);

        binding.btnLogin.setOnClickListener(v -> {
            finish();
        });

        binding.btnRegister.setOnClickListener(v -> {
            Register(binding.namaLengkap.getText().toString(), binding.email.getText().toString(), binding.password.getText().toString());

        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }

    private void reload(){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    private void Register(String nama, String email, String password){
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser != null) {
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(nama)
                                .build();
                        firebaseUser.updateProfile(request).addOnCompleteListener(task1 -> {
                            reload();
                        });
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Register Gagal", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }
}