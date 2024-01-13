package com.swtscheduler;

import com.google.gson.annotations.SerializedName;

public class LoadScheduleData {
    @SerializedName("id")
    private String id;

    @SerializedName("user")
    private String user;

    @SerializedName("title")
    private String title;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @Override
    public String toString() {
        return "{" + id + ", " + user + ", " + title + ", " + startTime + ", " + endTime + "}\n\n";
    }

    public LoadScheduleData(String id, String user, String title, String startTime, String endTime) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}