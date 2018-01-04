package ru.kazantsev.template.net;

import android.support.v4.util.Pair;
import android.util.Log;
import ru.kazantsev.template.domain.Valuable;
import ru.kazantsev.template.util.TextUtils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Dmitry on 29.06.2015.
 */
public class Request implements Cloneable, Serializable {

    private static final String TAG = Request.class.getSimpleName();

    private transient URL url;
    private String serialiseUrl;
    private String suffix = "";
    private List<Pair<String, String>> params = new ArrayList<>();
    private boolean archiveResult = false;
    private boolean withParams = false;
    private String encoding = "UTF-8";
    private String content = "";
    private boolean followRedirect = HttpURLConnection.getFollowRedirects();
    private String ref;
    private Map<String, String> headers = new LinkedHashMap<>();
    private Map<String, String> cookies = new LinkedHashMap<>();
    private Method method = Method.GET;
    private int reconnectCount = 3;

    public enum Method {
        OPTIONS,
        GET,
        HEAD,
        POST,
        PUT,
        DELETE,
        TRACE;
    }

    protected Request() {
    }

    public Request(String url) throws MalformedURLException, UnsupportedEncodingException {
        this(new URL(url), false);
    }

    public Request(String url, boolean parseParams) throws MalformedURLException, UnsupportedEncodingException {
        this(new URL(url), parseParams);
    }

    public Request(URL url) throws UnsupportedEncodingException {
        this(url, false);
    }

    public Request(URL url, boolean parseParams) throws UnsupportedEncodingException {
        if (parseParams) {
            try {
                this.url = new URL(url.getProtocol() + "://" + url.getHost() + url.getPath());
                if (!TextUtils.isEmpty(url.getQuery())) {
                    withParams = true;
                    String query = url.getQuery();
                    String[] params = query.split("&");
                    for (String param : params) {
                        String paramValue[] = param.split("=");
                        if (paramValue.length == 2) {
                            addParam(paramValue[0], URLDecoder.decode(paramValue[1], encoding));
                        }
                    }
                }
                if (!TextUtils.isEmpty(url.getRef())) {
                    ref = url.getRef();
                }
            } catch (MalformedURLException e) {
            }
        } else {
            this.url = url;
        }
    }

    public Request addParam(String name, Object value) {
        withParams = true;
        Pair param = new Pair(name, value.toString());
        int index = -1;
        if ((index = getParamIndex(name)) != -1) {
            params.set(index, param);
        } else {
            params.add(param);
        }
        return this;
    }

    public int getReconnectCount() {
        return reconnectCount;
    }

    public void setReconnectCount(int reconnectCount) {
        this.reconnectCount = reconnectCount;
    }

    public boolean canReconnect() {
        reconnectCount -= 1;
        return reconnectCount > 0;
    }

    public Method getMethod() {
        return method;
    }

    public Request setMethod(Method method) {
        this.method = method;
        return this;
    }

    boolean isPutOrPost() {
        return method.equals(Method.POST) || method.equals(Method.PUT);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Request addParam(Enum name, Object value) {
        return addParam(name.name(), value);
    }

    public String getParam(String name) {
        for (Pair<String, String> param : params) {
            if(param.first.equals(name)) {
                return param.second;
            }
        }
        return null;
    }

    public int getParamIndex(String name) {
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).first.equals(name)) return i;
        }
        return -1;
    }

    public String getParam(Enum name) {
        return getParam(name.name());
    }

    public Request clearParams() {
        params.clear();
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public String generateCookieHeader() {
        if(cookies.size() > 0) {
            StringBuilder builder = new StringBuilder();
            Iterator<Map.Entry<String, String>> it = cookies.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> val = it.next();
                builder.append(val.getKey());
                builder.append("=");
                builder.append(val.getValue());
                if(it.hasNext()) builder.append("; ");
            }
            return builder.toString();
        } else {
            return "";
        }
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Request addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public Request addHeader(Enum name, String value) {
        addHeader(name.name(), value);
        return this;
    }

    public Request addCookie(String name, String value) {
        cookies.put(name, value);
        return this;
    }

    public Request addCookie(Enum name, String value) {
        addCookie(name.name(), value);
        return this;
    }

    public String getEncoding() {
        return encoding;
    }

    public Request setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public Request setSuffix(String suffix) {
        if (suffix != null) {
            this.suffix = suffix;
        } else {
            this.suffix = "";
        }
        return this;
    }

    public String getSuffix() {
        return suffix;
    }


    private String getReference() {
        return TextUtils.isEmpty(ref) ? "" : "#" + ref;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public boolean isWithParams() {
        return withParams;
    }

    public boolean isFollowRedirect() {
        return followRedirect;
    }

    public Request setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
        return this;
    }

    public boolean isArchiveResult() {
        return archiveResult;
    }

    public Request archResult(boolean archResult) {
        this.archiveResult = archResult;
        return this;
    }

    public URL getBaseUrl() {
        try {
            return new URL(url + getSuffix());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Wrong suffix " + getSuffix(), e);
            return url;
        }
    }

    public URL getUrl() throws UnsupportedEncodingException, MalformedURLException {
        if (withParams && !isPutOrPost()) {
            return new URL(url + getSuffix() + encodeParams() + getReference());
        } else if (!TextUtils.isEmpty(suffix) || !TextUtils.isEmpty(ref)) {
            return new URL(url + getSuffix() + getReference());
        } else {
            return url;
        }
    }

    public Request initParams(Enum[] values) {
        for (Enum value : values) {
            if (value instanceof Valuable) {
                addParam(value.name(), ((Valuable) value).value());
            } else {
                addParam(value.name(), "");
            }
        }
        return this;
    }

    public String encodeParams() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (Pair<String, String> param : params) {
            if (builder.length() != 0) {
                builder.append("&");
            } else if(!isPutOrPost()) {
                builder.append("?");
            }
            builder.append(URLEncoder.encode(param.first, encoding));
            builder.append("=");
            builder.append(URLEncoder.encode(param.second, encoding));
        }
        return builder.toString();
    }

    @Override
    public Request clone() {
        Request requestClone = null;
        requestClone = new Request();
        try {
            requestClone.url = new URL(url.toString());
        } catch (MalformedURLException e) {
        }
        requestClone.encoding = encoding;
        requestClone.suffix = suffix;
        requestClone.withParams = withParams;
        requestClone.params.addAll(params);
        requestClone.headers.putAll(headers);
        requestClone.cookies.putAll(cookies);
        requestClone.content = content;
        requestClone.method = method;
        return requestClone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;

        Request request = (Request) o;

        if (archiveResult != request.archiveResult) return false;
        if (withParams != request.withParams) return false;
        if (!url.equals(request.url)) return false;
        if (suffix != null ? !suffix.equals(request.suffix) : request.suffix != null) return false;
        if (!params.equals(request.params)) return false;
        if (encoding != null ? !encoding.equals(request.encoding) : request.encoding != null) return false;
        if (content != null ? !content.equals(request.content) : request.content != null) return false;
        if (!headers.equals(request.headers)) return false;
        return method == request.method;

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
        result = 31 * result + params.hashCode();
        result = 31 * result + (archiveResult ? 1 : 0);
        result = 31 * result + (withParams ? 1 : 0);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + headers.hashCode();
        result = 31 * result + method.hashCode();
        return result;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        serialiseUrl = url.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        url = new URL(serialiseUrl);
    }

    public Response execute() throws InterruptedException, ExecutionException, IOException {
        return new HTTPExecutor(this).execute();
    }
}
