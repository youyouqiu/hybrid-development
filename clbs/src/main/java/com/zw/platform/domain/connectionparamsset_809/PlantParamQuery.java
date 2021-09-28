package com.zw.platform.domain.connectionparamsset_809;

import java.io.Serializable;

import com.zw.platform.util.common.BaseQueryBean;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlantParamQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String platformName;
}
