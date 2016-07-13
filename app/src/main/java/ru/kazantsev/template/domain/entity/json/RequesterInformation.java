package ru.kazantsev.template.domain.entity.json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 0shad on 28.02.2016.
 */


public class RequesterInformation {
    private String id;
    private String fingerprint;
    private String remoteIP;
    private Map<String,String> receivedParams = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public Map<String, String> getReceivedParams() {
        return receivedParams;
    }

    public void setReceivedParams(Map<String, String> receivedParams) {
        this.receivedParams = receivedParams;
    }
}
