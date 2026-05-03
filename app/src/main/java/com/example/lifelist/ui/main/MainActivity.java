package com.example.lifelist.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifelist.R;
import com.example.lifelist.data.entity.Entry;
import com.example.lifelist.ui.auth.AuthActivity;
import com.example.lifelist.ui.editor.EditorActivity;
import com.example.lifelist.viewmodel.EntryViewModel;
import com.example.lifelist.util.SecurityManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EntryViewModel entryViewModel;
    private RecyclerView recyclerView;
    private EntriesAdapter adapter;
    private FloatingActionButton fabAdd, fabSort;
    private SecurityManager securityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        securityManager = SecurityManager.getInstance(this);
        recyclerView = findViewById(R.id.recyclerViewEntries);
        fabAdd = findViewById(R.id.fabAddEntry);
        fabSort = findViewById(R.id.fabSort);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntriesAdapter(entry -> {
            if (securityManager.isGuest()) {
                Toast.makeText(this, "Режим Гостя: редактирование запрещено", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            intent.putExtra("entry_id", entry.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        entryViewModel = new ViewModelProvider(this).get(EntryViewModel.class);
        entryViewModel.getAllEntries().observe(this, entries -> adapter.setEntries(entries));

        fabSort.setOnClickListener(v -> entryViewModel.toggleSortOrder());

        fabAdd.setOnClickListener(v -> {
            if (securityManager.isGuest()) {
                Toast.makeText(this, "Режим Гостя: создание записей запрещено", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(MainActivity.this, EditorActivity.class));
        });

        // Если пользователь вышел в режиме Гостя, показываем предупреждение
        if (securityManager.isGuest()) {
            fabAdd.setVisibility(View.GONE);
            fabSort.setEnabled(false);
            Toast.makeText(this, "Вы в режиме Гостя (только чтение)", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем видимость FAB при возврате на экран
        if (securityManager.isGuest()) {
            fabAdd.setVisibility(View.GONE);
        } else {
            fabAdd.setVisibility(View.VISIBLE);
            fabSort.setEnabled(true);
        }
    }
}