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

    @Query("SELECT * FROM entries WHERE is_deleted = 0 ORDER BY timestamp DESC")
    LiveData<List<Entry>> getAllActiveEntries();

    @Query("SELECT * FROM entries WHERE id = :id")
    Entry getEntryById(int id);

    @Query("SELECT * FROM entries WHERE content LIKE :query OR title LIKE :query AND is_deleted = 0")
    LiveData<List<Entry>> searchEntries(String query);

    @Query("UPDATE entries SET is_deleted = 1, deleted_at = :timestamp WHERE id = :entryId")
    void softDelete(int entryId, long timestamp);

    @Query("UPDATE entries SET is_deleted = 0, deleted_at = NULL WHERE id = :entryId")
    void restoreEntry(int entryId);

    @Query("SELECT * FROM entries WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    LiveData<List<Entry>> getDeletedEntries();
}