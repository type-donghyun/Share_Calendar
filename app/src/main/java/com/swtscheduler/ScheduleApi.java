package com.swtscheduler;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ScheduleApi {
    @GET("/getSchedule.php")
    Call<List<LoadScheduleData>> getEvents();

    @POST("/postSchedule.php")
    Call<Void> insertEvent(@Body RequestBody data);
}
