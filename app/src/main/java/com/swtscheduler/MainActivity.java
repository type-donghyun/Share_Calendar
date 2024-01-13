package com.swtscheduler;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, selectedDurationHours, selectedDurationMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 스케줄 입력 버튼
        Button openDateTimePickerButton = findViewById(R.id.openDateTimePickerButton);
        openDateTimePickerButton.setOnClickListener(view -> showDateTimePicker());

        // 새로고침 버튼
        ImageView dbSync = findViewById(R.id.dbSync);
        dbSync.setOnClickListener(view -> loadDataFromServer());
    }

    private void showDateTimePicker() {
        // 현재 날짜 초기화된 캘린더 객체 생성
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog 생성
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (datePicker, year1, month1, day1) -> {
                    selectedYear = year1;
                    selectedMonth = month1;
                    selectedDay = day1;
                    showTimePicker();
                },
                year, month, day);

        // DatePickerDialog 표시
        datePickerDialog.show();
    }

    private void showTimePicker() {
        // 현재 시간으로 초기화된 캘린더 객체 생성
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // TimePickerDialog 생성
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (timePicker, hourOfDay, minute1) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute1;
                    showDurationPicker();
                },
                hour, minute, false);

        // TimePickerDialog 표시
        timePickerDialog.show();
    }

    private void showDurationPicker() {
        // NumberPicker를 사용하여 시간과 분을 선택할 다이얼로그 생성
        LayoutInflater inflater = LayoutInflater.from(this);
        View durationPickerView = inflater.inflate(R.layout.dialog_duration_picker, null);

        NumberPicker hoursPicker = durationPickerView.findViewById(R.id.hoursPicker);
        NumberPicker minutesPicker = durationPickerView.findViewById(R.id.minutesPicker);

        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(23);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        hoursPicker.setValue(1);

        // 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("계획 시간 선택");
        builder.setView(durationPickerView);
        builder.setPositiveButton("확인", (dialog, which) -> {
            selectedDurationHours = hoursPicker.getValue();
            selectedDurationMinutes = minutesPicker.getValue();
            if (selectedDurationHours == 0 && selectedDurationMinutes == 0) {
                Toast.makeText(MainActivity.this, "최소 1분 이상 선택해주세요.", Toast.LENGTH_SHORT).show();
                showDurationPicker();
                return;
            }
            showTitlePicker();
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showTitlePicker() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View titlePickerView = inflater.inflate(R.layout.dialog_title_picker, null);
        EditText titleEditText = titlePickerView.findViewById(R.id.titleEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("일정 제목 입력");
        builder.setView(titlePickerView);
        builder.setPositiveButton("확인", (dialog, which) -> {
            String eventTitle = titleEditText.getText().toString();
            if (eventTitle.isEmpty()) {
                Toast.makeText(MainActivity.this, "일정 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                showTitlePicker();
                return;
            }
            sendDataToServer(eventTitle);
            dialog.dismiss();
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());
        builder.show();

        titleEditText.postDelayed(() -> {
            titleEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private void loadDataFromServer() {
        ScheduleApi apiService = ApiClient.getApiService();
        Call<List<LoadScheduleData>> call = apiService.getEvents();

        call.enqueue(new Callback<List<LoadScheduleData>>() {
            @Override
            public void onResponse(Call<List<LoadScheduleData>> call, Response<List<LoadScheduleData>> response) {
                EditText editText = findViewById(R.id.et);
                if (response.isSuccessful()) {
                    List<LoadScheduleData> data = response.body();
                    // TODO: JSON 데이터를 이용하여 원하는 작업 수행
                    editText.setText(data.toString());
                } else {
                    // 오류 처리
                    try {
                        String errorBody = response.errorBody().string();
                        editText.setText(errorBody);
                        Toast.makeText(MainActivity.this, "data load fail\n상태 코드: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<LoadScheduleData>> call, Throwable t) {
                if (t instanceof IOException) {
                    // 네트워크 문제로 실패한 경우
                    // 예: 연결이 끊겼거나, 서버에 연결할 수 없는 경우
                    Log.e("NetworkError", "Network failure: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                } else {
                    // 기타 오류
                    Log.e("Error", "Error: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "기타 오류", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getFormattedDateTime(int year, int month, int day, int hour, int minute) {
        return String.format("%04d-%02d-%02d %02d:%02d:00", year, month + 1, day, hour, minute);
    }

    private void sendDataToServer(String eventTitle) {

        String startTime = getFormattedDateTime(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
        String endTime = getFormattedDateTime(selectedYear, selectedMonth, selectedDay, selectedHour + selectedDurationHours, selectedMinute + selectedDurationMinutes);
        ScheduleApi apiService = ApiClient.getApiService();

        // 전송할 데이터 생성
        JSONObject json = new JSONObject();
        try {
            json.put("user", "donghyun");
            json.put("title", eventTitle);
            json.put("start_time", startTime);
            json.put("end_time", endTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Retrofit을 사용하여 서버에 데이터 전송
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), String.valueOf(json));
        Call<Void> call = apiService.insertEvent(requestBody);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    EditText editText = findViewById(R.id.et);
                    editText.setText(json.toString());
                    Toast.makeText(MainActivity.this, "데이터 전송 성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "데이터 전송 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}