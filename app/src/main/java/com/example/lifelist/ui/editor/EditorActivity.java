package com.example.lifelist.ui.editor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.lifelist.R;
import com.example.lifelist.data.entity.Entry;
import com.example.lifelist.viewmodel.EntryViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class EditorActivity extends AppCompatActivity {

    private EntryViewModel viewModel;
    private TextInputEditText etTitle, etContent;
    private Entry currentEntry;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editor);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        viewModel = new ViewModelProvider(this).get(EntryViewModel.class);

        // Проверяем, редактируем ли мы существующую запись
        int entryId = getIntent().getIntExtra("entry_id", -1);
        if (entryId != -1) {
            isEditMode = true;
            viewModel.getAllEntries().observe(this, entries -> {
                for (Entry entry : entries) {
                    if (entry.getId() == entryId) {
                        currentEntry = entry;
                        etTitle.setText(entry.getTitle());
                        etContent.setText(entry.getContent());
                        break;
                    }
                }
            });
        }

        btnSave.setOnClickListener(v -> saveEntry());

        btnDelete.setOnClickListener(v -> {
            if (currentEntry != null) {
                viewModel.delete(currentEntry);
                Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveEntry() {
        String title = etTitle.getText() != null ? etTitle.getText().toString() : "";
        String content = etContent.getText() != null ? etContent.getText().toString() : "";

        if (content.isEmpty()) {
            Toast.makeText(this, "Введите текст записи", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && currentEntry != null) {
            currentEntry.setTitle(title);
            currentEntry.setContent(content);
            viewModel.update(currentEntry);
        } else {
            Entry newEntry = new Entry(title, content, System.currentTimeMillis(), "neutral");
            viewModel.insert(newEntry);
        }

        Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show();
        finish();
    }
}