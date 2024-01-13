package com.swtscheduler;

import com.google.gson.annotations.SerializedName;

public class SendScheduleData {
    @SerializedName("user")
    private String user;

    @SerializedName("title")
    private String title;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    public SendScheduleData(String user, String title, String startTime, String endTime) {
        this.user = user;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
