package com.zw.platform.domain.basicinfo;

import com.zw.platform.basic.dto.VehiclePurposeDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 车辆用途实体
 * @author tangshunyu
 */
@Data
@NoArgsConstructor
public class VehiclePurpose implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String purposeCategory; //车辆用途
    private String description; //说明
    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;
    private String codeNum;  //识别码

    public VehiclePurpose(VehiclePurposeDTO purposeDTO) {
        this.id = purposeDTO.getId();
        this.codeNum = purposeDTO.getCodeNum();
        this.description = purposeDTO.getDescription();
        this.purposeCategory = purposeDTO.getPurposeCategory();
    }

}
