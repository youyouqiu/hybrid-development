package com.zw.platform.domain.connectionparamsset_809;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.zw.platform.util.ffmpeg.EncriptUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
public class T809ForwardVehicleInfo {

    @JSONField(name = "app_id")
    private String appId;

    private transient String appSecret;
    //签名
    private String sign;

    private long timestam;

    private JSONObject data;

    public String getSign() {
        StringBuilder sb = new StringBuilder();
        if (data != null && data.size() > 0) {
            Set<Map.Entry<String, Object>> entries = data.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                Object value = entry.getValue();
                if (StringUtils.isNotEmpty(value + "")) {
                    String key = entry.getKey();
                    sb.append(key).append("=").append(value).append("&");
                }
            }
            if (sb.length() < 1) {
                return null;
            }
            sb.deleteCharAt(sb.length() - 1);
            char[] chars = sb.toString().toCharArray();
            Arrays.sort(chars);
            // 直接追加 加密秘钥 追加 时间戳
            String toMd5 = new String(chars) + getAppSecret() + new Date().getTime() / 1000;
            //md5加密转大写
            return StringUtils.upperCase(EncriptUtils.md5(toMd5));
        }
        return null;
    }
}
