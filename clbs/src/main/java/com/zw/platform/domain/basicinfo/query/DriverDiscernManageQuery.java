package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/24 14:14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DriverDiscernManageQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 393136690363733981L;

    /**
     * 通讯类型 15:交通部JT/T808-2013(苏标); 17:交通部JT/T808-2013(吉标);
     */
    @NotNull
    private Integer deviceType;

    /**
     * 树节点类型 0:企业; 1:分组
     */
    private Integer treeType;
    /**
     * 企业uuid/分组id
     */
    private String treeId;

    /**
     * 企业/分组权限及通讯类型筛选后的车辆ID
     */
    private List<String> vehicleIds;

}
