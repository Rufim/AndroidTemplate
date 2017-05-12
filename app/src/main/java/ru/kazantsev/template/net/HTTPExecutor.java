package ru.kazantsev.template.net;

import android.util.Log;
import ru.kazantsev.template.util.SystemUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Rufim on 07.07.2015.
 */
public class HTTPExecutor implements Callable<Response> {

    private static final String TAG = HTTPExecutor.class.getSimpleName();

    protected final Request request;
    protected Response response;
    protected int bufferSize = 1024 * 8;

    public HTTPExecutor(Request request) {
        this.request = request;
    }

    protected void configConnection(HttpURLConnection connection) {
        connection.setConnectTimeout(3000);
        connection.setUseCaches(false);
    }

    protected Response prepareResponse() throws IOException {
        return new SimpleResponse();
    }

    @Override
    public Response call() throws IOException {
        HttpURLConnection connection = null;
        response = null;
        do {
            try {
                connection = (HttpURLConnection) request.getUrl().openConnection();
                connection.setRequestMethod(request.getMethod().name());
            } catch (IOException ex) {
                Log.e(TAG, "Error on establish connection" + request.getUrl(), ex);
                continue;
            } finally {
                if (connection != null) connection.disconnect();
            }
            configConnection(connection);
            Map<String, String> headers = request.getHeaders();
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            GZIPOutputStream zos = null;
            InputStream is = null;
            OutputStreamWriter osw = null;
            RandomAccessFile raf = null;
            try {
                response = prepareResponse();
                if (response == null) {
                    throw new IOException("Error on prepare response");
                }
                if (request.isPutOrPost()) {
                    connection.setDoOutput(true);
                    osw = new OutputStreamWriter(connection.getOutputStream(), request.getEncoding());
                    osw.write(request.encodeParams());
                    osw.flush();
                    osw.close();
                }
                response.setCode(connection.getResponseCode());
                response.setMessage(connection.getResponseMessage());
                response.setHeaders(connection.getHeaderFields());
                if(response.getEncoding() == null) {
                    String encoding = getEncodingFromResponse(connection);
                    if(encoding != null) {
                        response.setEncoding(encoding);
                    } else {
                        response.setEncoding("UTF-8");
                    }
                }
                if (response.getCode() < 400) {
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }
                byte[] buffer = new byte[bufferSize];
                if (request.isArchiveResult()) {
                    zos = new GZIPOutputStream(new BufferedOutputStream(response.getOutputStream()));
                    SystemUtils.readStream(is, zos, buffer);
                    response.setArched(true);
                } else if (response instanceof CachedResponse) {
                    raf = new RandomAccessFile((File) response, "rw");
                    SystemUtils.readStream(is, raf, buffer);
                } else {
                    SystemUtils.readStream(is, response.getOutputStream(), buffer);
                }
                response.setDownloadOver(true);
            } catch (Exception ex) {
                Log.e(TAG, "Error on download html using url " + request.getUrl(), ex);
                continue;
            } finally {
                if (osw != null) osw.close();
                if (zos != null) zos.close();
                if (raf != null) raf.close();
                if (is != null) is.close();
                if (connection != null) connection.disconnect();
            }
        } while (request.canReconnect() && response == null);
        Log.i(TAG, "Request completed using url: " + request.getUrl() + (!request.isPutOrPost() ? " bytes received " + response.length() : ""));
        return response;
    }

    private String getEncodingFromResponse(HttpURLConnection connection) {
        String header = connection.getHeaderField("Content-Type");
        String charset = "charset";
        return parseParamFromHeader(header, charset);
    }

    public static String parseParamFromHeader(String header, String paramName) {
        if (header != null && header.contains(paramName)) {
            return header.substring(header.indexOf(paramName) + paramName.length() + 1).split(";")[0];
        }
        return null;
    }

    public Response getResponse() {
        return response;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Response execute(long minBytes) throws IOException, ExecutionException, InterruptedException {
        Future future = executeAsync();
        while (getResponse() == null || getResponse().length() < minBytes) {
            try {
                return (Response) future.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                //ignore and try again
            }
        }
        return getResponse();
    }

    public Response execute() throws IOException, ExecutionException, InterruptedException {
        return execute(Long.MAX_VALUE);
    }

    public Future<Response> executeAsync() throws IOException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        return service.submit(this);
    }
}
