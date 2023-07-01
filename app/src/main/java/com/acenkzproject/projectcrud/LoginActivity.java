package com.acenkzproject.projectcrud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.acenkzproject.projectcrud.databinding.ActivityLoginBinding;
import com.acenkzproject.projectcrud.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;

    private GoogleSignInClient googleSignInClient;

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Silahkan Tunggu");
        progressDialog.setCancelable(false);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });

        binding.btnLogin.setOnClickListener(v -> {
            if (!binding.email.getText().toString().equals("") && !binding.password.getText().toString().equals("")){
                Login(binding.email.getText().toString(), binding.password.getText().toString());
            }
            else {
                Toast.makeText(getApplicationContext(), "Silahkan Isi Data dengan lengkap", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnGoogle.setOnClickListener(v -> {
            GoogleSignIn();
        });

    }

    private void GoogleSignIn(){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("Google Sign In", "firebaseAuthWithGoogle : "+ account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
                
            }
            catch (ApiException e){
                Log.w("Google Sign In", "Google Sign In Failed "+e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        Log.d("Google Sign In", "SignInWithCredential:Success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                    }
                    else {
                        Log.w("Google Sign In", "SignInWithCredential:Failure", task.getException());
                    }
                    reload();
                });
    }

    private void Login(String email, String password){
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        if (task.getResult().getUser() != null){
                            reload();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Login Gagal", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Login Gagal", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
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
}