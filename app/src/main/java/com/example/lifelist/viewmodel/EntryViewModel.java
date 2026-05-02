package com.example.lifelist.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.lifelist.data.entity.Entry;
import com.example.lifelist.data.repository.EntryRepository;

import java.util.List;

public class EntryViewModel extends AndroidViewModel {
    private final EntryRepository repository;
    private final LiveData<List<Entry>> allEntries;

    // Флаг сортировки: false = DESC (новые), true = ASC (старые)
    private final MutableLiveData<Boolean> isAscending = new MutableLiveData<>(false);

    private final MutableLiveData<Entry> currentEntry;

    public EntryViewModel(@NonNull Application application) {
        super(application);
        repository = new EntryRepository(application);
        currentEntry = new MutableLiveData<>();

        // Динамически переключаем источник данных в зависимости от флага isAscending
        allEntries = Transformations.switchMap(isAscending, ascending -> {
            if (ascending) {
                return repository.getAllEntriesAsc();
            } else {
                return repository.getAllEntriesDesc();
            }
        });
    }

    public LiveData<List<Entry>> getAllEntries() {
        return allEntries;
    }

    // Метод для переключения сортировки из Activity
    public void toggleSortOrder() {
        isAscending.setValue(!isAscending.getValue());
    }

    // Метод для получения текущего состояния сортировки (для обновления иконки)
    public LiveData<Boolean> getIsAscending() {
        return isAscending;
    }

    public void insert(Entry entry) {
        repository.insert(entry);
    }

    public void update(Entry entry) {
        repository.update(entry);
    }

    public void delete(Entry entry) {
        repository.delete(entry);
    }

    public void softDelete(int entryId) {
        repository.softDelete(entryId, System.currentTimeMillis());
    }

    public void setCurrentEntry(Entry entry) {
        currentEntry.setValue(entry);
    }

    public LiveData<Entry> getCurrentEntry() {
        return currentEntry;
    }
}