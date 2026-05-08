package com.example.lifelist.data;

import java.io.Serializable;

public class FilterParams implements Serializable {
    private String searchQuery;
    private Integer moodFilter; // null = все настроения
    private Long startDate; // null = без ограничения
    private Long endDate; // null = без ограничения
    private boolean isAscending;

    public FilterParams() {
        this.searchQuery = "";
        this.moodFilter = null;
        this.startDate = null;
        this.endDate = null;
        this.isAscending = false;
    }

    // Геттеры и сеттеры
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public Integer getMoodFilter() { return moodFilter; }
    public void setMoodFilter(Integer moodFilter) { this.moodFilter = moodFilter; }

    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }

    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }

    public boolean isAscending() { return isAscending; }
    public void setAscending(boolean ascending) { isAscending = ascending; }

    // Метод для проверки, есть ли активные фильтры
    public boolean hasActiveFilters() {
        return (moodFilter != null) || (startDate != null) || (endDate != null) ||
                (searchQuery != null && !searchQuery.trim().isEmpty());
    }

    // Сброс всех фильтров
    public void reset() {
        searchQuery = "";
        moodFilter = null;
        startDate = null;
        endDate = null;
    }
}