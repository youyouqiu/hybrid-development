package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 
 * 用户和车关联Form
 * 
 * @author wangying
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserVehicleForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
     * 用户ID
     */
    private String userId;

    /**
     * 车辆ID
     */
    private String vehicleId;

}
