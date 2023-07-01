package com.acenkzproject.projectcrud;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.acenkzproject.projectcrud.databinding.ActivityEditBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;
    ActivityEditBinding binding;

    String id = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(EditActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Simpan Data");

        Intent intent = getIntent();
        if (intent != null){
            id = intent.getStringExtra("id");
            binding.nim.setText(intent.getStringExtra("nim"));
            binding.nama.setText(intent.getStringExtra("nama"));

            Glide.with(getApplicationContext()).load(intent.getStringExtra("avatar"))
                    .centerCrop()
                    .into(binding.avatar);
        }

        binding.btnSimpan.setOnClickListener(v -> {
            if (binding.nim.getText().toString().length() >0 && binding.nama.getText().toString().length() >0){
                upload(binding.nim.getText().toString(), binding.nama.getText().toString());
            }
            else {
                Toast.makeText(getApplicationContext(), "Data Belum Terisi Semua", Toast.LENGTH_LONG).show();
            }
        });

        binding.avatar.setOnClickListener(v -> {
            UploadImage();
        });

    }

    private void SaveData(String nim, String nama, String avatar) {
        Map<String, Object> user = new HashMap<>();
        user.put("nim", nim);
        user.put("nama", nama);
        user.put("avatar", avatar);

        progressDialog.show();
        if (id != null) {
            database.collection("mahasiswa")
                    .document(id)
                    .set(user)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Data Berhasil", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(EditActivity.this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Data Gagal", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();
                    });
        } else {
            database.collection("mahasiswa")
                    .add(user)
                    .addOnSuccessListener(documentReference -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Data Berhasil", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Data Gagal", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    });
        }
    }

    private void UploadImage(){
        final CharSequence[] items = {"Take Photo", "Choose From File", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(items, ((dialog, which) -> {
            if (items[which].equals("Take Photo")){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 10);
            }
            else if (items[which].equals("Choose From File")){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/");
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 20);
            } else if (items[which].equals("Cancel")) {
                dialog.dismiss();

            }
        }));
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Thread thread = new Thread(() ->{
                Bitmap bitmap = (Bitmap) extras.get("data");
                binding.avatar.post(()-> binding.avatar.setImageBitmap(bitmap));
            });
            thread.start();
        }
        if (requestCode == 20 && resultCode == RESULT_OK && data != null){
            final Uri path = data.getData();
            Thread thread = new Thread(()-> {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(path);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    binding.avatar.post(()-> binding.avatar.setImageBitmap(bitmap));
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    private void upload(String nim, String nama){
        progressDialog.show();
        binding.avatar.setDrawingCacheEnabled(true);
        binding.avatar.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) binding.avatar.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("images").child("Img "+new Date().getTime()+ ".jpeg");

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            if (taskSnapshot.getMetadata() != null) {
                if (taskSnapshot.getMetadata().getReference() != null) {
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(task -> {
                        if (task.getResult() != null) {
                            SaveData(nim, nama, task.getResult().toString());
                        } else {
                            Toast.makeText(getApplicationContext(), "Upload Gagal", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "Upload Gagal", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Upload Gagal", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }
}