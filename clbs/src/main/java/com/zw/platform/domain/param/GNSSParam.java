package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by LiaoYuecai on 2017/4/11.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GNSSParam extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
    private String vid;
    private Integer GPSFlag;
    private Integer beidouFlag;
    private Integer GLONASSFlag;
    private Integer GalileoFlag;
    private Integer GNSSBaudRate;
    private Integer GNSSPositionOutputRate;
    private Integer GNSSPositionCollectRate;
    private Integer GNSSPositionUploadType;
}
