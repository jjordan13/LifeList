package com.example.lifelist.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelist.R;
import com.example.lifelist.data.entity.Entry;
import com.example.lifelist.ui.editor.EditorActivity;
import com.example.lifelist.viewmodel.EntryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EntryViewModel entryViewModel;
    private RecyclerView recyclerView;
    private EntriesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewEntries);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddEntry);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntriesAdapter(entry -> {
            // Открытие редактора для редактирования
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            intent.putExtra("entry_id", entry.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        entryViewModel = new ViewModelProvider(this).get(EntryViewModel.class);

        entryViewModel.getAllEntries().observe(this, entries -> {
            adapter.setEntries(entries);
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            startActivity(intent);
        });
    }
}