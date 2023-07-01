package com.acenkzproject.projectcrud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.Toast;

import com.acenkzproject.projectcrud.adapter.MhsAdapter;
import com.acenkzproject.projectcrud.databinding.ActivityMainBinding;
import com.acenkzproject.projectcrud.model.ModelMhs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    MhsAdapter mhsAdapter;
    List<ModelMhs> list = new ArrayList<>();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){
            binding.username.setText("Halo " + firebaseUser.getDisplayName());
        }
        else {
            binding.username.setText("Login gagal");
        }

        mhsAdapter = new MhsAdapter(getApplicationContext(), list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.ItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        binding.rvData.setLayoutManager(layoutManager);
        binding.rvData.addItemDecoration(decoration);
        binding.rvData.setAdapter(mhsAdapter);

        mhsAdapter.setOnItemClickCallback(pos -> {
            final CharSequence[] dialogitem = {"edit", "hapus"};
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setItems(dialogitem, (dialog1, which) -> {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                        intent.putExtra("id", list.get(pos).getId());
                        intent.putExtra("nim", list.get(pos).getNim());
                        intent.putExtra("nama", list.get(pos).getNama());
                        intent.putExtra("avatar", list.get(pos).getAvatar());
                        startActivity(intent);
                        break;
                    case 1:
                        DeleteData(list.get(pos).getId(), list.get(pos).getAvatar());
                        break;
                }
            });
            dialog.show();
        });

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Memuat Data");

        binding.btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), EditActivity.class));
        });

        binding.btnLoggout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GetData();
    }

    private void GetData() {
        progressDialog.show();

        database.collection("mahasiswa")
                .get()
                .addOnCompleteListener(task -> {
                    list.clear();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ModelMhs modelMhs = new ModelMhs(document.getString("nim"), document.getString("nama"), document.getString("avatar"));
                            modelMhs.setId(document.getId());
                            list.add(modelMhs);
                        }
                        mhsAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getApplicationContext(), "Data Gagal Diambil", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                });

    }
    private void DeleteData(String id, String avatar) {
        progressDialog.show();
        database.collection("mahasiswa").document(id)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show();
                        FirebaseStorage.getInstance().getReferenceFromUrl(avatar).delete()
                                .addOnCompleteListener(task1 -> {
                                    progressDialog.dismiss();
                                    GetData();
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Data Gagal Dihapus", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}