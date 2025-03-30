package com.example.sqltp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sqltp.classes.Etudiant;
import com.example.sqltp.service.EtudiantService;

import java.util.List;

public class ListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        EtudiantService etudiantService = new EtudiantService(this);
        List<Etudiant> etudiants = etudiantService.findAll();

        adapter = new EtudiantAdapter(this, etudiants);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (adapter != null) {
            adapter.handleActivityResult(requestCode, resultCode, data);
        }
    }



}
