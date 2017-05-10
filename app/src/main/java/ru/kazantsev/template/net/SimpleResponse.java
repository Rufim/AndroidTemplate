package ru.kazantsev.template.net;

import ru.kazantsev.template.net.Request;
import ru.kazantsev.template.net.Response;

import java.io.*;

/**
 * Created by 0shad on 06.05.2017.
 */
public class SimpleResponse implements Response{

    private boolean isDownloadOver = false;
    private boolean arched = false;
    private int code;
    private String encoding = "UTF-8";

    private ByteArrayOutputStream bufferedOutputStream;


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
        if(bufferedOutputStream == null) return 0;
        return bufferedOutputStream.size();
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        bufferedOutputStream = new ByteArrayOutputStream();
        return new BufferedOutputStream(bufferedOutputStream);
    }

    @Override
    public synchronized InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(bufferedOutputStream.toByteArray());
    }

    @Override
    public String getRawContent(String encoding) throws IOException {
        if(bufferedOutputStream == null) return null;
        return bufferedOutputStream.toString(encoding);
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
