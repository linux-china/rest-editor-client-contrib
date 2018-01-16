package org.mvnsearch.intellij.plugins.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * http request call
 *
 * @author linux_china
 */
public class HttpCall {
    /**
     * action: GET, POST, PUT, DELETE
     */
    private String action;
    /**
     * http url
     */
    private String url;
    /**
     * params
     */
    private Map<String, String> params = new HashMap<>();
    /**
     * http headers
     */
    private Map<String, String> headers = new HashMap<>();
    /**
     * http payloads
     */
    private String payload;
    /**
     * comment, start with ### comment
     */
    private String comment;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void addParam(String name, String value) {
        if (value == null) return;
        params.put(name, value);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addJwtToken(String token) {
        headers.put("Authorization", "Bearer " + token);
    }

    public void addBasicAuth(String username, String password) {
        headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
    }

    public void setContentType(String contentType) {
        headers.put("Content-Type", contentType);
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        //comment
        builder.append("###" + StringUtils.defaultIfEmpty(comment, url));
        builder.append(SystemUtils.LINE_SEPARATOR);
        //action and url
        builder.append(action + " " + url);
        List<String> pairs = null;
        if (!params.isEmpty()) {
            pairs = params.entrySet().stream().map(entry -> entry.getKey() + "=" + encodeValue(entry.getValue())).collect(Collectors.toList());
        }
        //query for GET
        if (action.equalsIgnoreCase("GET") && pairs != null) {
            if (!url.contains("?")) {
                builder.append("?");
            } else {
                builder.append("&");
            }
            builder.append(String.join("&", pairs));
        }
        //http headers
        builder.append(SystemUtils.LINE_SEPARATOR);
        if (payload == null && !params.isEmpty() && !action.equalsIgnoreCase("GET")) {
            if (!headers.containsKey("Content-Type")) {
                headers.put("Content-Type", "application/x-www-form-urlencoded");
            }
        }
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.append(entry.getKey() + ": " + entry.getValue());
                builder.append(SystemUtils.LINE_SEPARATOR);
            }
        } else {
            builder.append(SystemUtils.LINE_SEPARATOR);
        }
        //payload
        if (action.equalsIgnoreCase("POST") || action.equalsIgnoreCase("PUT")) {
            if (payload != null && !payload.equalsIgnoreCase("null")) {
                builder.append("\n");
                builder.append(payload);
            } else if (pairs != null) {
                builder.append("\n");
                builder.append(String.join("&", pairs));
            }
        }
        builder.append(SystemUtils.LINE_SEPARATOR);
        return builder.toString();
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
}
