package ru.kazantsev.template.net;


import ru.kazantsev.template.util.SystemUtils;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Dmitry on 29.06.2015.
 */
public class CachedResponse extends File implements Serializable, Response {
    private final Request request;

    private boolean isDownloadOver = false;
    private boolean isCached = false;
    private boolean arched = false;
    private int code;
    private String message;
    private String encoding;
    private Map<String, List<String>> headers;

    private CachedResponse() {
        super("");
        request = null;
    }

    public CachedResponse(String path) {
        super(path);
        this.request = null;
    }

    public CachedResponse(File dir, String name, Request request) {
        super(dir, name);
        this.request = request;
    }

    public CachedResponse(String path, Request request) {
        super(path);
        this.request = request;
    }

    public CachedResponse(String dirPath, String name, Request request) {
        super(dirPath, name);
        this.request = request;
    }

    public CachedResponse(URI uri, Request request) {
        super(uri);
        this.request = request;
    }

    public boolean isArched() {
        return arched;
    }

    public void setArched(boolean arched) {
        this.arched = arched;
    }


    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

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

    public CachedResponse pack() throws IOException {
        byte[] buffer = new byte[4096];
        GZIPOutputStream zos = null;
        FileInputStream is = null;
        CachedResponse cachedResponse = new CachedResponse(getParentFile(), getName() + ".gzip", request.clone());
        try {
            zos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(cachedResponse, false)));
            is = new FileInputStream(this);
            while (SystemUtils.readStream(is, zos, buffer));
        } finally {
            if(zos != null) zos.close();
            if(is != null) is.close();;
        }
        return cachedResponse;
    }

    public CachedResponse unpack() throws IOException {
        byte[] buffer = new byte[4096];
        FileOutputStream os = null;
        InputStream zis = null;
        CachedResponse cachedResponse = new CachedResponse(getParentFile(), getName().replace(".gzip", ""), request.clone());
        try {
            os = new FileOutputStream(cachedResponse);
            zis = new GZIPInputStream(new FileInputStream(this));
            while (SystemUtils.readStream(zis, os, buffer)) ;
        } finally {
            if (os != null) os.close();
            if (zis != null) zis.close();
        }
        return cachedResponse;
    }

    public Request getRequest() {
        return request;
    }

    public boolean isDownloadOver() {
        return isDownloadOver;
    }

    public void setDownloadOver(boolean downloadOver) {
        isDownloadOver = downloadOver;
    }

    @Override
    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(this);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this);
    }

    @Override
    public String getRawContent(String encoding) throws IOException {
        return SystemUtils.readFile(this, encoding);
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

    public boolean isCached() {
        return isCached;
    }

    public void setCached(boolean cached) {
        isCached = cached;
    }
}
