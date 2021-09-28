package com.zw.platform.util.common;

import java.io.File;

public class WebPathUtil {

    /**
     * clbs根路径
     */
    public static final String webPath = System.getProperty("clbs.root");

    /**
     * clbs 图片路径
     */
    public static final String mediaPicPath = webPath + File.separator +
            "resources" + File.separator + "img" + File.separator + "media" +
            File.separator;

    public static final String mediaWebPath ="/clbs" +File.separator +
            "resources" + File.separator + "img" + File.separator + "media" +
            File.separator;

}
