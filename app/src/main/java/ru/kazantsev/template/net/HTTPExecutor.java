package ru.kazantsev.template.net;

import android.util.Log;
import ru.kazantsev.template.util.SystemUtils;
import ru.kazantsev.template.util.TextUtils;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Rufim on 07.07.2015.
 */
public class HTTPExecutor implements Callable<Response> {

    private static final String TAG = HTTPExecutor.class.getSimpleName();

    protected Request request;
    protected Response response;
    protected int bufferSize = 1024 * 8;

    public HTTPExecutor(Request request) {
        this.request = request;
    }

    public HTTPExecutor(String url) throws MalformedURLException, UnsupportedEncodingException {
        this.request = new Request(url);
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
        IOException exception;
        do {
            exception = null;
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
            Map<String, String> headers = new LinkedHashMap<>(request.getHeaders());
            if(request.getCookies() != null && request.getCookies().size() > 0) {
                if(headers.containsKey(Header.COOKIE) && TextUtils.notEmpty(headers.get(Header.COOKIE))) {
                   request.getCookies().putAll(parseParamsFromHeader(headers.get(Header.COOKIE)));
                }
                headers.put(Header.COOKIE, request.generateCookieHeader());
            }
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
                    request.setReconnectCount(0);
                    throw new IOException("Error on prepare response");
                }
                if (request.isPutOrPost()) {
                    connection.setDoOutput(true);
                    osw = new OutputStreamWriter(connection.getOutputStream(), request.getEncoding());
                    osw.write(request.encodeParams());
                    osw.flush();
                    osw.close();
                }

                int status = connection.getResponseCode();
                if(request.isFollowRedirect() && isStatusRedirect(status)) {
                    // get redirect url from "location" header field
                    String newUrl = connection.getHeaderField("Location");
                    // get the cookie if need, for login
                    String newCookies = connection.getHeaderField(Header.SET_COOKIE);
                    Request newRequest = new Request(newUrl);
                    newRequest.getHeaders().putAll(request.getHeaders());
                    newRequest.getCookies().putAll(request.getCookies());
                    newRequest.setFollowRedirect(true);
                    if(newCookies != null) {
                        newRequest.getCookies().putAll(parseParamsFromHeader(newCookies));
                    }
                    request = newRequest;
                    return call();
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
                break;
            } catch (IOException ex) {
                exception = ex;
                Log.e(TAG, "Error on download html using url " + request.getUrl(), ex);
                SystemUtils.sleepQuietly(300);
                continue;
            } finally {
                if (osw != null) osw.close();
                if (zos != null) zos.close();
                if (raf != null) raf.close();
                if (is != null) is.close();
                if (connection != null) connection.disconnect();
            }
        } while (request.canReconnect());
        if(response == null) {
            exception = new IOException("Cant obtain response");
        }
        if(exception != null) return new ExceptionResponse(exception);
        Log.i(TAG, "Request completed using url: " + request.getUrl() + (!request.isPutOrPost() ? " bytes received " + response.length() : ""));
        return response;
    }

    private boolean isStatusRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_SEE_OTHER;
    }

    private String getEncodingFromResponse(HttpURLConnection connection) {
        String header = connection.getHeaderField("Content-Type");
        String charset = "charset";
        return parseParamFromHeader(header, charset);
    }

    public static String parseParamFromHeader(String header, Enum paramName) {
        return parseParamFromHeader(header, paramName.name());
    }

    public static String parseParamFromHeader(String header, String paramName) {
        if (header != null && header.contains(paramName)) {
            return header.substring(header.indexOf(paramName) + paramName.length() + 1).split(";")[0];
        }
        return null;
    }

    public static Map<String, String> parseParamsFromHeader(String header) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        String[] keyValues = header.split("; ");
        if(keyValues != null && keyValues.length > 0) {
            for (String keyValue : keyValues) {
                int split = keyValue.indexOf("=");
                if(split > 0 && keyValue.length() > split + 1) {
                    params.put(keyValue.substring(0, split), keyValue.substring(split + 1));
                } else {
                    params.put(keyValue, "");
                }
            }
        }
        return params;
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
        if(getResponse() == null || (getResponse().length() < minBytes && !getResponse().isDownloadOver())) {
            while (getResponse() == null || getResponse().length() < minBytes){
                try {
                    future.get(300, TimeUnit.MILLISECONDS);
                    break;
                } catch (TimeoutException e) {
                    //ignore and try again
                }
            }
            if (getResponse() instanceof ExceptionResponse) {
                throw ((ExceptionResponse) response).getException();
            }
            if (getResponse() == null) {
                throw new IOException("Cannot obtain response");
            } else {
                return response;
            }
        } else {
            return getResponse();
        }
    }

    public Response execute() throws IOException, ExecutionException, InterruptedException {
        return execute(Long.MAX_VALUE);
    }

    public Future<Response> executeAsync() throws IOException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        return service.submit(this);
    }


    public static boolean pingHost(String host, int port, int timeout) {
        Socket socket = null;
        try  {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        } finally {
            if(socket != null) {
                try {
                    socket.close();
                } catch (Exception ex){
                    //ignore;
                }
            }
        }
    }

    /**
     * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
     * the 200-399 range.
     * @param url The HTTP URL to be pinged.
     * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
     * the total timeout is effectively two times the given timeout.
     * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout, otherwise <code>false</code>.
     */
    public static int pingURL(String url, int timeout) {
        url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setRequestProperty("Accept-Encoding", "");
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode();
        } catch (IOException exception) {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }
}
