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
     * ?????????URL??????POST???????????????
     * @param url     ???????????????URL
     * @param params  ????????????
     * @param charset ??????????????????
     * @return URL ????????????????????????????????????
     */
    public static String doPost(String url, Map<String, String> params, String charset) {
        RequestBuilder requestBuilder = RequestBuilder.post(url)
            .addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + charset)
            .setCharset(Charset.forName(charset)).setConfig(requestConfig);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        try {
            return client.execute(post, new StringResponseHandler(charset));
        } catch (Exception e) {
            logger.error("??????HTTP Post??????" + url + "????????????????????? ??????:" + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    /**
     * ??????post?????? ???????????????
     * @param url    ??????
     * @param params ??????
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
            logger.error("??????HTTP Post??????" + url + "????????????????????? ??????:" + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    /**
     * ?????????URL??????GET???????????????
     * @param url   ???????????????URL
     * @param params ????????????
     * @return URL ????????????????????????????????????
     */
    public static String sendGet(String url, Map<String, String> params) {
        RequestBuilder requestBuilder = RequestBuilder.get(url).setConfig(requestConfig);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        try {
            return client.execute(post, new StringResponseHandler());
        } catch (Exception e) {
            logger.error("??????HTTP Get??????" + url + "????????????????????? ??????:" + JSONObject.toJSONString(params), e);
        }
        return null;
    }

    /**
     * ??????HttpGet?????????????????????cookie
     * @param url      ??????
     * @param params   ??????
     * @param userName ?????????
     * @return
     */
    public static String sendGetAndUseSavedCookie(String url, Map<String, String> params, String userName) {
        RequestBuilder requestBuilder = RequestBuilder.get(url).setConfig(requestConfig);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        //???????????????cookie
        requestHeaderAddCookie(requestBuilder, userName);
        try {
            return client.execute(post, new StringResponseHandler());
        } catch (Exception e) {
            logger.error("??????HTTP Get??????" + url + "????????????????????? ??????:" + JSONObject.toJSONString(params), e);
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
            logger.error("??????HTTP Post??????" + url + "????????????????????? ??????:" + sql, e);
        }
        return null;
    }

    /**
     * ??????HttpPost?????????????????????cookie
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
            //???????????????cookie
            requestHeaderAddCookie(requestBuilder, userName);
            String response = client.execute(requestBuilder.build(), new StringResponseHandler());
            return JSONObject.parseObject(response);
        } catch (Exception e) {
            logger.error("??????HTTP Post??????" + url + "????????????????????? ??????:" + param, e);
        }
        return null;
    }

    /**
     * ???????????????cookie
     * @param requestBuilder RequestBuilder
     * @param userName       ????????????
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
     * ??????HttpPost???????????????cookie
     * @param url      ??????
     * @param param    ??????
     * @param userName ????????????
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
            logger.error("??????HTTP Post??????" + url + "????????????????????? ??????:" + param, e);
        }
        return null;
    }

    /**
     * ???????????????cookie
     * @param userName ?????????
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
            logger.error("??????HTTP Post??????" + url + "????????????????????? ??????:" + param, e);
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