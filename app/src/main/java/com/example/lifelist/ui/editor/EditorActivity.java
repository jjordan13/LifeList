package com.example.lifelist.ui.editor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.lifelist.R;
import com.example.lifelist.data.entity.Entry;
import com.example.lifelist.viewmodel.EntryViewModel;
import com.example.lifelist.util.SecurityManager;
import com.google.android.material.textfield.TextInputEditText;

public class EditorActivity extends AppCompatActivity {

    private EntryViewModel viewModel;
    private TextInputEditText etTitle, etContent;
    private Button btnSave, btnDelete;
    private RadioGroup rgMood;
    private Entry currentEntry;
    private boolean isEditMode = false;
    private SecurityManager securityManager;

    private String originalTitle = "";
    private String originalContent = "";
    private int originalMood = 2; // По умолчанию "Нормально"
    private boolean hasUnsavedChanges = false;
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editor);

        securityManager = SecurityManager.getInstance(this);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        rgMood = findViewById(R.id.rgMood);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        viewModel = new ViewModelProvider(this).get(EntryViewModel.class);

        if (securityManager.isGuest()) {
            etTitle.setEnabled(false);
            etContent.setEnabled(false);
            rgMood.setEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        int entryId = getIntent().getIntExtra("entry_id", -1);
        if (entryId != -1) {
            isEditMode = true;
            viewModel.getAllEntries().observe(this, entries -> {
                for (Entry entry : entries) {
                    if (entry.getId() == entryId) {
                        currentEntry = entry;
                        originalTitle = entry.getTitle() != null ? entry.getTitle() : "";
                        originalContent = entry.getContent() != null ? entry.getContent() : "";
                        originalMood = entry.getMood();

                        etTitle.setText(originalTitle);
                        etContent.setText(originalContent);

                        // Устанавливаем выбранное настроение
                        switch (originalMood) {
                            case 0: rgMood.check(R.id.rbMood0); break;
                            case 1: rgMood.check(R.id.rbMood1); break;
                            case 2: rgMood.check(R.id.rbMood2); break;
                            case 3: rgMood.check(R.id.rbMood3); break;
                            case 4: rgMood.check(R.id.rbMood4); break;
                        }

                        isInitialized = true;
                        break;
                    }
                }
            });
        } else {
            isInitialized = true;
            rgMood.check(R.id.rbMood2); // По умолчанию выбираем "Нормально"
        }

        if (!securityManager.isGuest()) {
            TextWatcher changeWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { if (isInitialized) checkForChanges(); }
            };
            etTitle.addTextChangedListener(changeWatcher);
            etContent.addTextChangedListener(changeWatcher);

            // Слушаем изменение настроения
            rgMood.setOnCheckedChangeListener((group, checkedId) -> {
                if(isInitialized) checkForChanges();
            });

            btnSave.setOnClickListener(v -> { if (performSave()) finish(); });

            btnDelete.setOnClickListener(v -> {
                if (currentEntry != null) {
                    new AlertDialog.Builder(this)
                            .setTitle("Удаление записи")
                            .setMessage("Вы уверены, что хотите удалить эту запись?")
                            .setPositiveButton("Удалить", (dialog, which) -> {
                                viewModel.delete(currentEntry);
                                Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                }
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (securityManager.isGuest() || !hasUnsavedChanges) {
                    finish();
                } else {
                    showUnsavedChangesDialog();
                }
            }
        });
    }

    private void checkForChanges() {
        String currentTitle = etTitle.getText() != null ? etTitle.getText().toString() : "";
        String currentContent = etContent.getText() != null ? etContent.getText().toString() : "";
        int currentMood = getCurrentSelectedMood();

        hasUnsavedChanges = !currentTitle.equals(originalTitle) ||
                !currentContent.equals(originalContent) ||
                currentMood != originalMood;
    }

    private int getCurrentSelectedMood() {
        int id = rgMood.getCheckedRadioButtonId();
        if (id == R.id.rbMood0) return 0;
        if (id == R.id.rbMood1) return 1;
        if (id == R.id.rbMood2) return 2;
        if (id == R.id.rbMood3) return 3;
        if (id == R.id.rbMood4) return 4;
        return 2; // Default
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Несохранённые изменения")
                .setMessage("Вы не сохранили запись. Что сделать?")
                .setPositiveButton("Сохранить", (dialog, which) -> { if (performSave()) finish(); })
                .setNegativeButton("Не сохранять", (dialog, which) -> finish())
                .setNeutralButton("Отмена", null)
                .show();
    }

    private boolean performSave() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";
        int mood = getCurrentSelectedMood();

        if (content.isEmpty()) {
            Toast.makeText(this, "Введите текст записи", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isEditMode && currentEntry != null) {
            currentEntry.setTitle(title);
            currentEntry.setContent(content);
            currentEntry.setMood(mood);
            viewModel.update(currentEntry);
        } else {
            Entry newEntry = new Entry(title, content, System.currentTimeMillis(), mood);
            viewModel.insert(newEntry);
        }

        Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show();
        hasUnsavedChanges = false;
        return true;
    }
}