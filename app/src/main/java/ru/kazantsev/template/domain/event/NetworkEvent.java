package ru.kazantsev.template.domain.event;

import okhttp3.Request;

/**
 * Created by 0shad on 26.02.2016.
 */
public class NetworkEvent<R> extends ResponseEvent<R> {

    public final Request request;

    public NetworkEvent(Status status, R response, Request request) {
        super(status, response);
        this.request = request;
    }
}
