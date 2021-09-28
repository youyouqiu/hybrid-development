package com.zw.platform.domain.connectionparamsset_809;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author hujun
 * @date 2018/6/7 11:46
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class T809PlantFormCheck implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ip; //主链路ip/从链路ip
    private Integer centerId; //接入码
    private String groupId; //企业id
    private Integer protocolType; //协议类型
    private String pid; //平台id

}
