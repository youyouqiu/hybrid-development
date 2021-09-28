package com.zw.platform.util.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * http 工具类
 */
public class BaiduHttpUtil {

    public static String post(String requestUrl, String accessToken, String params)
            throws Exception {
        String contentType = "application/x-www-form-urlencoded";
        return BaiduHttpUtil.post(requestUrl, accessToken, contentType, params);
    }

    public static String post(String requestUrl, String accessToken, String contentType, String params)
            throws Exception {
        String encoding = "UTF-8";
        if (requestUrl.contains("nlp")) {
            encoding = "GBK";
        }
        return BaiduHttpUtil.post(requestUrl, accessToken, contentType, params, encoding);
    }

    public static String post(String requestUrl, String accessToken, String contentType, String params, String encoding)
            throws Exception {
        String url = requestUrl + "?access_token=" + accessToken;
        return BaiduHttpUtil.postGeneralUrl(url, contentType, params, encoding);
    }

    public static String postGeneralUrl(String generalUrl, String contentType, String params, String encoding)
            throws Exception {
        URL url = new URL(generalUrl);
        // 打开和URL之间的连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // 设置通用的请求属性
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // 得到请求的输出流对象
        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.write(params.getBytes(encoding));
            out.flush();
        }

        // 建立实际的连接
        try {
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            try (final InputStream is = connection.getInputStream();
                 BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding))) {
                String getLine;
                StringBuilder result = new StringBuilder();
                while ((getLine = in.readLine()) != null) {
                    result.append(getLine);
                }
                return result.toString();
            }
        } finally {
            connection.disconnect();
        }
    }
}
