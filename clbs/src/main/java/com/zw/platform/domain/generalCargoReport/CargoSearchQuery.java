package com.zw.platform.domain.generalCargoReport;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author lijie
 * @date 2018/9/2 15:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CargoSearchQuery extends BaseQueryBean  implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String groupIds;
    private String time;
    private String search;
}
