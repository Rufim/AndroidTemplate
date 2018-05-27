package ru.kazantsev.template.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 0shad on 14.05.2017.
 */
public class ExceptionResponse implements Response {

    final IOException exception;

    public ExceptionResponse(IOException exception) {
        this.exception = exception;
    }

    public IOException getException() {
        return exception;
    }

    @Override
    public String getEncoding() {
        return "UTF-8";
    }

    @Override
    public void setEncoding(String encoding) {

    }

    @Override
    public String getMessage() {
        return exception.getMessage();
    }

    @Override
    public void setMessage(String message) {

    }

    @Override
    public int getCode() {
        return 404;
    }

    @Override
    public void setCode(int code) {

    }

    @Override
    public void setArched(boolean b) {

    }

    @Override
    public boolean isArched() {
        return false;
    }

    @Override
    public boolean isDownloadOver() {
        return true;
    }

    @Override
    public void setDownloadOver(boolean downloadOver) {

    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getRawContent(String encoding) throws IOException {
        return getRawContent();
    }

    @Override
    public String getRawContent() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        exception.fillInStackTrace().printStackTrace(writer);
        writer.close();
        return stream.toString();
    }

    @Override
    public void setHeaders(Map<String, List<String>> headerFields) {

    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return new HashMap<>();
    }
}
