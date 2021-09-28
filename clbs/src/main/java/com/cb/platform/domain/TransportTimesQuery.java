package com.cb.platform.domain;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class TransportTimesQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 5477346609346935067L;
    private String id;
    private String vehicleNumber;
}
