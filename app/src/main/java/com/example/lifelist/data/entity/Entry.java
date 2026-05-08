package com.example.lifelist.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.io.Serializable;

@Entity(tableName = "entries")
public class Entry implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    // Новое поле для настроения
    @ColumnInfo(name = "mood")
    private int mood; // 0: Ужасно, 1: Плохо, 2: Нормально, 3: Хорошо, 4: Прекрасно

    @ColumnInfo(name = "is_deleted")
    private boolean isDeleted;

    @ColumnInfo(name = "deleted_at")
    private Long deletedAt;

    public Entry(String title, String content, long timestamp, int mood) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.mood = mood;
        this.isDeleted = false;
    }

    // --- Геттеры и сеттеры ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getMood() { return mood; }
    public void setMood(int mood) { this.mood = mood; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public Long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Long deletedAt) { this.deletedAt = deletedAt; }
}