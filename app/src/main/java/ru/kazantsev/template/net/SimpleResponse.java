package ru.kazantsev.template.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by 0shad on 06.05.2017.
 */
public class SimpleResponse implements Response{

    private boolean isDownloadOver = false;
    private boolean arched = false;
    private int code;
    private String encoding;
    private String message;
    private Map<String, List<String>> headers;

    private ByteArrayOutputStream byteArrayOutputStream;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public boolean isDownloadOver() {
        return isDownloadOver;
    }

    @Override
    public void setDownloadOver(boolean downloadOver) {
        isDownloadOver = downloadOver;
    }

    public boolean isArched() {
        return arched;
    }

    @Override
    public void setArched(boolean arched) {
        this.arched = arched;
    }

    @Override
    public long length() {
        if(byteArrayOutputStream == null) return 0;
        return byteArrayOutputStream.size();
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        if(byteArrayOutputStream == null) {
            byteArrayOutputStream = new ByteArrayOutputStream();
        }
        return byteArrayOutputStream;
    }

    @Override
    public synchronized InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    @Override
    public String getRawContent(String encoding) throws IOException {
        if(byteArrayOutputStream == null) return null;
        return byteArrayOutputStream.toString(encoding);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getRawContent() throws IOException {
        return getRawContent(getEncoding());
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
