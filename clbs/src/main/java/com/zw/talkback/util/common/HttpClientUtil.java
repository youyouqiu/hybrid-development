package com.zw.talkback.util.common;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class HttpClientUtil {

    private static Logger logger = LogManager.getLogger(HttpClientUtil.class);

    private static final String CHARSET_UTF8 = "UTF-8";
    private static CloseableHttpClient client;
    private static RequestConfig requestConfig;
    private static Map<String, CookieStore> userCookieStoreMap;

    static {
        userCookieStoreMap = new ConcurrentHashMap<>();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(1000);

        client = HttpClients.custom().setConnectionManager(connectionManager).build();
        requestConfig =
            RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(5000)
                .build();
    }

    private HttpClientUtil() {
    }

    public static String sendPost(String url, Map<String, String> params) {
        return doPost(url, params, CHARSET_UTF8);
    }

    /**
     * 向指定URL发送POST方法的请求
     * @param url     发送请求的URL
     * @param params  请求参数
     * @param charset 返回的字符集
     * @return URL 所代表远程资源的响应结果
     */
    public static String doPost(String url, Map<String, String> params, String charset) {
        RequestBuilder requestBuilder = RequestBuilder.post(url)
            .addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + charset)
            .setCharset(Charset.forName(charset)).setConfig(requestConfig);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        try {
            return client.execute(post, new StringResponseHandler(charset));
        } catch (Exception e) {
            logger.error("执行HTTP Post请求" + url + "时，发生异常！ 参数:" + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    /**
     * 发送post请求 不捕获异常
     * @param url    地址
     * @param params 参数
     * @return JSONObject
     */
    public static JSONObject sendPostRequest(String url, Map<String, String> params) {
        RequestBuilder requestBuilder = RequestBuilder.post(url)
            .addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + CHARSET_UTF8)
            .setCharset(Charset.forName(CHARSET_UTF8)).setConfig(requestConfig);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        try {
            return JSONObject.parseObject(client.execute(post, new StringResponseHandler(CHARSET_UTF8)));
        } catch (Exception e) {
            logger.error("执行HTTP Post请求" + url + "时，发生异常！ 参数:" + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    /**
     * 向指定URL发送GET方法的请求
     * @param url   发送请求的URL
     * @param params 请求参数
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, Map<String, String> params) {
        RequestBuilder requestBuilder = RequestBuilder.get(url).setConfig(requestConfig);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        try {
            return client.execute(post, new StringResponseHandler());
        } catch (Exception e) {
            logger.error("执行HTTP Get请求" + url + "时，发生异常！ 参数:" + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    /**
     * 发送HttpGet请求使用保存的cookie
     * @param url      地址
     * @param params   参数
     * @param userName 用户名
     * @return
     */
    public static String sendGetAndUseSavedCookie(String url, Map<String, String> params, String userName) {
        RequestBuilder requestBuilder = RequestBuilder.get(url).setConfig(requestConfig);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        //请求头添加cookie
        requestHeaderAddCookie(requestBuilder, userName);
        try {
            return client.execute(post, new StringResponseHandler());
        } catch (Exception e) {
            logger.error("执行HTTP Get请求" + url + "时，发生异常！ 参数:" + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    private static HttpUriRequest configRequestParam(Map<String, String> params, RequestBuilder requestBuilder) {
        if (params != null) {
            params.forEach((key, value) -> {
                if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
                    return;
                }
                requestBuilder.addParameter(key, value);
            });
        }
        return requestBuilder.build();
    }

    public static JSONObject doHttPost(String url, String sql) {
        HttpUriRequest post =
            RequestBuilder.post(url).setConfig(requestConfig).addHeader(HTTP.CONTENT_TYPE, "application/json")
                .setEntity(new StringEntity(sql, ContentType.APPLICATION_JSON)).build();
        try {
            String response = client.execute(post, new StringResponseHandler());
            return JSONObject.parseObject(response);
        } catch (Exception e) {
            logger.error("执行HTTP Post请求" + url + "时，发生异常！ 参数:" + sql, e);
        }
        return null;
    }

    /**
     * 发送HttpPost请求使用保存的cookie
     * @param url
     * @param param
     * @param userName
     * @return
     */
    public static JSONObject doHttPostAndUseSavedCookie(String url, String param, String userName) {
        try {
            RequestBuilder requestBuilder =
                    RequestBuilder.post(url).setConfig(requestConfig).addHeader(HTTP.CONTENT_TYPE, "application/json")
                    .setEntity(new StringEntity(param, ContentType.APPLICATION_JSON));
            //请求头添加cookie
            requestHeaderAddCookie(requestBuilder, userName);
            String response = client.execute(requestBuilder.build(), new StringResponseHandler());
            return JSONObject.parseObject(response);
        } catch (Exception e) {
            logger.error("执行HTTP Post请求" + url + "时，发生异常！ 参数:" + param, e);
        }
        return null;
    }

    /**
     * 请求头添加cookie
     * @param requestBuilder RequestBuilder
     * @param userName       用户名称
     */
    private static void requestHeaderAddCookie(RequestBuilder requestBuilder, String userName) {
        CookieStore cookieStore = userCookieStoreMap.get(userName);
        if (cookieStore != null) {
            List<Cookie> cookies = cookieStore.getCookies();
            String cookieStr = null;
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (StringUtils.isBlank(name) || !Objects.equals(name, "JSESSIONID")) {
                    continue;
                }
                String sessionId = cookie.getValue();
                if (StringUtils.isBlank(sessionId)) {
                    break;
                }
                cookieStr = "JSESSIONID=" + sessionId;
                break;
            }
            if (StringUtils.isNotBlank(cookieStr)) {
                requestBuilder.addHeader("Cookie", cookieStr);
            }
        }
    }

    /**
     * 发送HttpPost请求并保存cookie
     * @param url      地址
     * @param param    参数
     * @param userName 用户名称
     * @return JSONObject
     */
    public static JSONObject doHttPostAndSaveCookie(String url, String param, String userName) {
        try {
            HttpClientContext httpClientContext = HttpClientContext.create();
            httpClientContext.setCookieStore(new BasicCookieStore());
            HttpUriRequest post =
                RequestBuilder.post(url).setConfig(requestConfig).addHeader(HTTP.CONTENT_TYPE, "application/json")
                    .setEntity(new StringEntity(param, ContentType.APPLICATION_JSON)).build();
            String response = client.execute(post, new StringResponseHandler(), httpClientContext);
            userCookieStoreMap.put(userName, httpClientContext.getCookieStore());
            return JSONObject.parseObject(response);
        } catch (Exception e) {
            logger.error("执行HTTP Post请求" + url + "时，发生异常！ 参数:" + param, e);
        }
        return null;
    }

    /**
     * 移除保存的cookie
     * @param userName 用户名
     */
    public static void removeSavedCookie(String userName) {
        userCookieStoreMap.remove(userName);
    }

    public static String sendPost(String url, String param) {
        HttpUriRequest post =
            RequestBuilder.post(url).setConfig(requestConfig).addHeader(HTTP.CONTENT_TYPE, "application/json")
                .setEntity(new StringEntity(param, ContentType.APPLICATION_JSON)).build();
        try {
            return client.execute(post, new StringResponseHandler());
        } catch (Exception e) {
            logger.error("执行HTTP Post请求" + url + "时，发生异常！ 参数:" + param, e);
        }
        return null;
    }

    private static class StringResponseHandler implements ResponseHandler<String> {
        private final String charset;

        StringResponseHandler() {
            this(CHARSET_UTF8);
        }

        StringResponseHandler(String charset) {
            this.charset = charset;
        }

        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, charset) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }
    }
}