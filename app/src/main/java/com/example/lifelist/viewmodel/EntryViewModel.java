package com.example.lifelist.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lifelist.data.entity.Entry;
import com.example.lifelist.data.repository.EntryRepository;

import java.util.List;

public class EntryViewModel extends AndroidViewModel {
    private final EntryRepository repository;
    private final LiveData<List<Entry>> allEntries;
    private final MutableLiveData<Entry> currentEntry;

    public EntryViewModel(@NonNull Application application) {
        super(application);
        repository = new EntryRepository(application);
        allEntries = repository.getAllEntries();
        currentEntry = new MutableLiveData<>();
    }

    public LiveData<List<Entry>> getAllEntries() {
        return allEntries;
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