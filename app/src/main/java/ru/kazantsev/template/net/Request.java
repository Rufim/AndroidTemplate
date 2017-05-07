package ru.kazantsev.template.net;

import android.support.v4.util.Pair;
import android.util.Log;
import ru.kazantsev.template.domain.Valuable;
import ru.kazantsev.template.util.TextUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String ref;
    private Map<String, String> headers = new HashMap<>();
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
            this(new URL(url));
    }

    public Request(URL url) throws UnsupportedEncodingException {
        try {
            this.url = new URL(url.getProtocol() + "://" + url.getHost() + url.getPath());
        } catch (MalformedURLException e) {
        }
        if (!TextUtils.isEmpty(url.getQuery())) {
            withParams = true;
            String query = url.getQuery().substring(1);
            String[] params = query.split("&");
            for (String param : params) {
                String paramValue[] = param.split("=");
                if(paramValue.length == 2) {
                    addParam(paramValue[0], URLDecoder.decode(paramValue[1], encoding));
                }
            }
        }
        if(!TextUtils.isEmpty(url.getRef())) {

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

    public void setMethod(Method method) {
        this.method = method;
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
            param.first.equals(name);
            return param.second;
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

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Request addHeader(String name, String value) {
        headers.put(name, value);
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
        if (withParams) {
            return new URL(url + getSuffix() + encodeParams() + getReference());
        } else if(!TextUtils.isEmpty(suffix) || !TextUtils.isEmpty(ref)) {
            return new URL(url + getSuffix() + getReference());
        } else {
            return url;
        }
    }

    public Request initParams(Enum<? extends Valuable>[] values) {
        for (Enum<? extends Valuable> value : values) {
            addParam(value.name(), ((Valuable) value).value());
        }
        return this;
    }

    public String encodeParams() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (Pair<String, String> param : params) {
            if (builder.length() != 0) {
                builder.append("&");
            } else {
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
        requestClone.url = url;
        requestClone.encoding = encoding;
        requestClone.suffix = suffix;
        requestClone.withParams = withParams;
        requestClone.params.addAll(params);
        requestClone.headers.putAll(headers);
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
}
