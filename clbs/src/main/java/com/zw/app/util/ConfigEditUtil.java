package com.zw.app.util;

import com.zw.platform.util.common.ConcurrentHashSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ConfigEditUtil {

    private static Set<String> configEditId = new ConcurrentHashSet<>();

    private static Set<String> configEditBrand = new ConcurrentHashSet<>();

    private static Set<String> configEditSim = new ConcurrentHashSet<>();

    private static Set<String> configEditDevice = new ConcurrentHashSet<>();

    /**
     * id
     * @param id configId
     * @return boolean
     */
    public static boolean putEditId(String id) {
        return configEditId.add(id);
    }

    public static void removeEditId(String id) {
        configEditId.remove(id);
    }

    public static void removeEditIds(List<String> ids) {
        configEditId.removeAll(ids);
    }

    public static Set<String> getConfigEditIds() {
        return configEditId;
    }

    /**
     * brand
     * @param brand 车牌
     * @return boolean
     */
    public static boolean putEditBrand(String brand) {
        return configEditBrand.add(brand);
    }

    public static void removeEditBrand(String brand) {
        configEditBrand.remove(brand);
    }

    /**
     * sim
     * @param sim sim
     * @return boolean
     */
    public static boolean putEditSim(String sim) {
        return configEditSim.add(sim);
    }

    public static void removeEditSim(String sim) {
        configEditSim.remove(sim);
    }

    /**
     * device
     * @param device device
     * @return boolean
     */
    public static boolean putEditDevice(String device) {
        return configEditDevice.add(device);
    }

    public static void removeEditDevice(String device) {
        configEditDevice.remove(device);
    }
}
