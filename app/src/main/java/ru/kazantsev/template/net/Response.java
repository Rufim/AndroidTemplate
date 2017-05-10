package ru.kazantsev.template.net;

import java.io.*;

/**
 * Created by 0shad on 06.05.2017.
 */
public interface Response {

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

}
