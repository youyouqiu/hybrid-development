package com.zw.platform.commons;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.util.alarm.PassCloudAlarmUrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class HttpClientUtil {

    private static final Logger logger = LogManager.getLogger(HttpClientUtil.class);

    private static final String CHARSET_UTF8 = "UTF-8";
    private static final CloseableHttpClient client;
    private static final RequestConfig requestConfig;

    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(1000);

        client = HttpClients.custom().setConnectionManager(connectionManager).build();
        requestConfig =
            RequestConfig.custom().setSocketTimeout(150000).setConnectTimeout(10000).setConnectionRequestTimeout(10000)
                .build();
    }

    private HttpClientUtil() {
    }

    public static String sendPost(String url, Map<String, String> params) {
        return doPost(url, params);
    }

    /**
     * ??????Content-Type???application/json???POST??????
     */
    public static String sendPostByJson(String url, String jsonParamsStr) {
        RequestBuilder requestBuilder = RequestBuilder.post(url)
            .addHeader(HTTP.CONTENT_TYPE, "application/json; charset=" + CHARSET_UTF8)
            .setConfig(requestConfig).setCharset(StandardCharsets.UTF_8);
        HttpUriRequest post = configRequestJsonParam(jsonParamsStr, requestBuilder);
        try {
            return client.execute(post, new StringResponseHandler(CHARSET_UTF8));
        } catch (Exception e) {
            logger.error("??????HTTP Post??????" + url + "?????????????????????", e);
        }
        return null;
    }

    /**
     * ??????put??????
     * @param url    ??????
     * @param params ??????
     */
    public static String sendPut(String url, Map<String, String> params) {
        RequestBuilder requestBuilder = RequestBuilder.put(url)
            .addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + HttpClientUtil.CHARSET_UTF8)
            .setConfig(requestConfig).setCharset(StandardCharsets.UTF_8);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        try {
            return client.execute(post, new StringResponseHandler(HttpClientUtil.CHARSET_UTF8));
        } catch (Exception e) {
            logger.error("??????HTTP Put??????" + url + "?????????????????????", e);
        }
        return null;
    }

    public static void doPut(String url, Map<String, String> params) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.put(url)
            .addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + HttpClientUtil.CHARSET_UTF8)
            .setConfig(requestConfig).setCharset(StandardCharsets.UTF_8);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        try {
            String queryResult = client.execute(post, new StringResponseHandler(HttpClientUtil.CHARSET_UTF8));
            JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
            if (queryResultJsonObj == null || !Objects
                .equals(queryResultJsonObj.getInteger(PassCloudAlarmUrlUtil.RETURN_RESULT_CODE_KEY),
                    PassCloudAlarmUrlUtil.SUCCESS_CODE)) {
                String errorMsg =
                    "??????PassCloud:" + url + "???????????????" + (queryResultJsonObj != null
                        ? queryResultJsonObj.getString("message") : null);
                throw new Exception(errorMsg);
            }
        } catch (Exception e) {
            logger.error("??????HTTP Put??????" + url + "?????????????????????", e);
            throw e;
        }
    }

    /**
     * ?????????URL??????POST???????????????
     * @param params ????????????
     * @param url    ???????????????URL
     * @return URL ????????????????????????????????????
     */
    public static String doPost(String url, Map<String, String> params) {
        RequestBuilder requestBuilder = RequestBuilder.post(url)
            .addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + HttpClientUtil.CHARSET_UTF8)
            .setConfig(requestConfig).setCharset(StandardCharsets.UTF_8);
        HttpUriRequest post = configRequestParam(params, requestBuilder);
        try {
            return client.execute(post, new StringResponseHandler(HttpClientUtil.CHARSET_UTF8));
        } catch (Exception e) {
            logger.error("??????HTTP Post??????" + url + "?????????????????????", e);
        }
        return null;
    }

    /**
     * ?????????URL??????GET???????????????
     * @param url    ???????????????URL
     * @param params ????????????
     * @return URL ????????????????????????????????????
     */
    public static String sendGet(String url, Map<String, String> params) {
        RequestBuilder requestBuilder = RequestBuilder.get(url).setConfig(requestConfig);
        HttpUriRequest get = configRequestParam(params, requestBuilder);
        try {
            return client.execute(get, new StringResponseHandler());
        } catch (Exception e) {
            logger.error("??????HTTP Get??????{}?????????????????????", url, e);
        }
        return null;
    }

    public static String doGet(String url, Map<String, String> params) throws IOException {
        RequestBuilder requestBuilder = RequestBuilder.get(url).setConfig(requestConfig);
        HttpUriRequest request = configRequestParam(params, requestBuilder);
        return client.execute(request, new StringResponseHandler());
    }

    /**
     * ??????Content-Type???application/json?????????
     */
    private static HttpUriRequest configRequestJsonParam(String jsonParamsStr, RequestBuilder requestBuilder) {
        if (StringUtils.isNotBlank(jsonParamsStr)) {
            requestBuilder.setEntity(new StringEntity(jsonParamsStr, CHARSET_UTF8));
        }
        return requestBuilder.build();
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

    /**
     * ??????????????????????????????
     */
    public static JSONArray doHttPostBatch(String url, String param) {
        HttpUriRequest post =
            RequestBuilder.post(url).setConfig(requestConfig).addHeader(HTTP.CONTENT_TYPE, "application/json")
                .setEntity(new StringEntity(param, ContentType.APPLICATION_JSON)).build();
        try {
            String response = client.execute(post, new StringResponseHandler());
            return JSONObject.parseArray(response);
        } catch (Exception e) {
            logger.error("??????HTTP Post??????" + url + "????????????????????? ??????:" + param, e);
        }
        return null;
    }

    public static JSONObject doHttPost(String url, String param) {
        HttpUriRequest post =
            RequestBuilder.post(url).setConfig(requestConfig).addHeader(HTTP.CONTENT_TYPE, "application/json")
                .setEntity(new StringEntity(param, ContentType.APPLICATION_JSON)).build();
        try {
            String response = client.execute(post, new StringResponseHandler());
            return JSONObject.parseObject(response);
        } catch (Exception e) {
            logger.error("httpPost????????????, url: {}, sql: {}", url, param, e);
        }
        return null;
    }

    public static <T> T doHttPost(String url, String param, Class<T> cls) {
        HttpUriRequest post =
            RequestBuilder.post(url).setConfig(requestConfig).addHeader(HTTP.CONTENT_TYPE, "application/json")
                .setEntity(new StringEntity(param, ContentType.APPLICATION_JSON)).build();
        try {
            String response = client.execute(post, new StringResponseHandler());
            if (response == null) {
                return null;
            }
            return JSONObject.parseObject(response, cls);
        } catch (Exception e) {
            logger.error("httpPost????????????, url: {}, param: {}", url, param, e);
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

    /**
     * ????????????api, ????????????GET??????
     * @param convert urlConvert
     * @param params  params
     * @return ??????????????????
     */
    public static String send(UrlConvert convert, Map<String, String> params) {
        final HttpMethod httpMethod = convert.getHttpMethod();
        if (HttpMethod.POST.equals(httpMethod)) {
            return sendPost(convert.getUrl(), params);
        } else if (HttpMethod.PUT.equals(httpMethod)) {
            return sendPut(convert.getUrl(), params);
        } else {
            return sendGet(convert.getUrl(), params);
        }
    }

    public static String doWsdlHttPost(String url, String wsdlInfo) {
        HttpUriRequest post = RequestBuilder.post(url).setConfig(requestConfig)
            .addHeader(HTTP.CONTENT_TYPE, "text/xml; charset=" + CHARSET_UTF8)
            .setEntity(new StringEntity(wsdlInfo, ContentType.TEXT_XML)).build();
        try {
            return client.execute(post, new StringResponseHandler());
        } catch (Exception e) {
            logger.error("httpPost????????????, url: {}, wdslinfo: {}", url, wsdlInfo, e);
        }
        return null;
    }
}
