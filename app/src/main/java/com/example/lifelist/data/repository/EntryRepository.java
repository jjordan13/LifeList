package com.example.lifelist.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.lifelist.data.dao.EntryDao;
import com.example.lifelist.data.database.AppDatabase;
import com.example.lifelist.data.entity.Entry;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntryRepository {
    private final EntryDao entryDao;
    private final LiveData<List<Entry>> allEntries;
    private final ExecutorService executorService;

    public EntryRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        entryDao = database.entryDao();
        allEntries = entryDao.getAllActiveEntries();
        executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Entry>> getAllEntries() {
        return allEntries;
    }

    public void insert(Entry entry) {
        executorService.execute(() -> entryDao.insert(entry));
    }

    public void update(Entry entry) {
        executorService.execute(() -> entryDao.update(entry));
    }

    public void delete(Entry entry) {
        executorService.execute(() -> entryDao.delete(entry));
    }

    public void softDelete(int entryId, long timestamp) {
        executorService.execute(() -> entryDao.softDelete(entryId, timestamp));
    }

    public Entry getEntryById(int id) {
        return entryDao.getEntryById(id);
    }

    public LiveData<List<Entry>> getAllEntriesDesc() {
        return entryDao.getAllActiveEntriesDesc();
    }

    public LiveData<List<Entry>> getAllEntriesAsc() {
        return entryDao.getAllActiveEntriesAsc();
    }
}