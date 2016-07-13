package ru.kazantsev.template.domain.entity.json;

/**
 * Created by 0shad on 28.02.2016.
 */


public class ServerInformation {
    private String serverName;
    private String apiVersion;
    private int requestDuration;
    private long currentTime;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public int getRequestDuration() {
        return requestDuration;
    }

    public void setRequestDuration(int requestDuration) {
        this.requestDuration = requestDuration;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
}
