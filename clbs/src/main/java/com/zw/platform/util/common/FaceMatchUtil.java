package com.zw.platform.util.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.util.Base64Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 @Author lijie
 @Date 2019/10/28 14:11
 @Description 百度人脸对比工具类
 @version 1.0
 **/
@Component
public class FaceMatchUtil {

    private static Logger log = LogManager.getLogger(FaceMatchUtil.class);

    @Value("${baidu.api.ak}")
    private String baiduAK;

    @Value("${baidu.api.sk}")
    private String baiduSK;

    private static String ak;

    private static String sk;

    @Autowired
    public void init() {
        ak = baiduAK;
        sk = baiduSK;
    }

    /**
     * 获取权限token
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    public static String getAuth() {
        // 官网获取的 API Key 更新为你注册的
        String clientId = ak;
        // 官网获取的 Secret Key 更新为你注册的
        String clientSecret = sk;
        return getAuth(clientId, clientSecret);
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public static String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
            // 1. grant_type为固定参数
            + "grant_type=client_credentials"
            // 2. 官网获取的 API Key
            + "&client_id=" + ak
            // 3. 官网获取的 Secret Key
            + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            try {
                connection.connect();
                // 获取所有响应头字段
                Map<String, List<String>> map = connection.getHeaderFields();
                // 定义 BufferedReader输入流来读取URL的响应
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }
                    JSONObject jsonObject = JSON.parseObject(result.toString());
                    return jsonObject.getString("access_token");
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            log.error("获取百度access_token异常！", e);
        }
        return null;
    }

    /**
     * 百度人脸对比方法
     */

    public static String match(byte[] bytes1, byte[] bytes2) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
        try {

            // byte[] bytes1 = FileUtil.readFileByBytes(path1);
            // byte[] bytes2 = FileUtil.readFileByBytes(path2);
            String image1 = Base64Util.encode(bytes1);
            String image2 = Base64Util.encode(bytes2);

            List<Map<String, Object>> images = new ArrayList<>();

            Map<String, Object> map1 = new HashMap<>();
            map1.put("image", image1);
            map1.put("image_type", "BASE64");
            map1.put("face_type", "LIVE");
            map1.put("quality_control", "NONE");
            map1.put("liveness_control", "NONE");

            Map<String, Object> map2 = new HashMap<>();
            map2.put("image", image2);
            map2.put("image_type", "BASE64");
            map2.put("face_type", "LIVE");
            map2.put("quality_control", "NONE");
            map2.put("liveness_control", "NONE");

            images.add(map1);
            images.add(map2);

            String param = JSON.toJSONString(images);

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = getAuth();

            return BaiduHttpUtil.post(url, accessToken, "application/json", param);
        } catch (Exception e) {
            log.error("百度人脸识别异常！", e);
        }
        return null;
    }


}
