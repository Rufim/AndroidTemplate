package ru.kazantsev.template.domain.entity.json;

/**
 * Created by 0shad on 28.02.2016.
 */


public class JsonEntity {
    private ServerInformation serverInformation;
    private RequesterInformation requesterInformation;


    public ServerInformation getServerInformation() {
        return serverInformation;
    }

    public void setServerInformation(ServerInformation serverInformation) {
        this.serverInformation = serverInformation;
    }

    public RequesterInformation getRequesterInformation() {
        return requesterInformation;
    }

    public void setRequesterInformation(RequesterInformation requesterInformation) {
        this.requesterInformation = requesterInformation;
    }
}
