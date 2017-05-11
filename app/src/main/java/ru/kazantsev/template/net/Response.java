package ru.kazantsev.template.net;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created by 0shad on 06.05.2017.
 */
public interface Response {

    String getEncoding();

    void setEncoding(String encoding);

    String getMessage();

    void setMessage(String message);

    int getCode();

    void setCode(int code);

    void setArched(boolean b);

    boolean isArched();

    boolean isDownloadOver();

    void setDownloadOver(boolean downloadOver);

    long length();

    OutputStream getOutputStream() throws IOException;

    InputStream getInputStream() throws IOException;

    String getRawContent(String encoding) throws IOException;

    void setHeaders(Map<String, List<String>> headerFields);

    Map<String, List<String>> getHeaders();
}
