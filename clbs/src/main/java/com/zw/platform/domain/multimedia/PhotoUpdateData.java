package com.zw.platform.domain.multimedia;

import lombok.Data;

/**
 * @author XK
 */
@Data
public class PhotoUpdateData {

    /**
     * 驾驶员从业资格证号
     */
    String certificationId;

    /**
     * 人证照片版本
     */
    String version;
}
