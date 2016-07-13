package ru.kazantsev.template.domain.entity.json;

/**
 * Created by Dmitry on 26.04.2016.
 */

public class JsonError extends JsonEntity {
    private String action;
    private String error;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
