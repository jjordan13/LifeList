package com.example.lifelist.ui.editor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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
    private Entry currentEntry;
    private boolean isEditMode = false;
    private SecurityManager securityManager;

    private String originalTitle = "";
    private String originalContent = "";
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
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        viewModel = new ViewModelProvider(this).get(EntryViewModel.class);

        // Блокировка интерфейса в режиме Гостя
        if (securityManager.isGuest()) {
            etTitle.setEnabled(false);
            etContent.setEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            Toast.makeText(this, "Режим Гостя: запись доступна только для чтения", Toast.LENGTH_SHORT).show();
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
                        etTitle.setText(originalTitle);
                        etContent.setText(originalContent);
                        isInitialized = true;
                        break;
                    }
                }
            });
        } else {
            isInitialized = true;
        }

        if (!securityManager.isGuest()) {
            TextWatcher changeWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { if (isInitialized) checkForChanges(); }
            };
            etTitle.addTextChangedListener(changeWatcher);
            etContent.addTextChangedListener(changeWatcher);

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
        hasUnsavedChanges = !currentTitle.equals(originalTitle) || !currentContent.equals(originalContent);
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

        if (content.isEmpty()) {
            Toast.makeText(this, "Введите текст записи", Toast.LENGTH_SHORT).show();
            return false;
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
        hasUnsavedChanges = false;
        return true;
    }
}