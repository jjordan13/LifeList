package com.example.lifelist.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.textfield.TextInputEditText;
import com.example.lifelist.data.FilterParams;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private EntryViewModel entryViewModel;
    private RecyclerView recyclerView;
    private EntriesAdapter adapter;
    private FloatingActionButton fabAdd, fabSort;
    private TextInputEditText etSearch;
    private ImageButton btnClearSearch, btnFilter; // Добавляем кнопку фильтра
    private SecurityManager securityManager;
    private FilterParams currentFilterParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        securityManager = SecurityManager.getInstance(this);
        recyclerView = findViewById(R.id.recyclerViewEntries);
        fabAdd = findViewById(R.id.fabAddEntry);
        fabSort = findViewById(R.id.fabSort);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnFilter = findViewById(R.id.btnFilter); // Инициализация

        currentFilterParams = new FilterParams();

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

        entryViewModel.getFilteredEntries().observe(this, entries -> {
            adapter.setEntries(entries);
        });

        // Логика поиска
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                entryViewModel.setSearchQuery(query);
                btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            entryViewModel.setSearchQuery("");
        });

        // Кнопка фильтра
        btnFilter.setOnClickListener(v -> {
            FilterDialogFragment dialog = FilterDialogFragment.newInstance(currentFilterParams);
            dialog.setOnFilterAppliedListener(params -> {
                currentFilterParams = params;
                entryViewModel.applyFilters(params);
                updateFilterButtonAppearance();
            });
            dialog.show(getSupportFragmentManager(), "FilterDialog");
        });

        fabSort.setOnClickListener(v -> entryViewModel.toggleSortOrder());

        fabAdd.setOnClickListener(v -> {
            if (securityManager.isGuest()) {
                Toast.makeText(this, "Режим Гостя: создание записей запрещено", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(MainActivity.this, EditorActivity.class));
        });

        if (securityManager.isGuest()) {
            fabAdd.setVisibility(View.GONE);
            fabSort.setEnabled(false);
            btnFilter.setEnabled(false);
            Toast.makeText(this, "Вы в режиме Гостя (только чтение)", Toast.LENGTH_LONG).show();
        }
    }

    private void updateFilterButtonAppearance() {
        if (currentFilterParams.hasActiveFilters()) {
            btnFilter.setColorFilter(ContextCompat.getColor(this, R.color.white));
        } else {
            btnFilter.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (securityManager.isGuest()) {
            fabAdd.setVisibility(View.GONE);
        } else {
            fabAdd.setVisibility(View.VISIBLE);
            fabSort.setEnabled(true);
            btnFilter.setEnabled(true);
        }
    }
}