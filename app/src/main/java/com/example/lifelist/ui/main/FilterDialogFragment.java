package com.example.lifelist.ui.main;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lifelist.R;
import com.example.lifelist.data.FilterParams;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FilterDialogFragment extends DialogFragment {

    private RadioGroup rgMoodFilter;
    private MaterialButton btnStartDate, btnEndDate, btnClear, btnApply;
    private TextView tvActiveFilters;

    private Calendar startCalendar;
    private Calendar endCalendar;

    private FilterParams currentParams;
    private OnFilterAppliedListener listener;

    public interface OnFilterAppliedListener {
        void onFilterApplied(FilterParams params);
    }

    public static FilterDialogFragment newInstance(FilterParams params) {
        FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("filter_params", params);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentParams = (FilterParams) getArguments().getSerializable("filter_params");
        }
        if (currentParams == null) {
            currentParams = new FilterParams();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_filter, container, false);

        initViews(view);
        setupListeners();
        loadCurrentFilters();

        return view;
    }

    private void initViews(View view) {
        rgMoodFilter = view.findViewById(R.id.rgMoodFilter);
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);
        btnClear = view.findViewById(R.id.btnClear);
        btnApply = view.findViewById(R.id.btnApply);
        tvActiveFilters = view.findViewById(R.id.tvActiveFilters);

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
    }

    private void setupListeners() {
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        btnClear.setOnClickListener(v -> {
            rgMoodFilter.clearCheck();
            btnStartDate.setText("Дата начала");
            btnEndDate.setText("Дата окончания");
            currentParams.setMoodFilter(null);
            currentParams.setStartDate(null);
            currentParams.setEndDate(null);
            updateActiveFiltersText();
        });

        btnApply.setOnClickListener(v -> {
            // Сохраняем выбранное настроение
            int selectedId = rgMoodFilter.getCheckedRadioButtonId();
            if (selectedId != -1) {
                if (selectedId == R.id.rbFilterMood0) currentParams.setMoodFilter(0);
                else if (selectedId == R.id.rbFilterMood1) currentParams.setMoodFilter(1);
                else if (selectedId == R.id.rbFilterMood2) currentParams.setMoodFilter(2);
                else if (selectedId == R.id.rbFilterMood3) currentParams.setMoodFilter(3);
                else if (selectedId == R.id.rbFilterMood4) currentParams.setMoodFilter(4);
            } else {
                currentParams.setMoodFilter(null);
            }

            // Применяем фильтры
            if (listener != null) {
                listener.onFilterApplied(currentParams);
            }
            dismiss();
        });
    }

    private void showDatePicker(boolean isStart) {
        Calendar calendar = isStart ? startCalendar : endCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (isStart) {
                        currentParams.setStartDate(calendar.getTimeInMillis());
                        btnStartDate.setText(formatDate(calendar));
                    } else {
                        currentParams.setEndDate(calendar.getTimeInMillis());
                        btnEndDate.setText(formatDate(calendar));
                    }
                    updateActiveFiltersText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private String formatDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void loadCurrentFilters() {
        // Загружаем текущее настроение
        Integer mood = currentParams.getMoodFilter();
        if (mood != null) {
            switch (mood) {
                case 0: rgMoodFilter.check(R.id.rbFilterMood0); break;
                case 1: rgMoodFilter.check(R.id.rbFilterMood1); break;
                case 2: rgMoodFilter.check(R.id.rbFilterMood2); break;
                case 3: rgMoodFilter.check(R.id.rbFilterMood3); break;
                case 4: rgMoodFilter.check(R.id.rbFilterMood4); break;
            }
        }

        // Загружаем даты
        if (currentParams.getStartDate() != null) {
            startCalendar.setTimeInMillis(currentParams.getStartDate());
            btnStartDate.setText(formatDate(startCalendar));
        }

        if (currentParams.getEndDate() != null) {
            endCalendar.setTimeInMillis(currentParams.getEndDate());
            btnEndDate.setText(formatDate(endCalendar));
        }

        updateActiveFiltersText();
    }

    private void updateActiveFiltersText() {
        int count = 0;
        if (currentParams.getMoodFilter() != null) count++;
        if (currentParams.getStartDate() != null) count++;
        if (currentParams.getEndDate() != null) count++;

        if (count == 0) {
            tvActiveFilters.setText("Фильтры не применены");
        } else {
            tvActiveFilters.setText("Активных фильтров: " + count);
        }
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }
}