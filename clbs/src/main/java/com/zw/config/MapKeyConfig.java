package com.zw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MapKeyConfig {
    private final Map<String, String> mapKeys;

    @Value("${map.key.baidu}")
    public void setBaiduKey(String baiduKey) {
        this.mapKeys.put("baidu", baiduKey);
    }

    @Value("${map.key.gaode}")
    public void setGaodeKey(String gaodeKey) {
        this.mapKeys.put("gaode", gaodeKey);
    }

    @Value("${map.key.google}")
    public void setGoogleKey(String googleKey) {
        this.mapKeys.put("google", googleKey);
    }

    @Value("${map.key.siwei}")
    public void setSiweiKey(String siweiKey) {
        this.mapKeys.put("siwei", siweiKey);
    }

    @Value("${map.key.tian}")
    public void setTianKey(String tianKey) {
        this.mapKeys.put("tian", tianKey);
    }

    public MapKeyConfig() {
        this.mapKeys = new HashMap<>(5);
    }

    public Map<String, String> getMapKeys() {
        return this.mapKeys;
    }

    public String getMapKey(String map) {
        return this.mapKeys.getOrDefault(map, "");
    }
}
