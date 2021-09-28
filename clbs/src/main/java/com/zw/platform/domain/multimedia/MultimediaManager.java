package com.zw.platform.domain.multimedia;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiaoYuecai on 2017/4/6.
 */
public class MultimediaManager {

    private static MultimediaManager manager;
    private Map<String, MultimediaData> multimediaMap;

    private MultimediaManager() {
        multimediaMap = new HashMap<String, MultimediaData>();
    }

    public static MultimediaManager getInstance() {
        if (manager == null) {
            manager = new MultimediaManager();
        }
        return manager;
    }

    public MultimediaData get(String id){
        return multimediaMap.get(id);
    }


    public void put(String id, MultimediaData multimedia) {
        multimediaMap.put(id, multimedia);
    }

    public void remove(String id) {
        multimediaMap.remove(id);
    }
}
