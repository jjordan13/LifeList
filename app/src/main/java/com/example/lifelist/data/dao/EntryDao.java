package com.example.lifelist.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lifelist.data.entity.Entry;
import java.util.List;

@Dao
public interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Entry entry);

    @Update
    void update(Entry entry);

    @Delete
    void delete(Entry entry);

    // Все активные записи (новые сверху)
    @Query("SELECT * FROM entries WHERE is_deleted = 0 ORDER BY timestamp DESC")
    LiveData<List<Entry>> getAllActiveEntriesDesc();

    // Все активные записи (старые сверху)
    @Query("SELECT * FROM entries WHERE is_deleted = 0 ORDER BY timestamp ASC")
    LiveData<List<Entry>> getAllActiveEntriesAsc();

    // ПОЛНОТЕКСТОВЫЙ ПОИСК
    @Query("SELECT * FROM entries WHERE (title LIKE :query OR content LIKE :query) AND is_deleted = 0 ORDER BY timestamp DESC")
    LiveData<List<Entry>> searchEntries(String query);

    // ФИЛЬТР ПО НАСТРОЕНИЮ (MOOD)
    @Query("SELECT * FROM entries WHERE mood = :mood AND is_deleted = 0 ORDER BY timestamp DESC")
    LiveData<List<Entry>> getEntriesByMood(int mood);

    // ФИЛЬТР ПО КОНКРЕТНОЙ ДАТЕ (начало и конец дня в timestamp)
    @Query("SELECT * FROM entries WHERE timestamp >= :startOfDay AND timestamp < :endOfDay AND is_deleted = 0 ORDER BY timestamp DESC")
    LiveData<List<Entry>> getEntriesByDate(long startOfDay, long endOfDay);

    // ФИЛЬТР ПО ДИАПАЗОНУ ДАТ
    @Query("SELECT * FROM entries WHERE timestamp >= :startDate AND timestamp <= :endDate AND is_deleted = 0 ORDER BY timestamp DESC")
    LiveData<List<Entry>> getEntriesByDateRange(long startDate, long endDate);

    // КОМБИНИРОВАННЫЙ ФИЛЬТР: настроение + диапазон дат
    @Query("SELECT * FROM entries WHERE mood = :mood AND timestamp >= :startDate AND timestamp <= :endDate AND is_deleted = 0 ORDER BY timestamp DESC")
    LiveData<List<Entry>> getEntriesByMoodAndDateRange(int mood, long startDate, long endDate);

    // КОМБИНИРОВАННЫЙ ФИЛЬТР: поиск + настроение
    @Query("SELECT * FROM entries WHERE (title LIKE :query OR content LIKE :query) AND mood = :mood AND is_deleted = 0 ORDER BY timestamp DESC")
    LiveData<List<Entry>> searchEntriesByMood(String query, int mood);

    @Query("SELECT * FROM entries WHERE id = :id")
    Entry getEntryById(int id);

    @Query("UPDATE entries SET is_deleted = 1, deleted_at = :timestamp WHERE id = :entryId")
    void softDelete(int entryId, long timestamp);

    @Query("UPDATE entries SET is_deleted = 0, deleted_at = NULL WHERE id = :entryId")
    void restoreEntry(int entryId);

    @Query("SELECT * FROM entries WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    LiveData<List<Entry>> getDeletedEntries();
}