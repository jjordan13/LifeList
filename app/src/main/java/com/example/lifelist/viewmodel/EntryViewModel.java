package com.example.lifelist.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.lifelist.data.FilterParams;
import com.example.lifelist.data.entity.Entry;
import com.example.lifelist.data.repository.EntryRepository;

import java.util.List;

public class EntryViewModel extends AndroidViewModel {
    private final EntryRepository repository;
    private final LiveData<List<Entry>> filteredEntries;
    private final MutableLiveData<Entry> currentEntry;
    private final MutableLiveData<FilterParams> filterParams;

    public EntryViewModel(@NonNull Application application) {
        super(application);
        repository = new EntryRepository(application);
        currentEntry = new MutableLiveData<>();

        // Инициализация параметров фильтрации
        FilterParams initialParams = new FilterParams();
        filterParams = new MutableLiveData<>(initialParams);

        // Основной фильтр — реагирует на изменение параметров
        filteredEntries = Transformations.switchMap(filterParams, params -> {
            return getLiveDataForParams(params);
        });
    }

    // Определение, какой запрос выполнить в зависимости от параметров
    private LiveData<List<Entry>> getLiveDataForParams(FilterParams params) {
        boolean hasSearch = params.getSearchQuery() != null && !params.getSearchQuery().trim().isEmpty();
        boolean hasMood = params.getMoodFilter() != null;
        boolean hasDateRange = params.getStartDate() != null && params.getEndDate() != null;

        // Комбинации фильтров
        if (hasSearch && hasMood) {
            return repository.searchEntriesByMood(params.getSearchQuery(), params.getMoodFilter());
        } else if (hasMood && hasDateRange) {
            return repository.getEntriesByMoodAndDateRange(
                    params.getMoodFilter(),
                    params.getStartDate(),
                    params.getEndDate()
            );
        } else if (hasMood) {
            return repository.getEntriesByMood(params.getMoodFilter());
        } else if (hasDateRange) {
            return repository.getEntriesByDateRange(params.getStartDate(), params.getEndDate());
        } else if (hasSearch) {
            return repository.searchEntries(params.getSearchQuery());
        } else {
            // Без фильтров — обычная сортировка
            return params.isAscending() ?
                    repository.getAllEntriesAsc() :
                    repository.getAllEntriesDesc();
        }
    }

    public LiveData<List<Entry>> getFilteredEntries() {
        return filteredEntries;
    }

    // Обновление параметров фильтрации
    public void applyFilters(FilterParams params) {
        filterParams.setValue(params);
    }

    // Быстрые методы для изменения отдельных параметров
    public void setSearchQuery(String query) {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            current.setSearchQuery(query);
            filterParams.setValue(current);
        }
    }

    public void setMoodFilter(Integer mood) {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            current.setMoodFilter(mood);
            filterParams.setValue(current);
        }
    }

    public void setDateRange(Long start, Long end) {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            current.setStartDate(start);
            current.setEndDate(end);
            filterParams.setValue(current);
        }
    }

    public void toggleSortOrder() {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            current.setAscending(!current.isAscending());
            filterParams.setValue(current);
        }
    }

    public void clearFilters() {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            current.reset();
            filterParams.setValue(current);
        }
    }

    public LiveData<FilterParams> getFilterParams() {
        return filterParams;
    }

    // Для EditorActivity
    public LiveData<List<Entry>> getAllEntries() {
        FilterParams params = filterParams.getValue();
        boolean isAscending = (params != null) && params.isAscending();
        return isAscending ?
                repository.getAllEntriesAsc() :
                repository.getAllEntriesDesc();
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