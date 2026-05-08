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
    private final ExecutorService executorService;

    public EntryRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        entryDao = database.entryDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Entry>> getAllEntriesDesc() {
        return entryDao.getAllActiveEntriesDesc();
    }

    public LiveData<List<Entry>> getAllEntriesAsc() {
        return entryDao.getAllActiveEntriesAsc();
    }

    public LiveData<List<Entry>> searchEntries(String query) {
        return entryDao.searchEntries("%" + query + "%");
    }

    // Фильтр по настроению
    public LiveData<List<Entry>> getEntriesByMood(int mood) {
        return entryDao.getEntriesByMood(mood);
    }

    // Фильтр по конкретной дате
    public LiveData<List<Entry>> getEntriesByDate(long startOfDay, long endOfDay) {
        return entryDao.getEntriesByDate(startOfDay, endOfDay);
    }

    // Фильтр по диапазону дат
    public LiveData<List<Entry>> getEntriesByDateRange(long startDate, long endDate) {
        return entryDao.getEntriesByDateRange(startDate, endDate);
    }

    // Комбинированный фильтр: настроение + диапазон дат
    public LiveData<List<Entry>> getEntriesByMoodAndDateRange(int mood, long startDate, long endDate) {
        return entryDao.getEntriesByMoodAndDateRange(mood, startDate, endDate);
    }

    // Комбинированный фильтр: поиск + настроение
    public LiveData<List<Entry>> searchEntriesByMood(String query, int mood) {
        return entryDao.searchEntriesByMood("%" + query + "%", mood);
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
}